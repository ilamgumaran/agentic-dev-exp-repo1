package dev.reranker.pipeline;

import dev.reranker.engine.Aggregator;
import dev.reranker.engine.WeightedAggregator;
import dev.reranker.model.Document;
import dev.reranker.model.Query;
import dev.reranker.model.RankedResult;
import dev.reranker.model.Score;
import dev.reranker.scoring.Scorer;
import dev.reranker.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * A composable re-ranking pipeline that wires together a tokenizer, one or more
 * scorers and either an {@link Aggregator} or a
 * {@link dev.reranker.fusion.FusionStrategy} to produce ranked results.
 *
 * <p>Instances are created via the {@link #builder()} and are immutable and
 * thread-safe after {@link Builder#build()}.
 */
public final class ReRankPipeline {

    private final Tokenizer tokenizer;
    private final List<Scorer> scorers;
    private final Aggregator aggregator;
    private final dev.reranker.fusion.FusionStrategy fusion;
    private final OptionalInt topK;

    private ReRankPipeline(Builder builder) {
        this.tokenizer = builder.tokenizer;
        this.scorers = List.copyOf(builder.scorers);
        this.aggregator = builder.aggregator;
        this.fusion = builder.fusion;
        this.topK = builder.topK;
    }

    /**
     * @return a new builder for assembling a pipeline
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Ranks the documents against the query.
     *
     * @param query     the search query
     * @param documents the documents to rank
     * @return ranked results sorted by score descending (ties broken by document
     *         id ascending), truncated to topK if configured
     */
    public List<RankedResult> rank(Query query, List<Document> documents) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(documents, "documents must not be null");

        if (documents.isEmpty()) {
            return List.of();
        }

        // Run every scorer once over the full document set.
        List<List<Score>> scoresByScorer = new ArrayList<>(scorers.size());
        for (Scorer scorer : scorers) {
            scoresByScorer.add(scorer.score(query, documents));
        }

        double[] finalScores = (fusion != null)
                ? fusion.fuse(query, documents, scoresByScorer)
                : aggregateScores(documents.size(), scoresByScorer);

        List<RankedResult> results = new ArrayList<>(documents.size());
        for (int i = 0; i < documents.size(); i++) {
            List<Score> component = new ArrayList<>(scoresByScorer.size());
            for (List<Score> scorerScores : scoresByScorer) {
                component.add(scorerScores.get(i));
            }
            results.add(new RankedResult(documents.get(i), finalScores[i], component));
        }

        results.sort(null); // RankedResult natural order: score desc, id asc.

        if (topK.isPresent()) {
            int k = Math.min(topK.getAsInt(), results.size());
            return List.copyOf(results.subList(0, k));
        }
        return List.copyOf(results);
    }

    private double[] aggregateScores(int docCount, List<List<Score>> scoresByScorer) {
        double[] finalScores = new double[docCount];
        for (int i = 0; i < docCount; i++) {
            List<Score> component = new ArrayList<>(scoresByScorer.size());
            for (List<Score> scorerScores : scoresByScorer) {
                component.add(scorerScores.get(i));
            }
            finalScores[i] = aggregator.aggregate(component);
        }
        return finalScores;
    }

    /**
     * Mutable builder for {@link ReRankPipeline}. Validates configuration at
     * {@link #build()} time, not at rank time.
     */
    public static final class Builder {

        private Tokenizer tokenizer;
        private final List<Scorer> scorers = new ArrayList<>();
        private Aggregator aggregator;
        private dev.reranker.fusion.FusionStrategy fusion;
        private OptionalInt topK = OptionalInt.empty();

        private Builder() {
        }

        /**
         * @param tokenizer the tokenizer to use (required)
         * @return this builder
         */
        public Builder tokenizer(Tokenizer tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        /**
         * @param scorer a scorer to add (at least one is required)
         * @return this builder
         */
        public Builder scorer(Scorer scorer) {
            this.scorers.add(scorer);
            return this;
        }

        /**
         * @param aggregator the per-document aggregator (mutually exclusive with fusion)
         * @return this builder
         */
        public Builder aggregator(Aggregator aggregator) {
            this.aggregator = aggregator;
            return this;
        }

        /**
         * @param fusion the fusion strategy (mutually exclusive with aggregator)
         * @return this builder
         */
        public Builder fusion(dev.reranker.fusion.FusionStrategy fusion) {
            this.fusion = fusion;
            return this;
        }

        /**
         * @param topK the maximum number of results to return (must be {@code >= 0})
         * @return this builder
         */
        public Builder topK(int topK) {
            if (topK < 0) {
                throw new IllegalArgumentException("topK must be non-negative, got: " + topK);
            }
            this.topK = OptionalInt.of(topK);
            return this;
        }

        /**
         * Validates the configuration and builds an immutable pipeline.
         *
         * @return the assembled pipeline
         * @throws IllegalStateException    if required components are missing, or if
         *                                  both/neither of aggregator and fusion are set
         * @throws IllegalArgumentException if a {@link WeightedAggregator}'s weight
         *                                  count does not match the scorer count
         */
        public ReRankPipeline build() {
            if (tokenizer == null) {
                throw new IllegalStateException("tokenizer is required");
            }
            if (scorers.isEmpty()) {
                throw new IllegalStateException("at least one scorer is required");
            }
            boolean hasAggregator = aggregator != null;
            boolean hasFusion = fusion != null;
            if (hasAggregator && hasFusion) {
                throw new IllegalStateException(
                        "set either an aggregator or a fusion strategy, not both");
            }
            if (!hasAggregator && !hasFusion) {
                throw new IllegalStateException(
                        "either an aggregator or a fusion strategy is required");
            }
            if (aggregator instanceof WeightedAggregator weighted
                    && weighted.weightCount() != scorers.size()) {
                throw new IllegalArgumentException(
                        "WeightedAggregator weight count (" + weighted.weightCount()
                                + ") must match scorer count (" + scorers.size() + ")");
            }
            return new ReRankPipeline(this);
        }
    }
}
