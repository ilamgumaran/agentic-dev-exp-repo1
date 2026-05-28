package dev.reranker.fusion;

/**
 * A user-supplied gateway to a Large Language Model. Core provides no
 * implementation, which keeps {@code reranker-core} zero-dependency: the user
 * wires in OpenAI, Anthropic, a local model, or a fake for tests.
 */
@FunctionalInterface
public interface LlmClient {

    /**
     * @param prompt the fully-constructed re-ranking prompt
     * @return the raw LLM completion text
     */
    String complete(String prompt);
}
