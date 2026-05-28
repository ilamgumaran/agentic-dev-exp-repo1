package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;

import java.util.List;
import java.util.Objects;

/**
 * Min-max normalized linear (convex) fusion. Each scorer's scores are normalized
 * to {@code [0,1]} via {@code (score - min) / (max - min)}, then combined as a
 * weighted sum. If a scorer's scores are all equal ({@code max == min}) it
 * contributes 0 for every document.
 *
 * <p>Normalizing before combining solves the incomparable-scale problem: a
 * large-magnitude scorer (e.g. BM25) can no longer dominate a bounded scorer
 * (e.g. cosine) purely by scale.
 *
 * <p>Rank/score-based, so the {@code query} parameter is ignored.
 *
 * <p>Complexity is O(S × D) where S = scorers and D = documents.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class MinMaxNormalizedFusion implements FusionStrategy {

    private final double[] weights;

    /**
     * @param weights one weight per scorer, in scorer order; must match the scorer
     *                count at {@code fuse} time
     */
    public MinMaxNormalizedFusion(double... weights) {
        this.weights = weights.clone();
    }

    /**
     * @param query          the query (ignored)
     * @param documents      the documents being ranked, in a stable order
     * @param scoresByScorer one list of scores per scorer, each in document order
     * @return the weighted sum of normalized scores per document, in document order
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
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < n; i++) {
                double v = scores.get(i).value();
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            }
            double range = max - min;
            if (range == 0.0) {
                continue; // all equal → contributes 0 for every document
            }
            double weight = weights[s];
            for (int i = 0; i < n; i++) {
                double norm = (scores.get(i).value() - min) / range;
                fused[i] += weight * norm;
            }
        }
        return fused;
    }
}
