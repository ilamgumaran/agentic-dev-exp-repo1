package dev.reranker.scoring;

import dev.reranker.model.Document;
import dev.reranker.model.FieldWeight;
import dev.reranker.model.Query;
import dev.reranker.model.Score;
import dev.reranker.model.Token;
import dev.reranker.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * BM25 (Best Matching 25) relevance scorer.
 *
 * <p>Scores each document field independently and combines the per-field BM25
 * contributions using the configured {@link FieldWeight}s. Corpus statistics
 * (document frequency, average document length) are recomputed per {@code score}
 * call over the documents passed in, so the scorer is stateless and thread-safe.
 *
 * <p>The score for a query Q over document D is:
 * <pre>
 * score(Q, D) = Σ_field weight(field) × Σ_qi IDF(qi)
 *                 × (tf(qi) × (k1 + 1)) / (tf(qi) + k1 × (1 - b + b × |D| / avgdl))
 * </pre>
 * where {@code IDF(qi) = ln((N - n(qi) + 0.5) / (n(qi) + 0.5) + 1)}.
 *
 * <p>Complexity is O(Q × D × T) where Q = query terms, D = documents and
 * T = average tokens per document.
 *
 * <p>This class is immutable and therefore thread-safe.
 */
public final class BM25Scorer implements Scorer {

    /** The name reported on every {@link Score} produced by this scorer. */
    public static final String SCORER_NAME = "bm25";

    private final BM25Config config;
    private final Tokenizer tokenizer;
    private final List<FieldWeight> fieldWeights;

    /**
     * Creates a scorer with default config (k1=1.2, b=0.75) and equal field weights.
     *
     * @param tokenizer the tokenizer used to process queries and documents
     */
    public BM25Scorer(Tokenizer tokenizer) {
        this(BM25Config.defaults(), tokenizer);
    }

    /**
     * Creates a scorer with a custom config and equal field weights.
     *
     * @param config    the BM25 parameters
     * @param tokenizer the tokenizer used to process queries and documents
     */
    public BM25Scorer(BM25Config config, Tokenizer tokenizer) {
        this(config, tokenizer, List.of());
    }

