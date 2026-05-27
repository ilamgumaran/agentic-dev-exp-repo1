package dev.reranker.model;

import java.util.Map;
import java.util.Objects;

/**
 * An immutable document with named text fields, to be ranked by relevance.
 *
 * @param id     unique document identifier
 * @param fields map of field name to field text content
 */
public record Document(String id, Map<String, String> fields) {

    public Document {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(fields, "fields must not be null");
        fields = Map.copyOf(fields);
    }

    /**
     * @param id     unique identifier
     * @param fields map of field name to text content
     * @return a new Document
     */
    public static Document of(String id, Map<String, String> fields) {
        return new Document(id, fields);
    }
}
