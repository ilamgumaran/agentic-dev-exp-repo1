package dev.reranker.engine;

import dev.reranker.model.Score;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AverageAggregatorTest {

    @Test
    void test_average_aggregate_returns_mean() {
        AverageAggregator agg = new AverageAggregator();
        double result = agg.aggregate(List.of(
                new Score("s1", 2.0), new Score("s2", 4.0), new Score("s3", 6.0)));
        assertEquals(4.0, result, 1e-12);
    }

    @Test
    void test_average_aggregate_single_score() {
        AverageAggregator agg = new AverageAggregator();
        double result = agg.aggregate(List.of(new Score("s1", 7.0)));
        assertEquals(7.0, result, 1e-12);
    }

    @Test
    void test_average_aggregate_all_zeros() {
        AverageAggregator agg = new AverageAggregator();
        double result = agg.aggregate(List.of(
                new Score("s1", 0.0), new Score("s2", 0.0)));
        assertEquals(0.0, result, 1e-12);
    }
}
