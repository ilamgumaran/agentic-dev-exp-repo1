package dev.reranker.model;

import java.util.Objects;

/**
 * A relevance score produced by a single scorer.
 *
 * @param scorerName identifies which scorer produced this score
 * @param value      the score value (must be non-negative)
 */
public record Score(String scorerName, double value) {

    public Score {
        Objects.requireNonNull(scorerName, "scorerName must not be null");
        if (value < 0.0) {
            throw new IllegalArgumentException("score value must be non-negative, got: " + value);
        }
    }
}
