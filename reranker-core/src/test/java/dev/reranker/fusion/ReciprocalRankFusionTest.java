package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReciprocalRankFusionTest {

    private static final Query Q = Query.of("query");

    private static List<Document> docs(String... ids) {
        List<Document> list = new ArrayList<>();
        for (String id : ids) {
            list.add(Document.of(id, Map.of("body", id)));
        }
        return list;
    }

    private static List<Score> scores(double... values) {
        List<Score> list = new ArrayList<>();
        for (double v : values) {
            list.add(new Score("s", v));
        }
        return list;
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
    void test_rrf_single_scorer_preserves_order() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("a", "b", "c");
        // Scorer ranks b > a > c.
        List<List<Score>> sbs = List.of(scores(2.0, 3.0, 1.0));
        double[] fused = rrf.fuse(Q, docs, sbs);
        // b should have the highest fused score, then a, then c.
        assertTrue(fused[indexOf(docs, "b")] > fused[indexOf(docs, "a")]);
        assertTrue(fused[indexOf(docs, "a")] > fused[indexOf(docs, "c")]);
    }

    @Test
    void test_rrf_two_scorers_basic_fusion() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("a", "b");
        // Scorer 1: a > b. Scorer 2: a > b. a wins both.
        List<List<Score>> sbs = List.of(scores(2.0, 1.0), scores(5.0, 1.0));
        double[] fused = rrf.fuse(Q, docs, sbs);
        assertTrue(fused[indexOf(docs, "a")] > fused[indexOf(docs, "b")]);
    }

    @Test
    void test_rrf_worked_example_consensus_wins() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("A", "B", "C", "D", "E");
        // Scorer 1 ranks: A=1,B=2,C=3,D=4,E=5
        List<Score> s1 = scores(5.0, 4.0, 3.0, 2.0, 1.0);
        // Scorer 2 ranks: C=1,A=2,E=3,B=4,D=5
        // order A,B,C,D,E -> A=4, B=2, C=5, D=1, E=3
        List<Score> s2 = scores(4.0, 2.0, 5.0, 1.0, 3.0);
        List<List<Score>> sbs = List.of(s1, s2);
        double[] fused = rrf.fuse(Q, docs, sbs);

        double a = fused[indexOf(docs, "A")];
        double b = fused[indexOf(docs, "B")];
        double c = fused[indexOf(docs, "C")];

        // Expected RRF values (k=60).
        assertEquals(1.0 / 61 + 1.0 / 62, a, 1e-9);
        assertEquals(1.0 / 63 + 1.0 / 61, c, 1e-9);
        assertEquals(1.0 / 62 + 1.0 / 64, b, 1e-9);

        // Consensus property: A (good on both) beats C (rank 1 on only one scorer).
        assertTrue(a > c);
        assertTrue(c > b);
    }

    @Test
    void test_rrf_document_in_one_list_ranks_lower() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        // Five docs so a "rank 1 in one list only" doc can be ranked far down in the
        // other list, demonstrating that cross-scorer consensus wins.
        List<Document> docs = docs("top1", "top2", "consensus", "filler1", "filler2");
        // Scorer 1 ranks: top1=1, consensus=2, then fillers, top2 last (=5).
        // order top1,top2,consensus,filler1,filler2 -> 9,1,7,5,3
        List<Score> s1 = scores(9.0, 1.0, 7.0, 5.0, 3.0);
        // Scorer 2 ranks: top2=1, consensus=2, then fillers, top1 last (=5).
        List<Score> s2 = scores(1.0, 9.0, 7.0, 5.0, 3.0);
        double[] fused = rrf.fuse(Q, docs, List.of(s1, s2));
        // consensus (ranked 2,2) beats top1 (1,5) and top2 (5,1).
        assertTrue(fused[indexOf(docs, "consensus")] > fused[indexOf(docs, "top1")]);
        assertTrue(fused[indexOf(docs, "consensus")] > fused[indexOf(docs, "top2")]);
    }

    @Test
    void test_rrf_default_k_is_60() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("a");
        double[] fused = rrf.fuse(Q, docs, List.of(scores(1.0)));
        // Single doc, single scorer, rank 1 → 1/(60+1).
        assertEquals(1.0 / 61, fused[0], 1e-12);
    }

    @Test
    void test_rrf_custom_k_changes_weighting() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion(new RrfConfig(1));
        List<Document> docs = docs("a");
        double[] fused = rrf.fuse(Q, docs, List.of(scores(1.0)));
        // k=1, rank 1 → 1/(1+1) = 0.5.
        assertEquals(0.5, fused[0], 1e-12);
    }

    @Test
    void test_rrf_ties_broken_by_document_id() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("zebra", "alpha");
        // Both scored equally; tie broken by id ascending → alpha rank 1, zebra rank 2.
        double[] fused = rrf.fuse(Q, docs, List.of(scores(5.0, 5.0)));
        // alpha gets the better (rank 1) reciprocal.
        assertTrue(fused[indexOf(docs, "alpha")] > fused[indexOf(docs, "zebra")]);
        assertEquals(1.0 / 61, fused[indexOf(docs, "alpha")], 1e-12);
        assertEquals(1.0 / 62, fused[indexOf(docs, "zebra")], 1e-12);
    }

    @Test
    void test_rrf_empty_documents_returns_empty() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        double[] fused = rrf.fuse(Q, List.of(), List.of());
        assertEquals(0, fused.length);
    }

    @Test
    void test_rrf_scores_are_descending_friendly() {
        // Every RRF score must be strictly positive.
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("a", "b", "c");
        double[] fused = rrf.fuse(Q, docs, List.of(scores(3.0, 2.0, 1.0)));
        for (double v : fused) {
            assertTrue(v > 0.0);
        }
    }

    @Test
    void test_rrf_is_deterministic() {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("a", "b", "c");
        List<List<Score>> sbs = List.of(scores(3.0, 2.0, 1.0), scores(1.0, 3.0, 2.0));
        double[] first = rrf.fuse(Q, docs, sbs);
        double[] second = rrf.fuse(Q, docs, sbs);
        assertEquals(first.length, second.length);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], second[i], 1e-12);
        }
    }

    @Test
    void test_rrf_is_thread_safe() throws Exception {
        ReciprocalRankFusion rrf = new ReciprocalRankFusion();
        List<Document> docs = docs("a", "b", "c", "d");
        List<List<Score>> sbs = List.of(
                scores(4.0, 3.0, 2.0, 1.0),
                scores(1.0, 2.0, 3.0, 4.0));
        double[] expected = rrf.fuse(Q, docs, sbs);

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<double[]>> futures = IntStream.range(0, 200)
                    .mapToObj(i -> pool.submit(() -> rrf.fuse(Q, docs, sbs)))
                    .toList();
            for (Future<double[]> f : futures) {
                assertArrayEqualsExact(expected, f.get());
            }
        } finally {
            pool.shutdownNow();
        }
        assertFalse(docs.isEmpty());
    }

    private static void assertArrayEqualsExact(double[] expected, double[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], 0.0);
        }
    }
}
