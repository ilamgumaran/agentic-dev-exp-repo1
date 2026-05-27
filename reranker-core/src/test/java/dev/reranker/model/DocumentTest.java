package dev.reranker.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    void test_of_creates_document_with_id_and_fields() {
        var doc = Document.of("doc1", Map.of("title", "Hello World"));
        assertEquals("doc1", doc.id());
        assertEquals("Hello World", doc.fields().get("title"));
    }

    @Test
    void test_of_makes_defensive_copy_of_fields() {
        var mutable = new HashMap<String, String>();
        mutable.put("title", "original");
        var doc = Document.of("doc1", mutable);
        mutable.put("title", "modified");
        assertEquals("original", doc.fields().get("title"));
    }

    @Test
    void test_of_with_null_id_throws_exception() {
        assertThrows(NullPointerException.class, () -> Document.of(null, Map.of()));
    }

    @Test
    void test_of_with_blank_id_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> Document.of("  ", Map.of()));
    }

    @Test
    void test_of_with_null_fields_throws_exception() {
        assertThrows(NullPointerException.class, () -> Document.of("doc1", null));
    }

    @Test
    void test_of_with_empty_fields_is_valid() {
        var doc = Document.of("doc1", Map.of());
        assertTrue(doc.fields().isEmpty());
    }

    @Test
    void test_fields_map_is_immutable() {
        var doc = Document.of("doc1", Map.of("title", "test"));
        assertThrows(UnsupportedOperationException.class, () -> doc.fields().put("new", "value"));
    }

    @Test
    void test_equals_and_hashcode_by_id_and_fields() {
        var doc1 = Document.of("doc1", Map.of("title", "Hello"));
        var doc2 = Document.of("doc1", Map.of("title", "Hello"));
        var doc3 = Document.of("doc2", Map.of("title", "Hello"));
        assertEquals(doc1, doc2);
        assertEquals(doc1.hashCode(), doc2.hashCode());
        assertNotEquals(doc1, doc3);
    }
}
