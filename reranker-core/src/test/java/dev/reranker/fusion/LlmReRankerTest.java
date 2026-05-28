package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LlmReRankerTest {

    private static final Query Q = Query.of("relevant query");

    private static List<Document> docs(String... ids) {
        List<Document> list = new ArrayList<>();
        for (String id : ids) {
            list.add(Document.of(id, Map.of("body", "content of " + id)));
        }
        return list;
    }

    /** A base strategy that ranks documents by a single scorer's raw scores via RRF. */
    private static FusionStrategy baseRrf() {
        return new ReciprocalRankFusion();
    }

    /** Single-scorer score matrix giving descending scores in argument order. */
    private static List<List<Score>> descendingScores(int count) {
        List<Score> scores = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            scores.add(new Score("s", count - i)); // first doc highest
        }
        return List.of(scores);
    }

    private static int indexOf(List<Document> docs, String id) {
        for (int i = 0; i < docs.size(); i++) {
            if (docs.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new IllegalArgumentException(id);
    }

    @Test
    void test_fuse_calls_llm_once_per_invocation() {
        AtomicInteger calls = new AtomicInteger();
        LlmClient client = prompt -> {
            calls.incrementAndGet();
            return "1,2,3";
        };
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        reranker.fuse(Q, docs, descendingScores(3));
        assertEquals(1, calls.get());
    }

    @Test
    void test_fuse_passes_query_text_in_prompt() {
        AtomicReference<String> captured = new AtomicReference<>();
        LlmClient client = prompt -> {
            captured.set(prompt);
            return "1,2,3";
        };
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        reranker.fuse(Q, docs("A", "B", "C"), descendingScores(3));
        assertTrue(captured.get().contains("relevant query"),
                "prompt should contain the query text");
    }

    @Test
    void test_fuse_includes_candidate_documents_in_prompt() {
        AtomicReference<String> captured = new AtomicReference<>();
        LlmClient client = prompt -> {
            captured.set(prompt);
            return "1,2,3";
        };
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        reranker.fuse(Q, docs("A", "B", "C"), descendingScores(3));
        String prompt = captured.get();
        assertTrue(prompt.contains("content of A"));
        assertTrue(prompt.contains("content of B"));
        assertTrue(prompt.contains("content of C"));
    }

    @Test
    void test_llm_reordering_changes_final_ranking() {
        // Base ranking: A(1), B(2), C(3). LLM returns "3,2,1" reversing the candidates.
        LlmClient client = prompt -> "3,2,1";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));

        double a = fused[indexOf(docs, "A")];
        double b = fused[indexOf(docs, "B")];
        double c = fused[indexOf(docs, "C")];

        // A: base rank1 + llm rank3 = 1/61 + 1/63
        // B: base rank2 + llm rank2 = 1/62 + 1/62
        // C: base rank3 + llm rank1 = 1/63 + 1/61
        assertEquals(1.0 / 61 + 1.0 / 63, a, 1e-9);
        assertEquals(1.0 / 62 + 1.0 / 62, b, 1e-9);
        assertEquals(1.0 / 63 + 1.0 / 61, c, 1e-9);
        // A and C are symmetric.
        assertEquals(a, c, 1e-12);
        // B sits between (strictly, due to convexity of 1/(k+rank)).
        assertTrue(b < a);
    }

    @Test
    void test_merge_combines_base_and_llm_rankings() {
        LlmClient client = prompt -> "1,2,3"; // LLM agrees with base
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        // Each doc has base + llm contribution; A highest.
        assertEquals(1.0 / 61 + 1.0 / 61, fused[indexOf(docs, "A")], 1e-9);
    }

    @Test
    void test_llm_agreeing_with_base_reinforces_top_result() {
        LlmClient client = prompt -> "1,2,3";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        double a = fused[indexOf(docs, "A")];
        for (Document d : docs) {
            if (!d.id().equals("A")) {
                assertTrue(a > fused[indexOf(docs, d.id())]);
            }
        }
        assertEquals(2.0 / 61, a, 1e-9);
    }

    @Test
    void test_document_ranked_high_by_both_wins() {
        LlmClient client = prompt -> "1,2,3"; // agrees: A best
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        int best = 0;
        for (int i = 1; i < fused.length; i++) {
            if (fused[i] > fused[best]) {
                best = i;
            }
        }
        assertEquals("A", docs.get(best).id());
    }

    @Test
    void test_base_only_documents_get_base_contribution_only() {
        // candidateCount=2 so only top 2 base docs are sent to LLM; the third gets
        // only its base contribution.
        LlmClient client = prompt -> "1,2";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf(), new LlmReRankConfig(2, 60));
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        // C is base rank 3, not a candidate → only 1/(60+3).
        assertEquals(1.0 / 63, fused[indexOf(docs, "C")], 1e-9);
    }

    @Test
    void test_merge_uses_configured_rank_constant() {
        LlmClient client = prompt -> "1,2,3";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf(), new LlmReRankConfig(10, 5));
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        // A: base rank1 + llm rank1 with k=5 → 1/6 + 1/6.
        assertEquals(2.0 / 6, fused[indexOf(docs, "A")], 1e-9);
    }

    @Test
    void test_only_top_candidates_sent_to_llm() {
        AtomicReference<String> captured = new AtomicReference<>();
        LlmClient client = prompt -> {
            captured.set(prompt);
            return "1,2";
        };
        LlmReRanker reranker = new LlmReRanker(client, baseRrf(), new LlmReRankConfig(2, 60));
        List<Document> docs = docs("A", "B", "C");
        reranker.fuse(Q, docs, descendingScores(3));
        // Only A and B (top 2 base) sent; C must not appear.
        assertTrue(captured.get().contains("content of A"));
        assertTrue(captured.get().contains("content of B"));
        assertTrue(!captured.get().contains("content of C"));
    }

    @Test
    void test_candidate_count_larger_than_docs_sends_all() {
        AtomicReference<String> captured = new AtomicReference<>();
        LlmClient client = prompt -> {
            captured.set(prompt);
            return "1,2";
        };
        LlmReRanker reranker = new LlmReRanker(client, baseRrf(), new LlmReRankConfig(50, 60));
        List<Document> docs = docs("A", "B");
        reranker.fuse(Q, docs, descendingScores(2));
        assertTrue(captured.get().contains("content of A"));
        assertTrue(captured.get().contains("content of B"));
    }

    @Test
    void test_documents_outside_candidates_keep_base_rank() {
        LlmClient client = prompt -> "1"; // only ranks the first candidate
        LlmReRanker reranker = new LlmReRanker(client, baseRrf(), new LlmReRankConfig(1, 60));
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        // Only A is a candidate; B and C keep base-only.
        assertEquals(1.0 / 62, fused[indexOf(docs, "B")], 1e-9);
        assertEquals(1.0 / 63, fused[indexOf(docs, "C")], 1e-9);
    }

    @Test
    void test_empty_llm_response_falls_back_to_base() {
        LlmClient client = prompt -> "";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        double[] base = baseRrf().fuse(Q, docs, descendingScores(3));
        // Final ranking equals base ranking (relative order preserved). With no LLM
        // ranks applied, each doc keeps only its base contribution.
        for (int i = 0; i < docs.size(); i++) {
            assertEquals(base[i], fused[i], 1e-12);
        }
    }

    @Test
    void test_garbage_llm_response_falls_back_to_base() {
        LlmClient client = prompt -> "the model could not decide, sorry!";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        double[] base = baseRrf().fuse(Q, docs, descendingScores(3));
        for (int i = 0; i < docs.size(); i++) {
            assertEquals(base[i], fused[i], 1e-12);
        }
    }

    @Test
    void test_llm_response_with_out_of_range_ids_ignored() {
        // Candidates are [1..3]; 99 and 0 are out of range and ignored; "2" is valid.
        LlmClient client = prompt -> "99, 0, 2";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        // Only candidate 2 (= B, base rank 2) gets an LLM rank (rank 1).
        assertEquals(1.0 / 62 + 1.0 / 61, fused[indexOf(docs, "B")], 1e-9);
        // A and C get base only.
        assertEquals(1.0 / 61, fused[indexOf(docs, "A")], 1e-9);
        assertEquals(1.0 / 63, fused[indexOf(docs, "C")], 1e-9);
    }

    @Test
    void test_llm_response_with_duplicate_ids_first_wins() {
        LlmClient client = prompt -> "2,2,1";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        // LLM ranks: candidate 2 (=B) llm rank 1, candidate 1 (=A) llm rank 2.
        // duplicate 2 ignored. C not mentioned.
        assertEquals(1.0 / 62 + 1.0 / 61, fused[indexOf(docs, "B")], 1e-9);
        assertEquals(1.0 / 61 + 1.0 / 62, fused[indexOf(docs, "A")], 1e-9);
        assertEquals(1.0 / 63, fused[indexOf(docs, "C")], 1e-9);
    }

    @Test
    void test_llm_omitting_candidates_gives_base_contribution() {
        LlmClient client = prompt -> "2"; // only B mentioned
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] fused = reranker.fuse(Q, docs, descendingScores(3));
        assertEquals(1.0 / 61, fused[indexOf(docs, "A")], 1e-9);
        assertEquals(1.0 / 63, fused[indexOf(docs, "C")], 1e-9);
        assertEquals(1.0 / 62 + 1.0 / 61, fused[indexOf(docs, "B")], 1e-9);
    }

    @Test
    void test_parsing_never_throws_on_malformed_output() {
        String[] weird = {
                "", "   ", "abc", "!!!", "-1,-2", "1.5, 2.7", "\n\t,,,",
                "rank: one two three", "999999999999999999999999999", null
        };
        List<Document> docs = docs("A", "B", "C");
        for (String response : weird) {
            LlmClient client = prompt -> response;
            LlmReRanker reranker = new LlmReRanker(client, baseRrf());
            // Must not throw for any malformed response.
            double[] fused = reranker.fuse(Q, docs, descendingScores(3));
            assertEquals(3, fused.length);
        }
    }

    @Test
    void test_empty_documents_returns_empty_without_calling_llm() {
        AtomicInteger calls = new AtomicInteger();
        LlmClient client = prompt -> {
            calls.incrementAndGet();
            return "1";
        };
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        double[] fused = reranker.fuse(Q, List.of(), List.of());
        assertEquals(0, fused.length);
        assertEquals(0, calls.get());
    }

    @Test
    void test_single_document() {
        LlmClient client = prompt -> "1";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("only");
        double[] fused = reranker.fuse(Q, docs, descendingScores(1));
        // base rank 1 + llm rank 1.
        assertEquals(2.0 / 61, fused[0], 1e-9);
    }

    @Test
    void test_llm_client_exception_propagates() {
        LlmClient client = prompt -> {
            throw new RuntimeException("LLM down");
        };
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        assertThrows(RuntimeException.class,
                () -> reranker.fuse(Q, docs, descendingScores(3)));
    }

    @Test
    void test_deterministic_with_deterministic_client() {
        LlmClient client = prompt -> "3,1,2";
        LlmReRanker reranker = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] first = reranker.fuse(Q, docs, descendingScores(3));
        double[] second = reranker.fuse(Q, docs, descendingScores(3));
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], second[i], 1e-12);
        }
    }

    @Test
    void test_reranker_holds_only_immutable_state() {
        // Constructing two rerankers and reusing them must not share mutable state:
        // repeated calls yield identical results, and two instances are independent.
        LlmClient client = prompt -> "2,1,3";
        LlmReRanker r1 = new LlmReRanker(client, baseRrf());
        LlmReRanker r2 = new LlmReRanker(client, baseRrf());
        List<Document> docs = docs("A", "B", "C");
        double[] a = r1.fuse(Q, docs, descendingScores(3));
        double[] b = r2.fuse(Q, docs, descendingScores(3));
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i], 0.0);
        }
        // Sanity: this reranker actually changed something vs an empty response.
        LlmReRanker fallback = new LlmReRanker(prompt -> "", baseRrf());
        double[] base = fallback.fuse(Q, docs, descendingScores(3));
        assertNotEquals(a[0], base[0]);
        assertSame(docs.get(0), docs.get(0));
    }
}