    /**
     * Creates a scorer with full control over config, tokenizer and field weights.
     *
     * @param config       the BM25 parameters
     * @param tokenizer    the tokenizer used to process queries and documents
     * @param fieldWeights per-field weights; empty means equal weight 1.0 for all fields
     */
    public BM25Scorer(BM25Config config, Tokenizer tokenizer, List<FieldWeight> fieldWeights) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.tokenizer = Objects.requireNonNull(tokenizer, "tokenizer must not be null");
        Objects.requireNonNull(fieldWeights, "fieldWeights must not be null");
        this.fieldWeights = List.copyOf(fieldWeights);
    }

    /**
     * Scores each document against the query.
     *
     * @param query     the search query
     * @param documents the documents to score
     * @return one {@link Score} per document, in the same order as the input
     */
    @Override
    public List<Score> score(Query query, List<Document> documents) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(documents, "documents must not be null");

        int docCount = documents.size();
        if (docCount == 0) {
            return List.of();
        }

        // Effective query terms after tokenization (lowercase, stopwords removed).
        List<String> queryTerms = tokenizeQueryTerms(query);

        double[] totals = new double[docCount];
        if (queryTerms.isEmpty()) {
            return toScores(totals);
        }

        // Determine which fields to score and their weights.
        Map<String, Double> weights = resolveFieldWeights(documents);

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            String field = entry.getKey();
            double weight = entry.getValue();
            if (weight == 0.0) {
                continue;
            }
            accumulateFieldScores(field, weight, queryTerms, documents, totals);
        }

        return toScores(totals);
    }

    /**
     * Tokenizes the raw query text into distinct-but-ordered term values. The
     * field name is irrelevant for the query itself, so a placeholder is used.
     */
    private List<String> tokenizeQueryTerms(Query query) {
        List<Token> tokens = tokenizer.tokenize(query.text(), "query");
        List<String> terms = new ArrayList<>(tokens.size());
        for (Token t : tokens) {
            terms.add(t.value());
        }
        return terms;
    }

    /**
     * Resolves the set of fields to score and the weight for each. If explicit
     * field weights were supplied they are used as-is; otherwise every field that
     * appears on any document is scored with weight 1.0.
     */
    private Map<String, Double> resolveFieldWeights(List<Document> documents) {
        Map<String, Double> weights = new HashMap<>();
        if (!fieldWeights.isEmpty()) {
            for (FieldWeight fw : fieldWeights) {
                weights.put(fw.fieldName(), fw.weight());
            }
            return weights;
        }
        Set<String> fields = new LinkedHashSet<>();
        for (Document doc : documents) {
            fields.addAll(doc.fields().keySet());
        }
        for (String field : fields) {
            weights.put(field, 1.0);
        }
        return weights;
    }

    /**
     * Computes the BM25 contribution of a single field across all documents and
     * adds {@code weight ×} that contribution into {@code totals}.
     */
    private void accumulateFieldScores(String field,
                                       double weight,
                                       List<String> queryTerms,
                                       List<Document> documents,
                                       double[] totals) {
        int docCount = documents.size();

        // Per-document term-frequency maps for this field, and field lengths.
        List<Map<String, Integer>> termFreqs = new ArrayList<>(docCount);
        int[] lengths = new int[docCount];
        long totalLength = 0;
        for (int i = 0; i < docCount; i++) {
            String text = documents.get(i).fields().getOrDefault(field, "");
            Map<String, Integer> tf = termFrequencies(text, field);
            termFreqs.add(tf);
            int len = 0;
            for (int c : tf.values()) {
                len += c;
            }
            lengths[i] = len;
            totalLength += len;
        }

        double avgdl = (double) totalLength / docCount;
        // Guard against avgdl == 0 (all fields empty) to avoid division by zero.
        boolean hasContent = totalLength > 0;

        // Document frequency per query term within this field.
        Map<String, Integer> docFreq = new HashMap<>();
        for (String term : queryTerms) {
            if (docFreq.containsKey(term)) {
                continue;
            }
            int n = 0;
            for (Map<String, Integer> tf : termFreqs) {
                if (tf.containsKey(term)) {
                    n++;
                }
            }
            docFreq.put(term, n);
        }

        double k1 = config.k1();
        double b = config.b();

        for (int i = 0; i < docCount; i++) {
            Map<String, Integer> tf = termFreqs.get(i);
            if (tf.isEmpty()) {
                continue;
            }
            double lengthNorm = hasContent
                    ? (1.0 - b + b * (lengths[i] / avgdl))
                    : 1.0;
            double fieldScore = 0.0;
            for (String term : queryTerms) {
                Integer freq = tf.get(term);
                if (freq == null) {
                    continue;
                }
                int n = docFreq.get(term);
                double idf = idf(docCount, n);
                double termFreq = freq;
                double denom = termFreq + k1 * lengthNorm;
                double contribution = idf * (termFreq * (k1 + 1.0)) / denom;
                fieldScore += contribution;
            }
            totals[i] += weight * fieldScore;
        }
    }

    /**
     * IDF(qi) = ln((N - n + 0.5) / (n + 0.5) + 1). This formulation is always
     * non-negative, so per-term contributions never go below zero.
     */
    private static double idf(int n, int docFreq) {
        return Math.log((n - docFreq + 0.5) / (docFreq + 0.5) + 1.0);
    }

    private Map<String, Integer> termFrequencies(String text, String field) {
        Map<String, Integer> tf = new HashMap<>();
        for (Token token : tokenizer.tokenize(text, field)) {
            tf.merge(token.value(), 1, Integer::sum);
        }
        return tf;
    }

    private static List<Score> toScores(double[] totals) {
        List<Score> scores = new ArrayList<>(totals.length);
        for (double total : totals) {
            // Clamp tiny negative rounding noise to zero; IDF formulation keeps
            // genuine contributions non-negative.
            double value = total < 0.0 ? 0.0 : total;
            scores.add(new Score(SCORER_NAME, value));
        }
        return List.copyOf(scores);
    }
}
