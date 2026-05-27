package dev.reranker.model;

/**
 * A parsed token from a document field, with position and source field info.
 *
 * @param value    the token text (lowercase)
 * @param position zero-based position in the source text
 * @param field    the document field this token came from
 */
public record Token(String value, int position, String field) {}
