# Rank Fusion & Hybrid Search — Research and Design

## Why This Matters for ReRanker

Our current pipeline combines multiple scorers using **score-based aggregation** (`WeightedAggregator`, `MaxAggregator`, `AverageAggregator`). This works when scorers produce comparable scores. But it breaks down in the most important real-world case: **combining lexical (BM25) and semantic (vector cosine) scores**.

The problem: BM25 scores are unbounded (0 to 30+, corpus-dependent) while cosine similarity is bounded ([-1, 1]). Adding or averaging them lets the larger-magnitude signal dominate purely by scale, not by relevance. This is the **score normalization problem**, and it's why Elasticsearch, OpenSearch, Weaviate, Qdrant, and Vespa all ship dedicated fusion methods.

## The Two Families of Fusion

### Rank-Based Fusion (uses position, ignores score magnitude)

**Reciprocal Rank Fusion (RRF)** — the industry default.

```
score(d) = Σ  1 / (k + rank_i(d))
          i
```

- `rank_i(d)` = 1-based rank of document `d` in scorer `i`'s result list
- `k` = rank constant, default **60** (must be ≥ 1)
- Documents missing from a scorer's list contribute 0 for that scorer
- Higher k flattens the curve (lower ranks matter more); lower k favors top ranks sharply

**Why RRF works**: it discards raw scores entirely and uses only rank position. No normalization, no per-query tuning, scale-invariant, robust to outliers and distribution drift. It rewards **consensus** — a document ranked moderately well by two scorers beats a document ranked #1 by only one.

**Worked example** (k=60), two scorers:
- BM25 ranks: [A, B, C, D]
- Cosine ranks: [C, A, E, B]

| Doc | BM25 rank | Cosine rank | RRF score | Calculation |
|-----|-----------|-------------|-----------|-------------|
| A | 1 | 2 | **0.03252** | 1/61 + 1/62 |
| C | 3 | 1 | **0.03226** | 1/63 + 1/61 |
| B | 2 | 4 | **0.03175** | 1/62 + 1/64 |
| E | – | 3 | **0.01587** | 1/63 |
| D | 4 | – | **0.01563** | 1/64 |

Final order: **A > C > B > E > D**. A wins (top-3 in both lists) over C (which was #1 in cosine) — consensus beats a single high rank. E and D, appearing in only one list, fall to the bottom.

**Tradeoff**: RRF throws away magnitude. The gap between rank 1 and rank 2 is treated identically whether the top doc was a perfect match or barely better.

### Score-Based Fusion (preserves magnitude, requires normalization)

**Min-Max Normalized Linear (Convex) Combination**:

```
norm_i(d) = (score_i(d) - min_i) / (max_i - min_i)
final(d)  = Σ  weight_i × norm_i(d)
```

- Normalize each scorer's results to [0, 1], then weighted-sum
- The `alpha` parameter blends dense vs sparse: `alpha × dense + (1-alpha) × sparse`
- Preserves "how much better" information
- Sensitive to outliers (one extreme score compresses everything else)
- This is what we already have (`WeightedAggregator`) — but WITHOUT normalization, which is the missing piece

**Z-Score Normalization** (more outlier-robust):
```
norm_i(d) = (score_i(d) - μ_i) / σ_i
```

**Distribution-Based Score Fusion (DBSF)** — Qdrant's method: normalize using mean ± 3σ as bounds (3-sigma clipping), then sum. More robust than plain min-max when distributions are Gaussian-ish with outliers.

### Classic IR Methods (predecessors to RRF)

- **CombSUM**: sum of normalized scores: `Σ norm_i(d)`
- **CombMNZ**: `CombSUM(d) × (number of lists containing d)` — rewards consensus, score-based analogue of RRF

## When to Use Which

| Method | Use When |
|--------|----------|
| **RRF** | Combining scorers with incomparable scales (BM25 + cosine); want zero-tuning robust default; rank order reliable but magnitudes aren't |
| **Min-max normalized linear** | Score magnitudes carry real relevance signal; can tune alpha per domain; want to preserve "how much better" |
| **Z-score normalized** | Score distributions roughly Gaussian; need outlier robustness |
| **DBSF** | Gaussian-ish distributions with outliers; min-max too sensitive |
| **CombMNZ** | Want score-based consensus rewarding |
| **Plain weighted sum (current)** | Scorers already produce comparable scores (e.g., two BM25 variants) |

## Vendor Landscape

| System | Default Fusion | Also Offers |
|--------|---------------|-------------|
| Elasticsearch | RRF (k=60) | Linear (via retrievers) |
| OpenSearch | Min-max normalization | RRF (2.19+) |
| Weaviate | Relative Score Fusion (min-max + alpha) | Ranked Fusion (RRF) |
| Qdrant | RRF | DBSF |
| Vespa | Custom ranking expressions | RRF feature, linear normalize |
| Pinecone | Convex combination | — |

Notable finding: research ("An Analysis of Fusion Functions," arXiv 2210.11934) shows **convex combination often beats RRF** when scores are reliable, but RRF wins on robustness and zero-tuning. Weaviate switched its default from RRF to Relative Score Fusion in v1.24 because magnitude information improves relevance when available.

## The Architectural Insight for ReRanker

Our current `Aggregator.aggregate(List<Score> scores)` operates **per-document** — it sees one document's component scores and produces one value. This works for score-based fusion (weighted sum, max, average).

**RRF cannot use this interface.** To compute a document's RRF score, you need its *rank* within each scorer's full result list — which means you need to see ALL documents' scores for a given scorer, not just one document's. Rank is a global property of the result set, not a local property of one document.

This requires a new abstraction: a **FusionStrategy** that operates on the full score matrix (all documents × all scorers) rather than per-document. The existing `Aggregator` becomes the special case of per-document, score-based fusion.

```
Current:  Scorer[] → per-document Aggregator → score
                     (sees one doc's scores)

Enhanced: Scorer[] → FusionStrategy → ranked results
                     (sees the full matrix: docs × scorers)
```

This is designed in `specs/features/005-rank-fusion.md`.

## Future: Learning-to-Rank

Beyond fusion, production systems add a **cross-encoder re-ranking layer**: retrieve candidates via hybrid fusion, then re-score the top-N with a model that jointly attends to query and document. This is expensive (run only on top-N) and needs training data or a pretrained cross-encoder. Out of scope for the zero-dependency core, but the `Scorer` interface allows users to plug in their own cross-encoder scorer, and `FusionStrategy` can be the candidate-generation stage feeding it.

## Sources

- RRF paper (Cormack et al. 2009): https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf
- Elasticsearch RRF: https://www.elastic.co/docs/reference/elasticsearch/rest-apis/reciprocal-rank-fusion
- OpenSearch RRF: https://opensearch.org/blog/introducing-reciprocal-rank-fusion-hybrid-search/
- Weaviate fusion algorithms: https://weaviate.io/blog/hybrid-search-fusion-algorithms
- Qdrant hybrid (RRF & DBSF): https://qdrant.tech/articles/hybrid-search/
- Analysis of Fusion Functions: https://arxiv.org/pdf/2210.11934
- DBSF: https://medium.com/plain-simple-software/distribution-based-score-fusion-dbsf-a-new-approach-to-vector-search-ranking-f87c37488b18
