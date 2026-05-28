# Feature: Cosine Similarity Scorer (Character N-Gram)

## Status: READY

## Summary

Implement a lightweight semantic-ish scorer using cosine similarity over character n-gram frequency vectors. Zero dependencies, no embeddings, no ML framework. Produces bounded scores in [0.0, 1.0] — deliberately a different scale from BM25's unbounded scores, making it the ideal second signal for demonstrating and testing hybrid rank fusion.

## Motivation

To demonstrate hybrid search and rank fusion (specs 005, 006), the library needs at least two scorers with genuinely different score scales. BM25 (spec 003) is unbounded lexical. This scorer adds a bounded [0,1] fuzzy/character-level similarity signal that catches morphological matches BM25 misses ("running" ~ "runner", "jog" ~ "jogging") without needing embeddings. It is NOT a true semantic embedding model — it is a fast, dependency-free approximation suitable for re-ranking and for exercising the fusion machinery.

## Behavior

### CosineSimilarityScorer (implements Scorer)

**Configuration**: `NGramConfig(int n)` — a record
- `n`: character n-gram size, default 3 (trigrams), must be >= 1

**Algorithm** (`score(query, documents)`):
1. Build the query's character n-gram frequency vector from `query.text()` (lowercased).
2. For each document, concatenate its field values (space-separated), lowercase, build its character n-gram frequency vector.
3. Compute cosine similarity between query vector and document vector:
   `cos = (A · B) / (|A| · |B|)`
4. Return a `Score("cosine-ngram", cos)` per document, in document order. Cosine of non-negative frequency vectors is in [0, 1].

### N-Gram Extraction
- Lowercase the text (Locale.ROOT).
- Slide a window of size `n` across the character sequence, producing n-grams.
- Count frequency of each n-gram (the vector is a map of n-gram → count).
- If text length < n, the only n-gram is the whole (padded or whole) string; if text is empty, the vector is empty.

### Constructors
- `CosineSimilarityScorer()` — default n=3
- `CosineSimilarityScorer(NGramConfig config)` — custom n

## Edge Cases
- Empty query text: cannot construct a Query (Query rejects blank), so not reachable via Query; but if document field text is empty → its vector is empty → cosine 0.0
- Document with empty fields: cosine 0.0 for all queries
- Query and document share no n-grams: cosine 0.0
- Identical query and document text: cosine 1.0
- Single-character query with n=3: query vector has one n-gram (the padded/whole string); handle without error
- `NGramConfig` with n < 1: IllegalArgumentException

## Constraints
- No external dependencies
- Scores bounded in [0.0, 1.0]
- Thread-safe (immutable config, stateless scoring)
- O(D × L) where D = documents, L = average document length (n-gram extraction is linear)
- Package: `dev.reranker.semantic`

## Non-Goals
- True neural embeddings (out of scope for zero-dependency core; users can plug in their own `Scorer`)
- Token-level (word) cosine — this is character n-gram by design, to complement BM25's token matching
- Configurable field weighting (use BM25 for field-weighted token matching)
