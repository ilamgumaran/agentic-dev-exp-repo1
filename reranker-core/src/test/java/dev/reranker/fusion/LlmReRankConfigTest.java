package dev.reranker.fusion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LlmReRankConfigTest {

    @Test
    void test_default_candidate_count_is_10() {
        assertEquals(10, LlmReRankConfig.defaults().candidateCount());
    }

    @Test
    void test_default_merge_rank_constant_is_60() {
        assertEquals(60, LlmReRankConfig.defaults().mergeRankConstant());
    }

    @Test
    void test_custom_config_values() {
        LlmReRankConfig config = new LlmReRankConfig(5, 30);
        assertEquals(5, config.candidateCount());
        assertEquals(30, config.mergeRankConstant());
    }

    @Test
    void test_candidate_count_below_one_throws() {
        assertThrows(IllegalArgumentException.class, () -> new LlmReRankConfig(0, 60));
    }

    @Test
    void test_merge_rank_constant_below_one_throws() {
        assertThrows(IllegalArgumentException.class, () -> new LlmReRankConfig(10, 0));
    }
}
