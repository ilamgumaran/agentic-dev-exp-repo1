package dev.reranker.engine;

import dev.reranker.model.Score;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaxAggregatorTest {

    @Test
    void test_max_aggregate_returns_highest() {
        MaxAggregator agg = new MaxAggregator();
        double result = agg.aggregate(List.of(
                new Score("s1", 3.0), new Score("s2", 9.0), new Score("s3", 1.0)));
        assertEquals(9.0, result, 1e-12);
    }

    @Test
    void test_max_aggregate_single_score() {
        MaxAggregator agg = new MaxAggregator();
        double result = agg.aggregate(List.of(new Score("s1", 4.2)));
        assertEquals(4.2, result, 1e-12);
    }

    @Test
    void test_max_aggregate_all_zeros() {
        MaxAggregator agg = new MaxAggregator();
        double result = agg.aggregate(List.of(
                new Score("s1", 0.0), new Score("s2", 0.0)));
        assertEquals(0.0, result, 1e-12);
    }
}
