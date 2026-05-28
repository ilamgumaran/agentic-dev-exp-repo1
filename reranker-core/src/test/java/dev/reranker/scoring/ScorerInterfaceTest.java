package dev.reranker.scoring;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
import dev.reranker.tokenizer.StandardTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScorerInterfaceTest {

    @Test
    void test_scorer_interface_returns_list_of_scores() {
        Scorer scorer = new BM25Scorer(new StandardTokenizer());
        Query query = Query.of("quick fox");
        List<Document> docs = List.of(
                Document.of("d1", Map.of("body", "the quick brown fox")),
                Document.of("d2", Map.of("body", "lazy dog sleeps")));
        List<Score> scores = scorer.score(query, docs);
        assertNotNull(scores);
        assertEquals(2, scores.size());
    }
}
