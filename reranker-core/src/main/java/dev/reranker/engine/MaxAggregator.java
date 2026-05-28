package dev.reranker.engine;

import dev.reranker.model.Score;

import java.util.List;

/**
 * Returns the maximum score value among a document's component scores.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class MaxAggregator implements Aggregator {

    /**
     * @param scores one document's scores, one per scorer (must be non-empty)
     * @return the highest score value
     */
    @Override
    public double aggregate(List<Score> scores) {
        double max = Double.NEGATIVE_INFINITY;
        for (Score s : scores) {
            if (s.value() > max) {
                max = s.value();
            }
        }
        return max;
    }
}
