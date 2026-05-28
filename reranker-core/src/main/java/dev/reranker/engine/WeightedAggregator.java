package dev.reranker.engine;

import dev.reranker.model.Score;

import java.util.List;

/**
 * Combines scores as a weighted sum: {@code Σ weight_i × score_i.value()}.
 *
 * <p>The number of weights must match the number of scores supplied at
 * aggregation time; otherwise an {@link IllegalArgumentException} is thrown.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class WeightedAggregator implements Aggregator {

    private final double[] weights;

    /**
     * @param weights one weight per scorer, in scorer order
     */
    public WeightedAggregator(double... weights) {
        this.weights = weights.clone();
    }

    /**
     * @return the number of weights this aggregator was configured with
     */
    public int weightCount() {
        return weights.length;
    }

    /**
     * @param scores one document's scores, one per scorer
     * @return the weighted sum of the score values
     * @throws IllegalArgumentException if the score count differs from the weight count
     */
    @Override
    public double aggregate(List<Score> scores) {
        if (scores.size() != weights.length) {
            throw new IllegalArgumentException(
                    "expected " + weights.length + " scores, got " + scores.size());
        }
        double sum = 0.0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i] * scores.get(i).value();
        }
        return sum;
    }
}
