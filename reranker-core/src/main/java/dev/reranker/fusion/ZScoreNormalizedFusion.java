package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;

import java.util.List;
import java.util.Objects;

/**
 * Z-score normalized fusion. Each scorer's scores are standardized via
 * {@code (score - mean) / stddev}, then combined as a weighted sum. If a scorer's
 * standard deviation is 0 (all scores equal) it contributes 0 for every document.
 *
 * <p>Z-score normalization is more robust to outliers than min-max, because an
 * extreme value inflates the standard deviation and is thereby damped.
 *
 * <p>Rank/score-based, so the {@code query} parameter is ignored. Uses the
 * population standard deviation (divide by N).
 *
 * <p>Complexity is O(S × D) where S = scorers and D = documents.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class ZScoreNormalizedFusion implements FusionStrategy {

    private final double[] weights;

    /**
     * @param weights one weight per scorer, in scorer order; must match the scorer
     *                count at {@code fuse} time
     */
    public ZScoreNormalizedFusion(double... weights) {
        this.weights = weights.clone();
    }

    /**
     * @param query          the query (ignored)
     * @param documents      the documents being ranked, in a stable order
     * @param scoresByScorer one list of scores per scorer, each in document order
     * @return the weighted sum of standardized scores per document, in document order
     * @throws IllegalArgumentException if the weight count differs from the scorer count
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
        if (scoresByScorer.size() != weights.length) {
            throw new IllegalArgumentException(
                    "expected " + weights.length + " scorers, got " + scoresByScorer.size());
        }

        for (int s = 0; s < weights.length; s++) {
            List<Score> scores = scoresByScorer.get(s);
            double sum = 0.0;
            for (int i = 0; i < n; i++) {
                sum += scores.get(i).value();
            }
            double mean = sum / n;
            double sumSq = 0.0;
            for (int i = 0; i < n; i++) {
                double d = scores.get(i).value() - mean;
                sumSq += d * d;
            }
            double stddev = Math.sqrt(sumSq / n);
            if (stddev == 0.0) {
                continue; // zero variance → contributes 0 for every document
            }
            double weight = weights[s];
            for (int i = 0; i < n; i++) {
                double norm = (scores.get(i).value() - mean) / stddev;
                fused[i] += weight * norm;
            }
        }
        return fused;
    }
}
