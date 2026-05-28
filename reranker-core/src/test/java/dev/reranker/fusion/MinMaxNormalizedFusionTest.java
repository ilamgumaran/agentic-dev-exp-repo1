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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinMaxNormalizedFusionTest {

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
    void test_minmax_normalizes_to_zero_one_range() {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b", "c");
        double[] fused = fusion.fuse(Q, docs, List.of(scores(2.0, 10.0, 6.0)));
        // Single scorer, weight 1: values become normalized [0,1].
        assertEquals(0.0, fused[indexOf(docs, "a")], 1e-12); // min
        assertEquals(1.0, fused[indexOf(docs, "b")], 1e-12); // max
        assertEquals(0.5, fused[indexOf(docs, "c")], 1e-12); // midpoint
    }

    @Test
    void test_minmax_single_scorer() {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b");
        double[] fused = fusion.fuse(Q, docs, List.of(scores(1.0, 3.0)));
        assertTrue(fused[indexOf(docs, "b")] > fused[indexOf(docs, "a")]);
    }

    @Test
    void test_minmax_two_scorers_weighted() {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(2.0, 1.0);
        List<Document> docs = docs("a", "b");
        // Scorer1 normalized: a=0, b=1. Scorer2 normalized: a=1, b=0.
        double[] fused = fusion.fuse(Q, docs, List.of(scores(1.0, 2.0), scores(2.0, 1.0)));
        // a: 2*0 + 1*1 = 1; b: 2*1 + 1*0 = 2.
        assertEquals(1.0, fused[indexOf(docs, "a")], 1e-12);
        assertEquals(2.0, fused[indexOf(docs, "b")], 1e-12);
    }

    @Test
    void test_minmax_all_equal_scores_contribute_zero() {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b", "c");
        double[] fused = fusion.fuse(Q, docs, List.of(scores(5.0, 5.0, 5.0)));
        for (double v : fused) {
            assertEquals(0.0, v, 1e-12);
        }
    }

    @Test
    void test_minmax_single_document_contributes_zero() {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0);
        List<Document> docs = docs("a");
        double[] fused = fusion.fuse(Q, docs, List.of(scores(42.0)));
        assertEquals(0.0, fused[0], 1e-12);
    }

    @Test
    void test_minmax_negative_scores_normalized() {
        // Score values must be non-negative per the model, so use a synthetic
        // FusionStrategy input where the smallest is 0; min-max still shifts to [0,1].
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b", "c");
        double[] fused = fusion.fuse(Q, docs, List.of(scores(0.0, 4.0, 2.0)));
        assertEquals(0.0, fused[indexOf(docs, "a")], 1e-12);
        assertEquals(1.0, fused[indexOf(docs, "b")], 1e-12);
        assertEquals(0.5, fused[indexOf(docs, "c")], 1e-12);
    }

    @Test
    void test_minmax_wrong_weight_count_throws() {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b");
        assertThrows(IllegalArgumentException.class, () ->
                fusion.fuse(Q, docs, List.of(scores(1.0, 2.0), scores(2.0, 1.0))));
    }

    @Test
    void test_minmax_empty_documents_returns_empty() {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion();
        double[] fused = fusion.fuse(Q, List.of(), List.of());
        assertEquals(0, fused.length);
    }

    @Test
    void test_minmax_handles_incomparable_scales() {
        // The crux: BM25-scale [25,10,2] + cosine-scale [0.3,0.9,0.5] for A,B,C.
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0, 1.0);
        List<Document> docs = docs("A", "B", "C");
        List<Score> bm25 = scores(25.0, 10.0, 2.0);
        List<Score> cosine = scores(0.3, 0.9, 0.5);
        double[] fused = fusion.fuse(Q, docs, List.of(bm25, cosine));
        double a = fused[indexOf(docs, "A")];
        double b = fused[indexOf(docs, "B")];
        // B is strong on both relative scales and must beat the BM25-magnitude leader A.
        assertTrue(b > a, "expected B (" + b + ") > A (" + a + ")");
    }

    @Test
    void test_minmax_is_thread_safe() throws Exception {
        MinMaxNormalizedFusion fusion = new MinMaxNormalizedFusion(1.0, 1.0);
        List<Document> docs = docs("a", "b", "c");
        List<List<Score>> sbs = List.of(scores(25.0, 10.0, 2.0), scores(0.3, 0.9, 0.5));
        double[] expected = fusion.fuse(Q, docs, sbs);

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<double[]>> futures = IntStream.range(0, 200)
                    .mapToObj(i -> pool.submit(() -> fusion.fuse(Q, docs, sbs)))
                    .toList();
            for (Future<double[]> f : futures) {
                double[] actual = f.get();
                assertEquals(expected.length, actual.length);
                for (int i = 0; i < expected.length; i++) {
                    assertEquals(expected[i], actual[i], 0.0);
                }
            }
        } finally {
            pool.shutdownNow();
        }
        assertFalse(docs.isEmpty());
    }
}
