package dev.reranker.model;

import java.util.List;
import java.util.Objects;

/**
 * A document with its final aggregated score and component scores from each scorer.
 *
 * @param document        the original document
 * @param score           the final combined score
 * @param componentScores individual scores from each scorer
 */
public record RankedResult(Document document, double score, List<Score> componentScores)
        implements Comparable<RankedResult> {

    public RankedResult {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(componentScores, "componentScores must not be null");
        componentScores = List.copyOf(componentScores);
    }

    @Override
    public int compareTo(RankedResult other) {
        int scoreCompare = Double.compare(other.score, this.score);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return this.document.id().compareTo(other.document.id());
    }
}
