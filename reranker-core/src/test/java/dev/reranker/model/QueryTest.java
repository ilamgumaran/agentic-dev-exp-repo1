package dev.reranker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    @Test
    void test_of_creates_query_with_text_and_terms() {
        var query = Query.of("blue shoes");
        assertEquals("blue shoes", query.text());
        assertEquals(2, query.terms().size());
    }

    @Test
    void test_of_splits_text_into_lowercase_terms() {
        var query = Query.of("Blue Running Shoes");
        assertEquals("blue", query.terms().get(0));
        assertEquals("running", query.terms().get(1));
        assertEquals("shoes", query.terms().get(2));
    }

    @Test
    void test_of_with_multiple_spaces_collapses_terms() {
        var query = Query.of("blue   shoes");
        assertEquals(2, query.terms().size());
        assertEquals("blue", query.terms().get(0));
        assertEquals("shoes", query.terms().get(1));
    }

    @Test
    void test_of_with_single_character_is_valid() {
        var query = Query.of("a");
        assertEquals(1, query.terms().size());
        assertEquals("a", query.terms().get(0));
    }

    @Test
    void test_of_with_null_text_throws_exception() {
        assertThrows(NullPointerException.class, () -> Query.of(null));
    }

    @Test
    void test_of_with_blank_text_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> Query.of("   "));
    }

    @Test
    void test_terms_are_lowercase() {
        var query = Query.of("HELLO WORLD");
        assertEquals("hello", query.terms().get(0));
        assertEquals("world", query.terms().get(1));
    }

    @Test
    void test_terms_list_is_immutable() {
        var query = Query.of("hello world");
        assertThrows(UnsupportedOperationException.class, () -> query.terms().add("new"));
    }
}
