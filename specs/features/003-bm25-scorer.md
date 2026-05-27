# Feature: BM25 Scorer

## Status: READY

## Summary

Implement the BM25 (Best Matching 25) scoring algorithm as a Scorer that computes relevance scores for documents against a query.

## Motivation

BM25 is the industry-standard baseline for text retrieval scoring. It improves on TF-IDF by normalizing for document length and saturating term frequency. Every serious re-ranking library must include BM25.

## Behavior

### Interface: Scorer
- Method: `List<Score> score(Query query, List<Document> documents)`
- Returns one Score per document, in the same order as the input
- Package: `dev.reranker.scoring`

### BM25Scorer
- Implements Scorer
- Configuration: `BM25Config(double k1, double b)` — a record
  - k1: term frequency saturation parameter (default 1.2, must be >= 0)
  - b: document length normalization (default 0.75, must be between 0.0 and 1.0 inclusive)
- Uses a Tokenizer to process documents and queries

### Algorithm

For a query Q with terms q1...qn and document D:

```
score(Q, D) = Σ IDF(qi) × (tf(qi, D) × (k1 + 1)) / (tf(qi, D) + k1 × (1 - b + b × |D| / avgdl))
```

Where:
- `tf(qi, D)` = frequency of term qi in document D (count of occurrences)
- `|D|` = total number of tokens in document D
- `avgdl` = average document length across all documents being scored
- `IDF(qi) = ln((N - n(qi) + 0.5) / (n(qi) + 0.5) + 1)`
- `N` = total number of documents
- `n(qi)` = number of documents containing term qi

### Multi-Field Support
- Score each field independently
- Combine field scores using FieldWeight values
- Default: all fields have weight 1.0
- `BM25Scorer(BM25Config config, Tokenizer tokenizer)` — equal field weights
- `BM25Scorer(BM25Config config, Tokenizer tokenizer, List<FieldWeight> fieldWeights)` — custom weights

### Constructors
- `BM25Scorer(Tokenizer tokenizer)` — default config (k1=1.2, b=0.75), equal weights
- `BM25Scorer(BM25Config config, Tokenizer tokenizer)` — custom config, equal weights
- `BM25Scorer(BM25Config config, Tokenizer tokenizer, List<FieldWeight> fieldWeights)` — full control

## Edge Cases
- Empty query: return Score(0.0) for every document
- Empty document (no fields or all fields empty): Score(0.0)
- Query term not in any document: IDF still computed, contributes 0 to docs without it
- Single document: avgdl = that document's length
- All documents have same length: length normalization has no effect (b cancels out)
- k1 = 0: only IDF matters, term frequency ignored
- b = 0: no length normalization
- b = 1: full length normalization

## Constraints
- No external dependencies
- Thread-safe (immutable config, stateless scoring — corpus stats computed per call)
- O(Q × D × T) where Q=query terms, D=documents, T=average tokens per doc
- Must score 10,000 single-field documents in <10ms for a 3-term query (single thread)
- Package: `dev.reranker.scoring`
