package dev.reranker.fusion;

/**
 * Configuration for Reciprocal Rank Fusion.
 *
 * @param rankConstant the RRF rank constant k (must be {@code >= 1}); larger
 *                     values flatten the contribution of top ranks
 */
public record RrfConfig(int rankConstant) {

    /** Default RRF rank constant. */
    public static final int DEFAULT_RANK_CONSTANT = 60;

    public RrfConfig {
        if (rankConstant < 1) {
            throw new IllegalArgumentException("rankConstant must be >= 1, got: " + rankConstant);
        }
    }

    /**
     * @return a config using the default rank constant (60)
     */
    public static RrfConfig defaults() {
        return new RrfConfig(DEFAULT_RANK_CONSTANT);
    }
}
