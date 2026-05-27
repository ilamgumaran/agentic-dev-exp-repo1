package dev.reranker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldWeightTest {

    @Test
    void test_of_creates_field_weight() {
        var fw = FieldWeight.of("title", 2.0);
        assertEquals("title", fw.fieldName());
        assertEquals(2.0, fw.weight(), 0.001);
    }

    @Test
    void test_of_with_zero_weight_is_valid() {
        var fw = FieldWeight.of("title", 0.0);
        assertEquals(0.0, fw.weight(), 0.001);
    }

    @Test
    void test_of_with_negative_weight_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> FieldWeight.of("title", -1.0));
    }
}
