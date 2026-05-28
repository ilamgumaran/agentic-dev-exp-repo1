package dev.reranker.engine;

import dev.reranker.model.Score;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightedAggregatorTest {

    @Test
    void test_weighted_aggregate_single_score() {
        WeightedAggregator agg = new WeightedAggregator(0.5);
        double result = agg.aggregate(List.of(new Score("s1", 4.0)));
        assertEquals(2.0, result, 1e-12);
    }

    @Test
    void test_weighted_aggregate_multi_score() {
        WeightedAggregator agg = new WeightedAggregator(0.7, 0.3);
        double result = agg.aggregate(List.of(new Score("s1", 10.0), new Score("s2", 20.0)));
        assertEquals(0.7 * 10.0 + 0.3 * 20.0, result, 1e-12);
    }

    @Test
    void test_weighted_aggregate_wrong_count_throws() {
        WeightedAggregator agg = new WeightedAggregator(0.7, 0.3);
        assertThrows(IllegalArgumentException.class,
                () -> agg.aggregate(List.of(new Score("s1", 1.0))));
    }

    @Test
    void test_weighted_aggregate_zero_weight_ignores_score() {
        WeightedAggregator agg = new WeightedAggregator(1.0, 0.0);
        double result = agg.aggregate(List.of(new Score("s1", 5.0), new Score("s2", 100.0)));
        assertEquals(5.0, result, 1e-12);
    }
}
