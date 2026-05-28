package dev.reranker.tokenizer;

import dev.reranker.model.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Standard tokenizer: splits on whitespace and punctuation, lowercases tokens,
 * removes stopwords, and assigns sequential positions and a field name.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class StandardTokenizer implements Tokenizer {

    /** Default English stopwords filtered out during tokenization. */
    public static final Set<String> DEFAULT_STOPWORDS = Set.of(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will", "would",
            "could", "should", "may", "might", "shall", "can", "this", "that",
            "these", "those", "it", "its");

    private static final Pattern SPLIT = Pattern.compile("[\\s\\p{Punct}]+");

    private final Set<String> stopwords;

    /**
     * Creates a tokenizer using the default English stopwords.
     */
    public StandardTokenizer() {
        this(DEFAULT_STOPWORDS);
    }

    /**
     * Creates a tokenizer using a custom set of stopwords.
     *
     * @param stopwords the stopwords to filter out (compared in lowercase)
     */
    public StandardTokenizer(Set<String> stopwords) {
        Set<String> lowered = new java.util.HashSet<>();
        for (String s : stopwords) {
            lowered.add(s.toLowerCase(Locale.ROOT));
        }
        this.stopwords = Set.copyOf(lowered);
    }

    /**
     * @return a tokenizer that performs no stopword filtering
     */
    public static StandardTokenizer withNoStopwords() {
        return new StandardTokenizer(Set.of());
    }

    /**
     * Tokenizes the given text. Complexity is O(n) in the length of the text.
     *
     * @param text      the text to tokenize (must not be null)
     * @param fieldName the document field this text came from
     * @return immutable list of tokens with sequential positions starting at 0
     */
    @Override
    public List<Token> tokenize(String text, String fieldName) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        if (text.isBlank()) {
            return List.of();
        }
        List<Token> tokens = new ArrayList<>();
        int position = 0;
        for (String raw : SPLIT.split(text)) {
            if (raw.isBlank()) {
                continue;
            }
            String value = raw.toLowerCase(Locale.ROOT);
            if (stopwords.contains(value)) {
                continue;
            }
            tokens.add(new Token(value, position++, fieldName));
        }
        return List.copyOf(tokens);
    }
}
