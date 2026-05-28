package dev.reranker.semantic;

/**
 * Configuration for character n-gram extraction.
 *
 * @param n the character n-gram size (must be {@code >= 1})
 */
public record NGramConfig(int n) {

    /** Default n-gram size (trigrams). */
    public static final int DEFAULT_N = 3;

    public NGramConfig {
        if (n < 1) {
            throw new IllegalArgumentException("n must be >= 1, got: " + n);
        }
    }

    /**
     * @return a config using the default n-gram size (3)
     */
    public static NGramConfig defaults() {
        return new NGramConfig(DEFAULT_N);
    }
}
