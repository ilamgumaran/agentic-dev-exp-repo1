package dev.reranker.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RankedResultTest {

    @Test
    void test_ranked_result_stores_document_score_and_components() {
        var doc = Document.of("doc1", Map.of("title", "test"));
        var scores = List.of(new Score("bm25", 1.5), new Score("cosine", 0.8));
        var result = new RankedResult(doc, 2.3, scores);
        assertEquals(doc, result.document());
        assertEquals(2.3, result.score(), 0.001);
        assertEquals(2, result.componentScores().size());
    }

    @Test
    void test_comparable_orders_by_score_descending() {
        var doc1 = Document.of("a", Map.of());
        var doc2 = Document.of("b", Map.of());
        var r1 = new RankedResult(doc1, 1.0, List.of());
        var r2 = new RankedResult(doc2, 2.0, List.of());
        var sorted = new ArrayList<>(List.of(r1, r2));
        Collections.sort(sorted);
        assertEquals("b", sorted.get(0).document().id());
        assertEquals("a", sorted.get(1).document().id());
    }

    @Test
    void test_comparable_breaks_ties_by_document_id_ascending() {
        var doc1 = Document.of("b", Map.of());
        var doc2 = Document.of("a", Map.of());
        var r1 = new RankedResult(doc1, 1.0, List.of());
        var r2 = new RankedResult(doc2, 1.0, List.of());
        var sorted = new ArrayList<>(List.of(r1, r2));
        Collections.sort(sorted);
        assertEquals("a", sorted.get(0).document().id());
        assertEquals("b", sorted.get(1).document().id());
    }

    @Test
    void test_component_scores_list_is_immutable() {
        var doc = Document.of("doc1", Map.of());
        var result = new RankedResult(doc, 1.0, List.of(new Score("bm25", 1.0)));
        assertThrows(UnsupportedOperationException.class,
                () -> result.componentScores().add(new Score("extra", 0.5)));
    }
}
