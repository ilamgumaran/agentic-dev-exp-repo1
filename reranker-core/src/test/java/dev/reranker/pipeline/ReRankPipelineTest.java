package dev.reranker.pipeline;

import dev.reranker.engine.AverageAggregator;
import dev.reranker.engine.MaxAggregator;
import dev.reranker.engine.WeightedAggregator;
import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.RankedResult;
import dev.reranker.scoring.BM25Scorer;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReRankPipelineTest {

    private static ReRankPipeline.Builder baseBuilder() {
        return ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .aggregator(new MaxAggregator());
    }

    @Test
    void test_build_with_all_components_succeeds() {
        ReRankPipeline pipeline = baseBuilder().build();
        assertTrue(pipeline != null);
    }

    @Test
    void test_build_without_tokenizer_throws() {
        assertThrows(IllegalStateException.class, () -> ReRankPipeline.builder()
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .aggregator(new MaxAggregator())
                .build());
    }

    @Test
    void test_build_without_scorer_throws() {
        assertThrows(IllegalStateException.class, () -> ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .aggregator(new MaxAggregator())
                .build());
    }

    @Test
    void test_build_without_aggregator_throws() {
        assertThrows(IllegalStateException.class, () -> ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .build());
    }

    @Test
    void test_rank_single_scorer_single_document() {
        ReRankPipeline pipeline = baseBuilder().build();
        Query query = Query.of("fox");
        List<RankedResult> results = pipeline.rank(query,
                List.of(Document.of("d1", Map.of("body", "quick brown fox"))));
        assertEquals(1, results.size());
        assertEquals("d1", results.get(0).document().id());
        assertTrue(results.get(0).score() > 0.0);
    }

    @Test
    void test_rank_single_scorer_multi_document_sorted() {
        ReRankPipeline pipeline = baseBuilder().build();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("low", Map.of("body", "fox among many other unrelated padding words here")),
                Document.of("high", Map.of("body", "fox fox")),
                Document.of("none", Map.of("body", "nothing relevant")));
        List<RankedResult> results = pipeline.rank(query, docs);
        assertEquals(3, results.size());
        // Sorted by score descending.
        assertTrue(results.get(0).score() >= results.get(1).score());
        assertTrue(results.get(1).score() >= results.get(2).score());
        assertEquals("high", results.get(0).document().id());
        assertEquals("none", results.get(2).document().id());
    }

    @Test
    void test_rank_multi_scorer_weighted_aggregation() {
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .aggregator(new WeightedAggregator(0.5, 0.5))
                .build();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "quick brown fox")),
                Document.of("d2", Map.of("body", "lazy dog")));
        List<RankedResult> results = pipeline.rank(query, docs);
        assertEquals(2, results.size());
        // Each result has two component scores (one per scorer).
        assertEquals(2, results.get(0).componentScores().size());
    }

    @Test
    void test_rank_with_top_k_truncates() {
        ReRankPipeline pipeline = baseBuilder().topK(2).build();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("a", Map.of("body", "fox fox fox")),
                Document.of("b", Map.of("body", "fox fox")),
                Document.of("c", Map.of("body", "fox")),
                Document.of("d", Map.of("body", "nothing")));
        List<RankedResult> results = pipeline.rank(query, docs);
        assertEquals(2, results.size());
    }

    @Test
    void test_rank_with_top_k_zero_returns_empty() {
        ReRankPipeline pipeline = baseBuilder().topK(0).build();
        Query query = Query.of("fox");
        List<Document> docs = List.of(Document.of("a", Map.of("body", "fox")));
        assertTrue(pipeline.rank(query, docs).isEmpty());
    }

    @Test
    void test_rank_with_top_k_greater_than_docs_returns_all() {
        ReRankPipeline pipeline = baseBuilder().topK(100).build();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("a", Map.of("body", "fox")),
                Document.of("b", Map.of("body", "fox fox")));
        assertEquals(2, pipeline.rank(query, docs).size());
    }

    @Test
    void test_rank_empty_documents_returns_empty() {
        ReRankPipeline pipeline = baseBuilder().build();
        Query query = Query.of("fox");
        assertTrue(pipeline.rank(query, List.of()).isEmpty());
    }

    @Test
    void test_rank_empty_query_returns_zero_scores() {
        // A query that tokenizes to nothing (only stopwords) yields all-zero scores.
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(new StandardTokenizer())
                .scorer(new BM25Scorer(new StandardTokenizer()))
                .aggregator(new MaxAggregator())
                .build();
        Query query = Query.of("the and of");
        List<Document> docs = List.of(
                Document.of("b", Map.of("body", "quick fox")),
                Document.of("a", Map.of("body", "lazy dog")));
        List<RankedResult> results = pipeline.rank(query, docs);
        assertEquals(2, results.size());
        for (RankedResult r : results) {
            assertEquals(0.0, r.score(), 1e-12);
        }
        // All scores zero, sorted by id ascending.
        assertEquals("a", results.get(0).document().id());
        assertEquals("b", results.get(1).document().id());
    }

    @Test
    void test_rank_results_sorted_by_score_descending() {
        ReRankPipeline pipeline = baseBuilder().build();
        Query query = Query.of("fox");
        List<Document> docs = List.of(
                Document.of("a", Map.of("body", "fox")),
                Document.of("b", Map.of("body", "fox fox fox")),
                Document.of("c", Map.of("body", "fox fox")));
        List<RankedResult> results = pipeline.rank(query, docs);
        for (int i = 0; i + 1 < results.size(); i++) {
            assertTrue(results.get(i).score() >= results.get(i + 1).score());
        }
    }

    @Test
    void test_rank_tiebreak_by_document_id_ascending() {
        ReRankPipeline pipeline = baseBuilder().build();
        Query query = Query.of("fox");
        // Both docs identical content => identical scores => tiebreak by id.
        List<Document> docs = List.of(
                Document.of("zebra", Map.of("body", "fox here")),
                Document.of("alpha", Map.of("body", "fox here")));
        List<RankedResult> results = pipeline.rank(query, docs);
        assertEquals("alpha", results.get(0).document().id());
        assertEquals("zebra", results.get(1).document().id());
    }

    @Test
    void test_ranked_results_include_component_scores() {
        ReRankPipeline pipeline = baseBuilder().build();
        Query query = Query.of("fox");
        List<RankedResult> results = pipeline.rank(query,
                List.of(Document.of("d1", Map.of("body", "quick fox"))));
        assertEquals(1, results.get(0).componentScores().size());
        assertEquals("bm25", results.get(0).componentScores().get(0).scorerName());
    }

    @Test
    void test_pipeline_is_immutable_after_build() {
        ReRankPipeline.Builder builder = baseBuilder();
        ReRankPipeline pipeline = builder.build();
        // Mutating the builder after build must not affect the built pipeline.
        builder.topK(0);
        Query query = Query.of("fox");
        List<RankedResult> results = pipeline.rank(query,
                List.of(Document.of("d1", Map.of("body", "fox"))));
        assertEquals(1, results.size());
    }

    @Test
    void test_pipeline_is_thread_safe() throws Exception {
        ReRankPipeline pipeline = baseBuilder().build();
        Query query = Query.of("quick brown fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox jumps")),
                Document.of("d2", Map.of("body", "lazy dog sleeps")),
                Document.of("d3", Map.of("body", "quick quick fox fox")));
        List<String> expected = pipeline.rank(query, docs).stream()
                .map(r -> r.document().id()).toList();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<List<String>>> futures = IntStream.range(0, 200)
                    .mapToObj(i -> pool.submit(() ->
                            pipeline.rank(query, docs).stream()
                                    .map(r -> r.document().id()).toList()))
                    .toList();
            for (Future<List<String>> f : futures) {
                assertEquals(expected, f.get());
            }
        } finally {
            pool.shutdownNow();
        }
        assertFalse(expected.isEmpty());
    }
}
