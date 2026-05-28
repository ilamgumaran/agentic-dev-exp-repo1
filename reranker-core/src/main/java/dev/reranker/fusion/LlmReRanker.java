package dev.reranker.fusion;

import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.Score;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * A query-aware {@link FusionStrategy} that refines a base ranking with an LLM and
 * merges the two rankings via Reciprocal Rank Fusion. This "merge two results"
 * design gives the quality lift of LLM judgment while remaining robust to a single
 * bad LLM response: even a fully unparseable response degrades gracefully to the
 * base ranking.
 *
 * <p>The LLM is called at most once per {@link #fuse} invocation (all candidates
 * are batched into one prompt). Response parsing never throws.
 *
 * <p>This class holds only immutable state and is therefore thread-safe provided
 * the supplied {@link LlmClient} is thread-safe. Given a deterministic client, the
 * output is deterministic.
 */
public final class LlmReRanker implements FusionStrategy {

    private final LlmClient client;
    private final FusionStrategy base;
    private final LlmReRankConfig config;

    /**
     * Creates a re-ranker with the default config (top 10 candidates, merge k=60).
     *
     * @param client the user-supplied LLM gateway
     * @param base   the base fusion strategy producing the initial ranking
     */
    public LlmReRanker(LlmClient client, FusionStrategy base) {
        this(client, base, LlmReRankConfig.defaults());
    }

    /**
     * Creates a re-ranker with a custom config.
     *
     * @param client the user-supplied LLM gateway
     * @param base   the base fusion strategy producing the initial ranking
     * @param config the candidate count and merge rank constant
     */
    public LlmReRanker(LlmClient client, FusionStrategy base, LlmReRankConfig config) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.base = Objects.requireNonNull(base, "base must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Merges the base ranking with an LLM ranking of the top candidates.
     *
     * @param query          the search query, included verbatim in the LLM prompt
     * @param documents      the documents being ranked, in a stable order
     * @param scoresByScorer one list of scores per scorer, each in document order
     * @return the merged score per document, in document order
     */
    @Override
    public double[] fuse(Query query, List<Document> documents, List<List<Score>> scoresByScorer) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(documents, "documents must not be null");
        Objects.requireNonNull(scoresByScorer, "scoresByScorer must not be null");

        int n = documents.size();
        if (n == 0) {
            return new double[0];
        }

        // Step 1-2: base scores, then base ranking (descending, ties by id ascending).
        double[] baseScores = base.fuse(query, documents, scoresByScorer);
        int[] baseRank = new int[n];
        Integer[] order = baseOrder(documents, baseScores);
        for (int rank = 1; rank <= n; rank++) {
            baseRank[order[rank - 1]] = rank;
        }

        // Step 3: top candidateCount documents in base-rank order.
        int candidateCount = Math.min(config.candidateCount(), n);
        int[] candidateDocIndex = new int[candidateCount]; // candidate slot -> document index
        for (int slot = 0; slot < candidateCount; slot++) {
            candidateDocIndex[slot] = order[slot];
        }

        // Step 4-5: build prompt and call the LLM exactly once.
        String prompt = buildPrompt(query, documents, candidateDocIndex);
        String response = client.complete(prompt); // exceptions propagate by design

        // Step 6: parse the LLM ranking (1-based candidate slots).
        int[] llmRankByDoc = new int[n];
        // 0 means "no LLM rank for this document".
        List<Integer> parsed = parseCandidateOrder(response, candidateCount);
        for (int i = 0; i < parsed.size(); i++) {
            int candidateSlot = parsed.get(i); // 1-based
            int docIndex = candidateDocIndex[candidateSlot - 1];
            llmRankByDoc[docIndex] = i + 1; // LLM rank is 1-based
        }

        // Step 7: merge via RRF using mergeRankConstant.
        int k = config.mergeRankConstant();
        double[] fused = new double[n];
        for (int i = 0; i < n; i++) {
            double merged = 1.0 / (k + baseRank[i]);
            if (llmRankByDoc[i] > 0) {
                merged += 1.0 / (k + llmRankByDoc[i]);
            }
            fused[i] = merged;
        }
        return fused;
    }

    /**
     * Document indices ordered by base score descending, ties broken by document id
     * ascending (so the base ranking is deterministic).
     */
    private static Integer[] baseOrder(List<Document> documents, double[] baseScores) {
        Integer[] order = new Integer[documents.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        java.util.Arrays.sort(order, Comparator
                .comparingDouble((Integer i) -> baseScores[i]).reversed()
                .thenComparing(i -> documents.get(i).id()));
        return order;
    }

    /**
     * Builds the re-ranking prompt. Candidates are numbered {@code [1], [2], ...}
     * mapped internally to document indices, which is more parse-robust than echoing
     * raw document ids.
     */
    private static String buildPrompt(Query query, List<Document> documents, int[] candidateDocIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a search relevance expert. Given the query and the candidate ")
                .append("documents, rank the documents from MOST to LEAST relevant to the query.\n\n");
        sb.append("Query: \"").append(query.text()).append("\"\n\n");
        sb.append("Candidates:\n");
        for (int slot = 0; slot < candidateDocIndex.length; slot++) {
            Document doc = documents.get(candidateDocIndex[slot]);
            sb.append('[').append(slot + 1).append("] ").append(concatenateFields(doc)).append('\n');
        }
        sb.append("\nReturn ONLY the candidate numbers in ranked order, most relevant first, ")
                .append("comma-separated. Example: 3,1,2\n");
        return sb.toString();
    }

    private static String concatenateFields(Document doc) {
        return String.join(" ", new java.util.TreeMap<>(doc.fields()).values());
    }

    /**
     * Parses the LLM response into an ordered list of 1-based candidate slots. Reads
     * integers in order of appearance, ignores values outside {@code [1, count]} and
     * duplicates (first occurrence wins). Never throws on malformed input.
     */
    private static List<Integer> parseCandidateOrder(String response, int count) {
        LinkedHashSet<Integer> ordered = new LinkedHashSet<>();
        if (response == null) {
            return new ArrayList<>();
        }
        int i = 0;
        int len = response.length();
        while (i < len) {
            char c = response.charAt(i);
            if (c < '0' || c > '9') {
                i++;
                continue;
            }
            // Accumulate a run of digits, guarding against overflow by capping.
            long value = 0;
            boolean overflow = false;
            while (i < len && response.charAt(i) >= '0' && response.charAt(i) <= '9') {
                if (!overflow) {
                    value = value * 10 + (response.charAt(i) - '0');
                    if (value > Integer.MAX_VALUE) {
                        overflow = true;
                    }
                }
                i++;
            }
            if (!overflow && value >= 1 && value <= count) {
                ordered.add((int) value);
            }
        }
        return new ArrayList<>(ordered);
    }
}
