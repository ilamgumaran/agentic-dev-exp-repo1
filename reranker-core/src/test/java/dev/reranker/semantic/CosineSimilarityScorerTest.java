package dev.reranker.semantic;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
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

class CosineSimilarityScorerTest {

    @Test
    void test_identical_text_scores_one() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("hello world");
        List<Document> docs = List.of(Document.of("d1", Map.of("body", "hello world")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(1.0, scores.get(0).value(), 1e-9);
    }

    @Test
    void test_no_shared_ngrams_scores_zero() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("abcdef");
        List<Document> docs = List.of(Document.of("d1", Map.of("body", "xyzuvw")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(0.0, scores.get(0).value(), 1e-9);
    }

    @Test
    void test_partial_overlap_scores_between_zero_and_one() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("running");
        List<Document> docs = List.of(Document.of("d1", Map.of("body", "running shoes")));
        List<Score> scores = scorer.score(query, docs);
        double v = scores.get(0).value();
        assertTrue(v > 0.0 && v < 1.0, "expected strictly between 0 and 1, got " + v);
    }

    @Test
    void test_morphological_match_running_runner() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("running");
        List<Document> docs = List.of(
                Document.of("related", Map.of("body", "runner")),
                Document.of("unrelated", Map.of("body", "elephant")));
        List<Score> scores = scorer.score(query, docs);
        // "running" and "runner" share trigrams (run, unn); "elephant" shares none.
        assertTrue(scores.get(0).value() > 0.0);
        assertTrue(scores.get(0).value() > scores.get(1).value());
    }

    @Test
    void test_scores_are_bounded_zero_to_one() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("machine learning models");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "deep learning machines")),
                Document.of("d2", Map.of("body", "totally different content here")),
                Document.of("d3", Map.of("body", "machine learning models")),
                Document.of("d4", Map.of("body", "")));
        List<Score> scores = scorer.score(query, docs);
        for (Score s : scores) {
            assertTrue(s.value() >= 0.0 && s.value() <= 1.0,
                    "score out of [0,1]: " + s.value());
        }
    }

    @Test
    void test_empty_document_fields_scores_zero() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("anything");
        List<Document> docs = List.of(Document.of("d1", Map.of("body", "")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(0.0, scores.get(0).value(), 1e-9);
    }

    @Test
    void test_scores_returned_in_document_order() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("hello");
        List<Document> docs = List.of(
                Document.of("zebra", Map.of("body", "hello")),
                Document.of("alpha", Map.of("body", "goodbye")),
                Document.of("mango", Map.of("body", "hello")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(3, scores.size());
        assertEquals(1.0, scores.get(0).value(), 1e-9);
        assertTrue(scores.get(1).value() < 1.0);
        assertEquals(1.0, scores.get(2).value(), 1e-9);
    }

    @Test
    void test_multi_field_document_concatenated() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("hello world");
        // Across two fields the concatenation contains both query words.
        List<Document> multi = List.of(
                Document.of("d1", new java.util.TreeMap<>(Map.of("title", "hello", "body", "world"))));
        List<Document> single = List.of(
                Document.of("d2", Map.of("body", "hello")));
        double multiScore = scorer.score(query, multi).get(0).value();
        double singleScore = scorer.score(query, single).get(0).value();
        // The multi-field doc covers more of the query than the single-field doc.
        assertTrue(multiScore > singleScore);
    }

    @Test
    void test_custom_ngram_size() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer(new NGramConfig(2));
        Query query = Query.of("hello world");
        List<Document> docs = List.of(Document.of("d1", Map.of("body", "hello world")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(1.0, scores.get(0).value(), 1e-9);
    }

    @Test
    void test_single_character_query_does_not_throw() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer(); // n=3
        Query query = Query.of("a");
        List<Document> docs = List.of(Document.of("d1", Map.of("body", "a")));
        List<Score> scores = scorer.score(query, docs);
        // Single-character text shorter than n: handled without error; identical text → 1.0.
        assertEquals(1.0, scores.get(0).value(), 1e-9);
    }

    @Test
    void test_case_insensitive() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("Hello World");
        List<Document> docs = List.of(Document.of("d1", Map.of("body", "hello world")));
        List<Score> scores = scorer.score(query, docs);
        assertEquals(1.0, scores.get(0).value(), 1e-9);
    }

    @Test
    void test_deterministic_same_input_same_output() {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("machine learning");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "machine learning systems")),
                Document.of("d2", Map.of("body", "deep neural nets")));
        List<Score> first = scorer.score(query, docs);
        List<Score> second = scorer.score(query, docs);
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).value(), second.get(i).value(), 1e-12);
        }
    }

    @Test
    void test_thread_safe_concurrent_scoring() throws Exception {
        CosineSimilarityScorer scorer = new CosineSimilarityScorer();
        Query query = Query.of("machine learning models");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "machine learning")),
                Document.of("d2", Map.of("body", "learning models here")),
                Document.of("d3", Map.of("body", "unrelated content")));
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
