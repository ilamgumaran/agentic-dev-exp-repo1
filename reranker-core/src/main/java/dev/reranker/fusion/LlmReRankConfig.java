package dev.reranker.fusion;

/**
 * Configuration for {@link LlmReRanker}.
 *
 * @param candidateCount    how many top base-ranked documents to send to the LLM
 *                          (must be {@code >= 1})
 * @param mergeRankConstant the RRF rank constant used to merge the base and LLM
 *                          rankings (must be {@code >= 1})
 */
public record LlmReRankConfig(int candidateCount, int mergeRankConstant) {

    /** Default number of candidates sent to the LLM. */
    public static final int DEFAULT_CANDIDATE_COUNT = 10;

    /** Default RRF rank constant for merging base and LLM rankings. */
    public static final int DEFAULT_MERGE_RANK_CONSTANT = 60;

    public LlmReRankConfig {
        if (candidateCount < 1) {
            throw new IllegalArgumentException(
                    "candidateCount must be >= 1, got: " + candidateCount);
        }
        if (mergeRankConstant < 1) {
            throw new IllegalArgumentException(
                    "mergeRankConstant must be >= 1, got: " + mergeRankConstant);
        }
    }

    /**
     * @return a config with the defaults (candidateCount=10, mergeRankConstant=60)
     */
    public static LlmReRankConfig defaults() {
        return new LlmReRankConfig(DEFAULT_CANDIDATE_COUNT, DEFAULT_MERGE_RANK_CONSTANT);
    }
}
