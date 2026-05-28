package dev.reranker.tokenizer;

import dev.reranker.model.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenizerInterfaceTest {

    @Test
    void test_tokenizer_interface_returns_list_of_tokens() {
        Tokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("hello world", "body");
        assertNotNull(tokens);
        assertTrue(tokens.stream().allMatch(t -> t instanceof Token));
    }
}
