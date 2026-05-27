package dev.reranker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    @Test
    void test_score_stores_scorer_name_and_value() {
        var score = new Score("bm25", 1.5);
        assertEquals("bm25", score.scorerName());
        assertEquals(1.5, score.value(), 0.001);
    }

    @Test
    void test_score_with_zero_value_is_valid() {
        var score = new Score("bm25", 0.0);
        assertEquals(0.0, score.value(), 0.001);
    }

    @Test
    void test_score_with_positive_value_is_valid() {
        var score = new Score("bm25", 42.0);
        assertEquals(42.0, score.value(), 0.001);
    }

    @Test
    void test_score_with_negative_value_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> new Score("bm25", -1.0));
    }
}
