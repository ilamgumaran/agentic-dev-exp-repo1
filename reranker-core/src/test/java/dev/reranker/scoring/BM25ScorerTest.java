package dev.reranker.scoring;

import dev.reranker.model.Document;
import dev.reranker.model.FieldWeight;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
import dev.reranker.tokenizer.StandardTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BM25ScorerTest {

    private static BM25Scorer defaultScorer() {
        return new BM25Scorer(StandardTokenizer.withNoStopwords());
    }

    @Test
    void test_score_single_term_single_document() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(1, scores.size());
        assertTrue(scores.get(0).value() > 0.0);
    }

    @Test
    void test_score_multi_term_single_document() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("quick fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(1, scores.size());
        assertTrue(scores.get(0).value() > 0.0);
    }

    @Test
    void test_score_single_term_multi_document() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox")),
                Document.of("d2", Map.of("body", "lazy dog sleeps")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(2, scores.size());
        assertTrue(scores.get(0).value() > 0.0);
        assertEquals(0.0, scores.get(1).value(), 1e-12);
    }

    @Test
    void test_score_empty_query_returns_zeros() {
        // Build a query whose terms tokenize away (only stopwords), so the
        // effective query is empty after tokenization.
        BM25Scorer scorer = new BM25Scorer(new StandardTokenizer());
        Query query = Query.of("the and of");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "quick brown fox")),
                Document.of("d2", Map.of("body", "lazy dog")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(2, scores.size());
        assertEquals(0.0, scores.get(0).value(), 1e-12);
        assertEquals(0.0, scores.get(1).value(), 1e-12);
    }

    @Test
    void test_score_empty_document_returns_zero() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "")),
                Document.of("d2", Map.of("body", "quick brown fox")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(0.0, scores.get(0).value(), 1e-12);
        assertTrue(scores.get(1).value() > 0.0);
    }

    @Test
    void test_score_term_not_in_document_contributes_zero() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("elephant");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "quick brown fox")),
                Document.of("d2", Map.of("body", "lazy dog")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(0.0, scores.get(0).value(), 1e-12);
        assertEquals(0.0, scores.get(1).value(), 1e-12);
    }

    @Test
    void test_higher_term_frequency_gives_higher_score() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "fox fox fox")),
                Document.of("d2", Map.of("body", "fox other words here")));
        List<Score> scores = scorer.score(query, docs);
        assertTrue(scores.get(0).value() > scores.get(1).value());
    }

    @Test
    void test_document_length_normalization() {
        // Two docs with same single occurrence of the term; the shorter doc
        // should score higher under default b > 0.
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("short", Map.of("body", "quick fox")),
                Document.of("long", Map.of("body",
                        "fox among many many many many many many other words here today")));
        List<Score> scores = scorer.score(query, docs);
        assertTrue(scores.get(0).value() > scores.get(1).value());
    }

    @Test
    void test_idf_weighting_rare_terms_score_higher() {
        BM25Scorer scorer = defaultScorer();
        // "common" appears in all docs (low IDF); "rare" appears in one (high IDF).
        Query query = Query.of("common rare");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "common rare")),
                Document.of("d2", Map.of("body", "common term")),
                Document.of("d3", Map.of("body", "common word")));
        List<Score> scores = scorer.score(query, docs);
        // d1 contains the rare term and should score noticeably higher than d2/d3.
        assertTrue(scores.get(0).value() > scores.get(1).value());
        assertTrue(scores.get(0).value() > scores.get(2).value());
    }

    @Test
    void test_default_config_k1_and_b() {
        BM25Config config = BM25Config.defaults();
        assertEquals(1.2, config.k1(), 1e-12);
        assertEquals(0.75, config.b(), 1e-12);
    }

    @Test
    void test_custom_k1_zero_ignores_term_frequency() {
        // With k1=0, the tf component reduces to (tf*(0+1))/(tf+0) = 1, so a doc
        // with the term once scores the same as a doc with the term many times.
        BM25Scorer scorer = new BM25Scorer(new BM25Config(0.0, 0.75),
                StandardTokenizer.withNoStopwords());
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "fox fox fox padding words here now")),
                Document.of("d2", Map.of("body", "fox padding words here now extra")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(scores.get(0).value(), scores.get(1).value(), 1e-9);
    }

    @Test
    void test_custom_b_zero_no_length_normalization() {
        // With b=0, document length should not affect the score: two docs with the
        // same term frequency but different lengths score equally.
        BM25Scorer scorer = new BM25Scorer(new BM25Config(1.2, 0.0),
                StandardTokenizer.withNoStopwords());
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("short", Map.of("body", "fox here")),
                Document.of("long", Map.of("body",
                        "fox here with many additional padding words present today")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(scores.get(0).value(), scores.get(1).value(), 1e-9);
    }

    @Test
    void test_custom_b_one_full_length_normalization() {
        BM25Scorer scorer = new BM25Scorer(new BM25Config(1.2, 1.0),
                StandardTokenizer.withNoStopwords());
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("short", Map.of("body", "fox here")),
                Document.of("long", Map.of("body",
                        "fox here with many additional padding words present today")));
        List<Score> scores = scorer.score(query, docs);
        // Full length normalization: the shorter doc scores strictly higher.
        assertTrue(scores.get(0).value() > scores.get(1).value());
    }

    @Test
    void test_single_document_avgdl_equals_doc_length() {
        // For a single document, |D| == avgdl so the length normalization factor is
        // (1 - b + b*1) = 1, giving score = IDF * (tf*(k1+1))/(tf+k1).
        double k1 = 1.2;
        double b = 0.75;
        BM25Scorer scorer = new BM25Scorer(new BM25Config(k1, b),
                StandardTokenizer.withNoStopwords());
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox")));
        List<Score> scores = scorer.score(query, docs);
        // N=1, n=1: IDF = ln((1 - 1 + 0.5)/(1 + 0.5) + 1) = ln(0.5/1.5 + 1).
        double idf = Math.log((1 - 1 + 0.5) / (1 + 0.5) + 1);
        double tf = 1;
        double expected = idf * (tf * (k1 + 1)) / (tf + k1); // length factor == 1
        assertEquals(expected, scores.get(0).value(), 1e-9);
    }

    @Test
    void test_multi_field_scoring_with_equal_weights() {
        BM25Scorer scorer = new BM25Scorer(StandardTokenizer.withNoStopwords());
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("title", "fox", "body", "fox runs")),
                Document.of("d2", Map.of("title", "dog", "body", "dog runs")));
        List<Score> scores = scorer.score(query, docs);
        assertTrue(scores.get(0).value() > 0.0);
        assertEquals(0.0, scores.get(1).value(), 1e-12);
    }

    @Test
    void test_multi_field_scoring_with_custom_weights() {
        // Title weighted much higher than body: a doc with the term in the title
        // should beat a doc with the term only in the body.
        List<FieldWeight> weights = List.of(
                FieldWeight.of("title", 10.0),
                FieldWeight.of("body", 1.0));
        BM25Scorer scorer = new BM25Scorer(BM25Config.defaults(),
                StandardTokenizer.withNoStopwords(), weights);
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("titleMatch", Map.of("title", "fox here", "body", "nothing relevant")),
                Document.of("bodyMatch", Map.of("title", "nothing relevant", "body", "fox here")));
        List<Score> scores = scorer.score(query, docs);
        assertTrue(scores.get(0).value() > scores.get(1).value());
    }

    @Test
    void test_multi_field_zero_weight_ignores_field() {
        // body has weight 0, so a match only in body contributes nothing.
        List<FieldWeight> weights = List.of(
                FieldWeight.of("title", 1.0),
                FieldWeight.of("body", 0.0));
        BM25Scorer scorer = new BM25Scorer(BM25Config.defaults(),
                StandardTokenizer.withNoStopwords(), weights);
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("title", "cat dog", "body", "fox fox fox")),
                Document.of("d2", Map.of("title", "cat dog", "body", "nothing here")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(0.0, scores.get(0).value(), 1e-12);
        assertEquals(0.0, scores.get(1).value(), 1e-12);
    }

    @Test
    void test_score_is_non_negative() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("fox dog cat bird");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "fox dog")),
                Document.of("d2", Map.of("body", "cat bird fish")),
                Document.of("d3", Map.of("body", "nothing relevant at all here")),
                Document.of("d4", Map.of("body", "fox fox fox dog cat bird")));
        List<Score> scores = scorer.score(query, docs);
        for (Score s : scores) {
            assertTrue(s.value() >= 0.0, "score must be non-negative: " + s.value());
        }
    }

    @Test
    void test_score_deterministic_same_input_same_output() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("quick fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox")),
                Document.of("d2", Map.of("body", "quick quick fox dog")));
        List<Score> first = scorer.score(query, docs);
        List<Score> second = scorer.score(query, docs);
        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).value(), second.get(i).value(), 1e-12);
        }
    }

    @Test
    void test_scores_returned_in_document_order() {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("zebra", Map.of("body", "fox")),
                Document.of("alpha", Map.of("body", "fox fox")),
                Document.of("mango", Map.of("body", "no match")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(3, scores.size());
        // Order corresponds to input docs, not sorted by score or id.
        assertTrue(scores.get(0).value() > 0.0);
        assertTrue(scores.get(1).value() > 0.0);
        assertEquals(0.0, scores.get(2).value(), 1e-12);
    }

    @Test
    void test_thread_safety_concurrent_scoring() throws Exception {
        BM25Scorer scorer = defaultScorer();
        Query query = Query.of("quick brown fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox jumps")),
                Document.of("d2", Map.of("body", "lazy dog sleeps all day")),
                Document.of("d3", Map.of("body", "quick quick brown fox fox")));
        List<Double> expected = scorer.score(query, docs).stream().map(Score::value).toList();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<List<Double>>> futures = IntStream.range(0, 200)
                    .mapToObj(i -> pool.submit(() ->
                            scorer.score(query, docs).stream().map(Score::value).toList()))
                    .toList();
            for (Future<List<Double>> f : futures) {
                assertEquals(expected, f.get());
            }
        } finally {
            pool.shutdownNow();
        }
        assertFalse(expected.isEmpty());
    }
}
