# Feature: Rank Fusion (RRF and Normalized Score Fusion)

## Status: READY

## Summary

Add a `FusionStrategy` abstraction that combines the result lists of multiple scorers using rank-based or normalized score-based fusion. Implement Reciprocal Rank Fusion (RRF), Min-Max Normalized Linear fusion, and Z-Score Normalized fusion. Extend the pipeline to support fusion strategies alongside the existing per-document aggregators.

## Motivation

The existing aggregators (`WeightedAggregator`, `MaxAggregator`, `AverageAggregator`) operate per-document and combine raw scores directly. This fails when scorers produce scores on incomparable scales — the critical real-world case of combining BM25 (unbounded) with cosine similarity (bounded [-1,1]). Adding raw scores lets the larger-magnitude signal dominate by scale rather than relevance (the "score normalization problem").

Industry-standard systems (Elasticsearch, OpenSearch, Weaviate, Qdrant) solve this with rank fusion (RRF) and normalized score fusion. RRF is the robust zero-tuning default; normalized linear fusion preserves magnitude when scores are reliable.

See `docs/human-learning/RANK_FUSION_AND_HYBRID_SEARCH.md` for the full research and design rationale.

## Behavior

### Interface: FusionStrategy

The key architectural difference from `Aggregator`: a fusion strategy sees the **full score matrix** (all documents across all scorers), not one document's scores. This is required because RRF needs each document's *rank* within each scorer's result list — a global property of the result set.

```java
package dev.reranker.fusion;

public interface FusionStrategy {
    /**
     * @param documents     the documents being ranked, in a stable order
     * @param scoresByScorer one list of scores per scorer; each inner list is
     *                       in the same order as documents (scoresByScorer.get(i).get(j)
     *                       is scorer i's score for documents.get(j))
     * @return final fused score per document, in the same order as documents
     */
    double[] fuse(List<Document> documents, List<List<Score>> scoresByScorer);
}
```

- `scoresByScorer.size()` equals the number of scorers
- Each inner list has the same size as `documents`
- Returns a `double[]` of length `documents.size()`, final score per document in document order

### ReciprocalRankFusion

Implements RRF.

**Configuration**: `RrfConfig(int rankConstant)` — a record
- `rankConstant` (k): default 60, must be >= 1

**Algorithm**:
1. For each scorer, rank the documents by that scorer's score (descending). Rank is 1-based: highest score = rank 1. Ties in score get the same rank handling: sort by score descending, then by document id ascending for determinism; assign sequential ranks 1, 2, 3... (no rank sharing — each document gets a distinct rank).
2. For each document, compute `Σ 1/(k + rank_i)` summed across all scorers.
3. A document is present in every scorer's list (all documents are scored), so every document gets a contribution from every scorer. (Unlike Elasticsearch where retrievers return different candidate sets, our scorers all score the same document set.)
4. Return the RRF score per document.

**Constructors**:
- `ReciprocalRankFusion()` — default k=60
- `ReciprocalRankFusion(RrfConfig config)` — custom k

### MinMaxNormalizedFusion

Implements normalized linear (convex) combination.

**Configuration**: weights as varargs doubles, one per scorer.

**Algorithm**:
1. For each scorer, find min and max score across all documents.
2. Normalize each score: `norm = (score - min) / (max - min)`. If max == min (all scores equal), normalized value is 0.0 for all (no signal).
3. For each document, compute `Σ weight_i × norm_i(d)`.
4. Return the weighted sum per document.

**Constructors**:
- `MinMaxNormalizedFusion(double... weights)` — weights must match scorer count at fuse() time

### ZScoreNormalizedFusion

Implements z-score normalized combination (more outlier-robust).

**Configuration**: weights as varargs doubles, one per scorer.

**Algorithm**:
1. For each scorer, compute mean μ and standard deviation σ across all documents.
2. Normalize each score: `norm = (score - μ) / σ`. If σ == 0 (all scores equal), normalized value is 0.0 for all.
3. For each document, compute `Σ weight_i × norm_i(d)`.
4. Return the weighted sum per document.

**Constructors**:
- `ZScoreNormalizedFusion(double... weights)` — weights must match scorer count at fuse() time

### Pipeline Integration

Extend `ReRankPipeline.Builder` to accept a `FusionStrategy` as an alternative to an `Aggregator`:

```java
var pipeline = ReRankPipeline.builder()
    .tokenizer(new StandardTokenizer())
    .scorer(new BM25Scorer(tokenizer))
    .scorer(new CosineSimilarityScorer())
    .fusion(new ReciprocalRankFusion())   // RRF instead of aggregator
    .topK(10)
    .build();
```

- A pipeline must have EITHER an aggregator OR a fusion strategy, not both, not neither
- `build()` throws IllegalStateException if both or neither is set
- When fusion is used, the execution flow changes:
  1. Run all scorers → collect `List<List<Score>>`
  2. Pass to `FusionStrategy.fuse()` → get `double[]` final scores
  3. Build RankedResult per document with fused score and component scores
  4. Sort, apply topK, return

## Edge Cases

### RRF
- Single scorer: RRF still works (each doc gets `1/(k+rank)` from one scorer); result order matches that scorer's order
- All documents tied in a scorer's scores: break ties by document id ascending for deterministic ranks
- k = 1 (minimum): top ranks dominate strongly
- Empty documents list: return empty `double[]`
- rankConstant < 1: throw IllegalArgumentException in RrfConfig

### MinMaxNormalizedFusion
- All scores equal for a scorer (max == min): that scorer contributes 0 for all documents
- Single document: normalized value is 0.0 (min == max == that score); RRF would give it rank 1
- Negative input scores: min-max still works (shifts to [0,1])
- Wrong weight count: throw IllegalArgumentException at fuse() time
- Empty documents: return empty `double[]`

### ZScoreNormalizedFusion
- σ == 0 (all scores equal): that scorer contributes 0 for all documents
- Single document: σ == 0, contributes 0
- Wrong weight count: throw IllegalArgumentException at fuse() time
- Empty documents: return empty `double[]`

### Pipeline
- Both aggregator and fusion set: IllegalStateException at build()
- Neither aggregator nor fusion set: IllegalStateException at build()

## Constraints

- No external dependencies
- Thread-safe (immutable configuration, no shared mutable state)
- RRF: O(S × D log D) where S = scorers, D = documents (sorting per scorer)
- Normalized fusion: O(S × D) (single pass for stats, single pass for normalization)
- Package: `dev.reranker.fusion`
- Must not break the existing `Aggregator` path — both coexist

## Non-Goals

- Learning-to-rank / cross-encoder re-ranking (out of scope for zero-dependency core)
- DBSF (distribution-based score fusion) — may be added later as a separate spec
- CombMNZ / CombSUM classic methods — RRF supersedes them for our use
- Per-query dynamic weight tuning (DAT) — out of scope
