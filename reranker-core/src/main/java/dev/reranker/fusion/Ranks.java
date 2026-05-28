package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Score;

import java.util.List;

/**
 * Internal helper for rank-based fusion: assigns distinct 1-based ranks to
 * documents from a single scorer's scores.
 */
final class Ranks {

    private Ranks() {
    }

    /**
     * Computes distinct 1-based ranks for the given scorer's scores. The highest
     * score is rank 1. Ties are broken by document id ascending so that every
     * document receives a distinct rank (no rank sharing), making the result
     * deterministic.
     *
     * @param documents the documents, in their stable order
     * @param scores    that scorer's scores, in the same order as {@code documents}
     * @return an array where {@code result[i]} is the 1-based rank of
     *         {@code documents.get(i)}
     */
    static int[] assign(List<Document> documents, List<Score> scores) {
        int n = documents.size();
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        java.util.Arrays.sort(order, (i, j) -> {
            int byScore = Double.compare(scores.get(j).value(), scores.get(i).value());
            if (byScore != 0) {
                return byScore;
            }
            return documents.get(i).id().compareTo(documents.get(j).id());
        });
        int[] ranks = new int[n];
        for (int rank = 1; rank <= n; rank++) {
            ranks[order[rank - 1]] = rank;
        }
        return ranks;
    }
}
