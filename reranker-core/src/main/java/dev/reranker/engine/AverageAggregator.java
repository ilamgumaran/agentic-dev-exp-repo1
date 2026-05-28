package dev.reranker.engine;

import dev.reranker.model.Score;

import java.util.List;

/**
 * Returns the arithmetic mean of a document's component score values.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class AverageAggregator implements Aggregator {

    /**
     * @param scores one document's scores, one per scorer (must be non-empty)
     * @return the arithmetic mean of the score values
     */
    @Override
    public double aggregate(List<Score> scores) {
        double sum = 0.0;
        for (Score s : scores) {
            sum += s.value();
        }
        return sum / scores.size();
    }
}
