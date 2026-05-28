package dev.reranker.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BM25ConfigTest {

    @Test
    void test_default_config_values() {
        BM25Config config = BM25Config.defaults();
        assertEquals(1.2, config.k1(), 1e-12);
        assertEquals(0.75, config.b(), 1e-12);
    }

    @Test
    void test_custom_config_values() {
        BM25Config config = new BM25Config(2.0, 0.5);
        assertEquals(2.0, config.k1(), 1e-12);
        assertEquals(0.5, config.b(), 1e-12);
    }

    @Test
    void test_negative_k1_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> new BM25Config(-0.1, 0.75));
    }

    @Test
    void test_b_below_zero_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> new BM25Config(1.2, -0.01));
    }

    @Test
    void test_b_above_one_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> new BM25Config(1.2, 1.01));
    }
}
