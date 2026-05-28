package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FusionStrategyInterfaceTest {

    @Test
    void test_fusion_strategy_returns_score_per_document() {
        FusionStrategy strategy = new ReciprocalRankFusion();
        Query query = Query.of("anything");
        List<Document> docs = List.of(
                Document.of("a", Map.of("body", "x")),
                Document.of("b", Map.of("body", "y")));
        List<List<Score>> scores = List.of(
                List.of(new Score("s1", 1.0), new Score("s1", 2.0)));
        double[] fused = strategy.fuse(query, docs, scores);
        assertEquals(2, fused.length);
    }
}
