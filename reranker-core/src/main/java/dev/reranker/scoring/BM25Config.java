package dev.reranker.scoring;

/**
 * Configuration parameters for the BM25 scoring algorithm.
 *
 * @param k1 term frequency saturation parameter (must be {@code >= 0})
 * @param b  document length normalization (must be in {@code [0.0, 1.0]})
 */
public record BM25Config(double k1, double b) {

    /** Default term frequency saturation parameter. */
    public static final double DEFAULT_K1 = 1.2;

    /** Default document length normalization parameter. */
    public static final double DEFAULT_B = 0.75;

    public BM25Config {
        if (k1 < 0.0) {
            throw new IllegalArgumentException("k1 must be non-negative, got: " + k1);
        }
        if (b < 0.0 || b > 1.0) {
            throw new IllegalArgumentException("b must be in [0.0, 1.0], got: " + b);
        }
    }

    /**
     * @return a config with the standard BM25 defaults (k1=1.2, b=0.75)
     */
    public static BM25Config defaults() {
        return new BM25Config(DEFAULT_K1, DEFAULT_B);
    }
}
