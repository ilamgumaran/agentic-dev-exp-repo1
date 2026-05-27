package dev.reranker.model;

import java.util.Objects;

/**
 * The weight assigned to a document field during scoring.
 *
 * @param fieldName the name of the document field
 * @param weight    the weight (must be >= 0.0)
 */
public record FieldWeight(String fieldName, double weight) {

    public FieldWeight {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        if (weight < 0.0) {
            throw new IllegalArgumentException("weight must be non-negative, got: " + weight);
        }
    }

    /**
     * @param fieldName the field name
     * @param weight    the weight value
     * @return a new FieldWeight
     */
    public static FieldWeight of(String fieldName, double weight) {
        return new FieldWeight(fieldName, weight);
    }
}
