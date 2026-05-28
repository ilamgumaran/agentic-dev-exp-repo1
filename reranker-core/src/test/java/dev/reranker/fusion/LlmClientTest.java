package dev.reranker.fusion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LlmClientTest {

    @Test
    void test_llm_client_is_functional_interface() {
        // Must be implementable as a lambda (i.e. a single abstract method).
        LlmClient client = prompt -> "ranked: " + prompt.length();
        assertEquals("ranked: 5", client.complete("hello"));
    }
}
