package dev.reranker.fusion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RrfConfigTest {

    @Test
    void test_default_rank_constant_is_60() {
        assertEquals(60, RrfConfig.defaults().rankConstant());
    }

    @Test
    void test_custom_rank_constant() {
        assertEquals(10, new RrfConfig(10).rankConstant());
    }

    @Test
    void test_rank_constant_below_one_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> new RrfConfig(0));
    }
}
