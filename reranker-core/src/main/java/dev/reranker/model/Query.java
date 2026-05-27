package dev.reranker.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * An immutable search query with pre-computed lowercase terms.
 *
 * @param text  the raw query string
 * @param terms the query split into lowercase terms
 */
public record Query(String text, List<String> terms) {

    public Query {
        Objects.requireNonNull(text, "text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        terms = List.copyOf(terms);
    }

    /**
     * @param text the raw query string
     * @return a new Query with terms split on whitespace and lowercased
     */
    public static Query of(String text) {
        Objects.requireNonNull(text, "text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        List<String> terms = Arrays.stream(text.strip().split("\\s+"))
                .map(t -> t.toLowerCase(Locale.ROOT))
                .filter(t -> !t.isBlank())
                .toList();
        return new Query(text, terms);
    }
}
