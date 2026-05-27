package dev.reranker.tokenizer;

import dev.reranker.model.Token;

import java.util.List;

/**
 * Splits text into tokens for scoring.
 */
public interface Tokenizer {

    /**
     * @param text      the text to tokenize
     * @param fieldName the document field this text came from
     * @return list of tokens with positions and field info
     */
    List<Token> tokenize(String text, String fieldName);
}
