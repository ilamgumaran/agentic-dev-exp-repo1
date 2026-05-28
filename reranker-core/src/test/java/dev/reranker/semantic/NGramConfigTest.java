package dev.reranker.semantic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NGramConfigTest {

    @Test
    void test_default_n_is_3() {
        assertEquals(3, NGramConfig.defaults().n());
    }

    @Test
    void test_custom_n() {
        assertEquals(2, new NGramConfig(2).n());
    }

    @Test
    void test_n_below_one_throws() {
        assertThrows(IllegalArgumentException.class, () -> new NGramConfig(0));
    }
}
