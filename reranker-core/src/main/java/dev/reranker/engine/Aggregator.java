package dev.reranker.engine;

import dev.reranker.model.Score;

import java.util.List;

/**
 * Combines a single document's component scores into one final score.
 *
 * <p>Unlike a {@code FusionStrategy}, an aggregator sees only one document's
 * scores at a time and combines them directly.
 */
public interface Aggregator {

    /**
     * @param scores one document's scores, one per scorer, in scorer order
     * @return the combined final score for that document
     */
    double aggregate(List<Score> scores);
}
