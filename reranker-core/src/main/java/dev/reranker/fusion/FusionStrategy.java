package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;

import java.util.List;

/**
 * Combines the result lists of multiple scorers into a single fused score per
 * document, using rank-based or normalized score-based fusion.
 *
 * <p>Unlike an {@link dev.reranker.engine.Aggregator}, a fusion strategy sees the
 * full score matrix (all documents across all scorers), which lets rank-based
 * strategies such as RRF compute each document's rank within every scorer's list.
 */
public interface FusionStrategy {

    /**
     * @param query          the original query (used by query-aware strategies such
     *                       as LLM re-ranking; rank/score-based strategies may ignore it)
     * @param documents      the documents being ranked, in a stable order
     * @param scoresByScorer one list of scores per scorer; each inner list is in the
     *                       same order as {@code documents}
     * @return final fused score per document, in the same order as {@code documents}
     */
    double[] fuse(Query query, List<Document> documents, List<List<Score>> scoresByScorer);
}
