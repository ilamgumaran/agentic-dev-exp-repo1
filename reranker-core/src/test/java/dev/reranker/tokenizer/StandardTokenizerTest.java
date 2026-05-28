package dev.reranker.tokenizer;

import dev.reranker.model.Token;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardTokenizerTest {

    @Test
    void test_tokenize_simple_text() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("quick brown fox", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("quick", "brown", "fox"), values);
    }

    @Test
    void test_tokenize_converts_to_lowercase() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("Quick BROWN Fox", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("quick", "brown", "fox"), values);
    }

    @Test
    void test_tokenize_removes_punctuation() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("hello, world! foo-bar.", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("hello", "world", "foo", "bar"), values);
    }

    @Test
    void test_tokenize_removes_default_stopwords() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("the quick brown fox is on the mat", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("quick", "brown", "fox", "mat"), values);
    }

    @Test
    void test_tokenize_assigns_sequential_positions() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("the quick brown fox", "body");
        // After stopword removal: quick brown fox => positions 0,1,2
        List<Integer> positions = tokens.stream().map(Token::position).toList();
        assertEquals(List.of(0, 1, 2), positions);
    }

    @Test
    void test_tokenize_assigns_field_name() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("quick brown fox", "title");
        assertTrue(tokens.stream().allMatch(t -> t.field().equals("title")));
    }

    @Test
    void test_tokenize_with_null_text_throws_exception() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        assertThrows(IllegalArgumentException.class, () -> tokenizer.tokenize(null, "body"));
    }

    @Test
    void test_tokenize_with_empty_text_returns_empty() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        assertTrue(tokenizer.tokenize("", "body").isEmpty());
    }

    @Test
    void test_tokenize_with_blank_text_returns_empty() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        assertTrue(tokenizer.tokenize("   \t\n  ", "body").isEmpty());
    }

    @Test
    void test_tokenize_with_only_stopwords_returns_empty() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        assertTrue(tokenizer.tokenize("the a an and or but", "body").isEmpty());
    }

    @Test
    void test_tokenize_with_only_punctuation_returns_empty() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        assertTrue(tokenizer.tokenize("!@#$ %^&* ()-", "body").isEmpty());
    }

    @Test
    void test_tokenize_single_word() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("hello", "body");
        assertEquals(1, tokens.size());
        assertEquals("hello", tokens.get(0).value());
        assertEquals(0, tokens.get(0).position());
    }

    @Test
    void test_tokenize_multiple_spaces_between_words() {
        StandardTokenizer tokenizer = new StandardTokenizer();
        List<Token> tokens = tokenizer.tokenize("quick    brown\t\tfox", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("quick", "brown", "fox"), values);
    }

    @Test
    void test_tokenize_with_custom_stopwords() {
        StandardTokenizer tokenizer = new StandardTokenizer(Set.of("quick", "fox"));
        List<Token> tokens = tokenizer.tokenize("quick brown fox", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("brown"), values);
    }

    @Test
    void test_tokenize_with_no_stopwords() {
        StandardTokenizer tokenizer = StandardTokenizer.withNoStopwords();
        List<Token> tokens = tokenizer.tokenize("the quick brown fox", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("the", "quick", "brown", "fox"), values);
    }

    @Test
    void test_tokenize_unicode_text() {
        StandardTokenizer tokenizer = StandardTokenizer.withNoStopwords();
        List<Token> tokens = tokenizer.tokenize("CAFÉ Über NAÏVE", "body");
        List<String> values = tokens.stream().map(Token::value).toList();
        assertEquals(List.of("café", "über", "naïve"), values);
    }

    @Test
    void test_tokenize_is_thread_safe() throws Exception {
        StandardTokenizer tokenizer = new StandardTokenizer();
        String text = "the quick brown fox jumps over the lazy dog";
        List<String> expected = tokenizer.tokenize(text, "body").stream()
                .map(Token::value).toList();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<List<String>>> futures = IntStream.range(0, 200)
                    .mapToObj(i -> pool.submit(() ->
                            tokenizer.tokenize(text, "body").stream()
                                    .map(Token::value)
                                    .collect(Collectors.toList())))
                    .toList();
            for (Future<List<String>> f : futures) {
                assertEquals(expected, f.get());
            }
        } finally {
            pool.shutdownNow();
        }
        assertFalse(expected.isEmpty());
    }
}
