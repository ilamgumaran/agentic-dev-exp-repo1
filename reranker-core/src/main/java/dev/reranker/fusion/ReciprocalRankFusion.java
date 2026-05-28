package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;

import java.util.List;
import java.util.Objects;

/**
 * Reciprocal Rank Fusion (RRF). Combines scorers by summing reciprocal-rank
 * contributions: {@code Σ 1/(k + rank_i)} across scorers, where each scorer
 * assigns distinct 1-based ranks (highest score = rank 1, ties broken by document
 * id ascending).
 *
 * <p>RRF is rank-based, so it ignores the {@code query} parameter and is immune to
 * the incomparable-scale problem that affects raw score combination.
 *
 * <p>Complexity is O(S × D log D) where S = scorers and D = documents.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class ReciprocalRankFusion implements FusionStrategy {

    private final RrfConfig config;

    /**
     * Creates an RRF strategy with the default rank constant (60).
     */
    public ReciprocalRankFusion() {
        this(RrfConfig.defaults());
    }

    /**
     * Creates an RRF strategy with a custom rank constant.
     *
     * @param config the RRF configuration
     */
    public ReciprocalRankFusion(RrfConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * @param query          the query (ignored by RRF)
     * @param documents      the documents being ranked, in a stable order
     * @param scoresByScorer one list of scores per scorer, each in document order
     * @return the RRF score per document, in document order
     */
    @Override
    public double[] fuse(Query query, List<Document> documents, List<List<Score>> scoresByScorer) {
        Objects.requireNonNull(documents, "documents must not be null");
        Objects.requireNonNull(scoresByScorer, "scoresByScorer must not be null");

        int n = documents.size();
        double[] fused = new double[n];
        if (n == 0) {
            return fused;
        }
        int k = config.rankConstant();
        for (List<Score> scores : scoresByScorer) {
            int[] ranks = Ranks.assign(documents, scores);
            for (int i = 0; i < n; i++) {
                fused[i] += 1.0 / (k + ranks[i]);
            }
        }
        return fused;
    }
}
