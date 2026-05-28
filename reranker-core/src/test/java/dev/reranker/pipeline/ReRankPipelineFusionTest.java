package dev.reranker.pipeline;

import dev.reranker.engine.MaxAggregator;
import dev.reranker.fusion.LlmClient;
import dev.reranker.fusion.LlmReRanker;
import dev.reranker.fusion.MinMaxNormalizedFusion;
import dev.reranker.fusion.ReciprocalRankFusion;
import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.RankedResult;
import dev.reranker.scoring.BM25Scorer;
import dev.reranker.semantic.CosineSimilarityScorer;
import dev.reranker.tokenizer.StandardTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReRankPipelineFusionTest {

    private static List<Document> sampleDocs() {
        return List.of(
                Document.of("d1", Map.of("body", "the quick brown fox jumps")),
                Document.of("d2", Map.of("body", "lazy dog sleeps all day")),
                Document.of("d3", Map.of("body", "quick fox runs fast")),
                Document.of("d4", Map.of("body", "unrelated content about cooking")));
    }

    @Test
    void test_pipeline_with_fusion_strategy_builds() {
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new ReciprocalRankFusion())
                .build();
        assertTrue(pipeline != null);
    }

    @Test
    void test_pipeline_with_both_aggregator_and_fusion_throws() {
        assertThrows(IllegalStateException.class, () -> ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .aggregator(new MaxAggregator())
                .fusion(new ReciprocalRankFusion())
                .build());
    }

    @Test
    void test_pipeline_with_neither_aggregator_nor_fusion_throws() {
        assertThrows(IllegalStateException.class, () -> ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .build());
    }

    @Test
    void test_pipeline_rrf_end_to_end() {
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new ReciprocalRankFusion())
                .build();
        List<RankedResult> results = pipeline.rank(Query.of("quick fox"), sampleDocs());
        assertEquals(4, results.size());
        // d1 and d3 both mention the query terms; the unrelated doc d4 should rank last.
        assertEquals("d4", results.get(results.size() - 1).document().id());
    }

    @Test
    void test_pipeline_minmax_end_to_end() {
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new MinMaxNormalizedFusion(1.0, 1.0))
                .build();
        List<RankedResult> results = pipeline.rank(Query.of("quick fox"), sampleDocs());
        assertEquals(4, results.size());
        // Highest scoring is one of the docs containing the query terms.
        String topId = results.get(0).document().id();
        assertTrue(topId.equals("d1") || topId.equals("d3"));
    }

    @Test
    void test_pipeline_fusion_results_include_component_scores() {
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new ReciprocalRankFusion())
                .build();
        List<RankedResult> results = pipeline.rank(Query.of("quick fox"), sampleDocs());
        for (RankedResult r : results) {
            assertEquals(2, r.componentScores().size());
        }
    }

    @Test
    void test_pipeline_fusion_respects_topk() {
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new ReciprocalRankFusion())
                .topK(2)
                .build();
        List<RankedResult> results = pipeline.rank(Query.of("quick fox"), sampleDocs());
        assertEquals(2, results.size());
    }

    @Test
    void test_pipeline_fusion_results_sorted_descending() {
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new ReciprocalRankFusion())
                .build();
        List<RankedResult> results = pipeline.rank(Query.of("quick fox"), sampleDocs());
        for (int i = 0; i + 1 < results.size(); i++) {
            assertTrue(results.get(i).score() >= results.get(i + 1).score());
        }
    }

    @Test
    void test_pipeline_with_llm_reranker_end_to_end() {
        LlmClient fake = prompt -> "1,2,3,4";
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new LlmReRanker(fake, new ReciprocalRankFusion()))
                .build();
        List<RankedResult> results = pipeline.rank(Query.of("quick fox"), sampleDocs());
        assertEquals(4, results.size());
        for (RankedResult r : results) {
            assertEquals(2, r.componentScores().size());
        }
    }

    @Test
    void test_pipeline_llm_reranker_respects_topk() {
        LlmClient fake = prompt -> "2,1";
        ReRankPipeline pipeline = ReRankPipeline.builder()
                .tokenizer(StandardTokenizer.withNoStopwords())
                .scorer(new BM25Scorer(StandardTokenizer.withNoStopwords()))
                .scorer(new CosineSimilarityScorer())
                .fusion(new LlmReRanker(fake, new ReciprocalRankFusion()))
                .topK(2)
                .build();
        List<RankedResult> results = pipeline.rank(Query.of("quick fox"), sampleDocs());
        assertEquals(2, results.size());
    }
}
