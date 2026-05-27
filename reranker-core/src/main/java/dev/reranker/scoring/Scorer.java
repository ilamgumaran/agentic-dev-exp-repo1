package dev.reranker.scoring;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;

import java.util.List;

/**
 * Computes relevance scores for documents against a query.
 */
public interface Scorer {

    /**
     * @param query     the search query
     * @param documents the documents to score
     * @return one Score per document, in the same order as the input
     */
    List<Score> score(Query query, List<Document> documents);
}
