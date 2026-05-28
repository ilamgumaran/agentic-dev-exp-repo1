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

class ZScoreNormalizedFusionTest {

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
    void test_zscore_normalizes_by_mean_and_stddev() {
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b", "c");
        // values 2,4,6 -> mean 4, population stddev = sqrt((4+0+4)/3) = sqrt(8/3).
        double[] fused = fusion.fuse(Q, docs, List.of(scores(2.0, 4.0, 6.0)));
        double mean = 4.0;
        double sd = Math.sqrt(((2 - mean) * (2 - mean) + 0 + (6 - mean) * (6 - mean)) / 3.0);
        assertEquals((2 - mean) / sd, fused[indexOf(docs, "a")], 1e-9);
        assertEquals(0.0, fused[indexOf(docs, "b")], 1e-9);
        assertEquals((6 - mean) / sd, fused[indexOf(docs, "c")], 1e-9);
    }

    @Test
    void test_zscore_single_scorer() {
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b");
        double[] fused = fusion.fuse(Q, docs, List.of(scores(1.0, 5.0)));
        assertTrue(fused[indexOf(docs, "b")] > fused[indexOf(docs, "a")]);
    }

    @Test
    void test_zscore_two_scorers_weighted() {
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion(1.0, 1.0);
        List<Document> docs = docs("a", "b");
        // Each scorer symmetric around its mean; doc strong on both should win.
        double[] fused = fusion.fuse(Q, docs, List.of(scores(1.0, 3.0), scores(2.0, 8.0)));
        assertTrue(fused[indexOf(docs, "b")] > fused[indexOf(docs, "a")]);
    }

    @Test
    void test_zscore_zero_stddev_contributes_zero() {
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b", "c");
        double[] fused = fusion.fuse(Q, docs, List.of(scores(7.0, 7.0, 7.0)));
        for (double v : fused) {
            assertEquals(0.0, v, 1e-12);
        }
    }

    @Test
    void test_zscore_wrong_weight_count_throws() {
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion(1.0);
        List<Document> docs = docs("a", "b");
        assertThrows(IllegalArgumentException.class, () ->
                fusion.fuse(Q, docs, List.of(scores(1.0, 2.0), scores(2.0, 1.0))));
    }

    @Test
    void test_zscore_empty_documents_returns_empty() {
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion();
        double[] fused = fusion.fuse(Q, List.of(), List.of());
        assertEquals(0, fused.length);
    }

    @Test
    void test_zscore_is_outlier_robust() {
        // One scorer has a large outlier; z-score divides by a correspondingly
        // large stddev, dampening the outlier's pull relative to min-max.
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion(1.0, 1.0);
        List<Document> docs = docs("a", "b", "c");
        // Scorer 1: a has an extreme outlier; b and c are close.
        List<Score> s1 = scores(1000.0, 5.0, 4.0);
        // Scorer 2: b is clearly best.
        List<Score> s2 = scores(1.0, 10.0, 2.0);
        double[] fused = fusion.fuse(Q, docs, List.of(s1, s2));
        // b should still be competitive with a despite a's outlier on scorer 1.
        double a = fused[indexOf(docs, "a")];
        double b = fused[indexOf(docs, "b")];
        assertTrue(b > a, "z-score should dampen outlier so b (" + b + ") > a (" + a + ")");
    }

    @Test
    void test_zscore_is_thread_safe() throws Exception {
        ZScoreNormalizedFusion fusion = new ZScoreNormalizedFusion(1.0, 1.0);
        List<Document> docs = docs("a", "b", "c");
        List<List<Score>> sbs = List.of(scores(3.0, 6.0, 9.0), scores(1.0, 2.0, 3.0));
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
