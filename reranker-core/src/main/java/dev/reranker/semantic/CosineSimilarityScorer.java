package dev.reranker.semantic;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
import dev.reranker.scoring.Scorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A lightweight, dependency-free semantic-ish scorer based on cosine similarity
 * over character n-gram frequency vectors.
 *
 * <p>Produces bounded scores in {@code [0.0, 1.0]}, deliberately a different scale
 * from BM25's unbounded scores, making it a good second signal for hybrid rank
 * fusion. It catches morphological matches ("running" ~ "runner") that token-level
 * matching misses, without needing embeddings.
 *
 * <p>Complexity is O(D × L) where D = documents and L = average document length.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class CosineSimilarityScorer implements Scorer {

    /** The name reported on every {@link Score} produced by this scorer. */
    public static final String SCORER_NAME = "cosine-ngram";

    private final NGramConfig config;

    /**
     * Creates a scorer using the default trigram (n=3) configuration.
     */
    public CosineSimilarityScorer() {
        this(NGramConfig.defaults());
    }

    /**
     * Creates a scorer using a custom n-gram configuration.
     *
     * @param config the n-gram configuration
     */
    public CosineSimilarityScorer(NGramConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Scores each document by character n-gram cosine similarity to the query.
     *
     * @param query     the search query
     * @param documents the documents to score
     * @return one {@link Score} in {@code [0,1]} per document, in document order
     */
    @Override
    public List<Score> score(Query query, List<Document> documents) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(documents, "documents must not be null");

        Map<String, Integer> queryVector = ngramVector(query.text());
        double queryNorm = norm(queryVector);

        List<Score> scores = new ArrayList<>(documents.size());
        for (Document doc : documents) {
            Map<String, Integer> docVector = ngramVector(concatenateFields(doc));
            double cos = cosine(queryVector, queryNorm, docVector);
            scores.add(new Score(SCORER_NAME, clampUnit(cos)));
        }
        return List.copyOf(scores);
    }

    /**
     * Concatenates a document's field values, separated by spaces, in a stable
     * (field-name-sorted) order so the result is deterministic.
     */
    private static String concatenateFields(Document doc) {
        Map<String, String> sorted = new TreeMap<>(doc.fields());
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String value : sorted.values()) {
            if (!first) {
                sb.append(' ');
            }
            sb.append(value);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Builds the character n-gram frequency vector of the lowercased text. If the
     * text is shorter than n (but non-empty), the whole string is the single
     * n-gram. Empty text yields an empty vector.
     */
    Map<String, Integer> ngramVector(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        Map<String, Integer> vector = new HashMap<>();
        int len = lower.length();
        if (len == 0) {
            return vector;
        }
        int n = config.n();
        if (len < n) {
            vector.merge(lower, 1, Integer::sum);
            return vector;
        }
        for (int i = 0; i + n <= len; i++) {
            String gram = lower.substring(i, i + n);
            vector.merge(gram, 1, Integer::sum);
        }
        return vector;
    }

    /**
     * Cosine similarity between the query vector (with its precomputed norm) and a
     * document vector. Returns 0.0 if either vector is empty.
     */
    private static double cosine(Map<String, Integer> queryVector,
                                 double queryNorm,
                                 Map<String, Integer> docVector) {
        if (queryNorm == 0.0 || docVector.isEmpty()) {
            return 0.0;
        }
        double docNorm = norm(docVector);
        if (docNorm == 0.0) {
            return 0.0;
        }
        // Iterate over the smaller vector for the dot product.
        Map<String, Integer> small = queryVector.size() <= docVector.size()
                ? queryVector : docVector;
        Map<String, Integer> large = small == queryVector ? docVector : queryVector;
        double dot = 0.0;
        for (Map.Entry<String, Integer> e : small.entrySet()) {
            Integer other = large.get(e.getKey());
            if (other != null) {
                dot += (double) e.getValue() * other;
            }
        }
        return dot / (queryNorm * docNorm);
    }

    private static double norm(Map<String, Integer> vector) {
        double sumSquares = 0.0;
        for (int count : vector.values()) {
            sumSquares += (double) count * count;
        }
        return Math.sqrt(sumSquares);
    }

    private static double clampUnit(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        return Math.min(value, 1.0);
    }
}
