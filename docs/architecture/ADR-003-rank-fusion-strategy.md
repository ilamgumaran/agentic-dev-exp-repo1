# ADR-003: Separate FusionStrategy from per-document Aggregator

## Status: Accepted

## Context

The ReRanker pipeline combines multiple scorers. The original design uses an `Aggregator` with the signature `double aggregate(List<Score> scores)` — it receives one document's component scores and returns a combined value. This works for score-based combination (weighted sum, max, average).

We want to add Reciprocal Rank Fusion (RRF), the industry-standard method for combining lexical (BM25) and semantic (vector) scorers. Research into Elasticsearch, OpenSearch, Weaviate, and Qdrant confirmed RRF is the robust default for hybrid search because it sidesteps the score normalization problem (BM25 unbounded vs cosine bounded).

The problem: **RRF cannot be expressed through the per-document `Aggregator` interface.** To compute a document's RRF score, you need its *rank* within each scorer's full result list. Rank is a global property of the entire result set, not a local property of one document. The `Aggregator` only ever sees one document's scores, so it cannot know ranks.

## Options Considered

### Option A: Force RRF into the Aggregator interface
Pre-compute ranks in the pipeline, pass them somehow into the per-document aggregator.
- Pros: One interface
- Cons: Pollutes the Aggregator contract; ranks would need to be smuggled through Score objects or thread-local state; breaks the clean per-document semantics

### Option B: New FusionStrategy interface that sees the full matrix
`double[] fuse(List<Document> documents, List<List<Score>> scoresByScorer)`
- Pros: Clean separation; RRF, normalized fusion, and future methods fit naturally; Aggregator stays simple for the cases it serves well
- Cons: Two combination concepts in the pipeline (must pick one)

### Option C: Replace Aggregator with FusionStrategy entirely
Make everything go through the matrix-level interface.
- Pros: One concept
- Cons: Forces simple per-document cases (weighted sum) to handle the full matrix unnecessarily; breaking change to existing spec 004

## Decision

Option B. Introduce `FusionStrategy` as a parallel concept to `Aggregator`. A pipeline uses exactly one of them. The `Aggregator` handles score-based per-document combination (when scores are comparable); `FusionStrategy` handles rank-based and normalized fusion (when scores are incomparable or rank matters).

## Consequences

**Positive:**
- RRF, MinMax normalized, and Z-score normalized fusion all fit cleanly
- Existing `Aggregator` path is unchanged (no regression)
- The matrix-level interface enables future methods (DBSF, CombMNZ) without further architectural change
- Clear conceptual split: per-document score combination vs result-set-level fusion

**Negative:**
- Two combination concepts; users must understand when to use which (documented in RANK_FUSION_AND_HYBRID_SEARCH.md)
- Pipeline builder must validate exactly-one-of (aggregator XOR fusion)

**Guidance for users:**
- Use `Aggregator` (WeightedAggregator) when your scorers produce comparable scores (e.g., two BM25 variants)
- Use `FusionStrategy` (ReciprocalRankFusion) when combining scorers with different scales (BM25 + cosine) — this is the common hybrid-search case
- Use `MinMaxNormalizedFusion` when you want to preserve score magnitude and can tune weights

## References

- Research and design: `docs/human-learning/RANK_FUSION_AND_HYBRID_SEARCH.md`
- Spec: `specs/features/005-rank-fusion.md`
- RRF paper (Cormack et al. 2009): https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf
