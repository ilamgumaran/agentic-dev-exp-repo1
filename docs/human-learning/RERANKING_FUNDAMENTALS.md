# Re-Ranking Fundamentals: A Human Learning Guide

## What Is Document Re-Ranking?

Re-ranking is the process of taking a set of candidate documents and reordering them by relevance to a query. It sits downstream of retrieval:

```
User query → Retrieval (fast, rough) → Candidate set → RE-RANKING (precise) → Final results
```

Re-ranking is used everywhere:
- **E-commerce**: "blue running shoes" → rank 50K products by relevance
- **Search engines**: rank web pages for a query
- **Email**: rank inbox messages by importance
- **Financial analysis**: rank records matching a keyword
- **RAG pipelines**: rank retrieved chunks before feeding to an LLM

## Why Not Just Use the Retrieval Score?

Retrieval systems (Elasticsearch, Solr, vector databases) optimize for **recall** — finding all possibly-relevant documents quickly. Their ranking is approximate. Re-ranking optimizes for **precision** — putting the best documents first using more expensive scoring.

| Stage | Speed | Precision | Typical Size |
|-------|-------|-----------|--------------|
| Retrieval | <50ms for millions | Low-Medium | 100-10,000 candidates |
| Re-ranking | <10ms for thousands | High | 10-1,000 results |

## The Two Types of Matching

### Token Matching (Lexical)

Token matching compares the actual words/tokens in the query against the document.

**TF-IDF (Term Frequency - Inverse Document Frequency)**
- TF: How often does this term appear in this document? (More = more relevant)
- IDF: How rare is this term across all documents? (Rarer = more important)
- Score = TF × IDF
- Intuition: A document that mentions "quantum" 5 times is more relevant to "quantum" than one that mentions it once — BUT the word "the" appearing 5 times tells us nothing.

**BM25 (Best Matching 25)**
- An improvement on TF-IDF that handles document length
- Short documents that mention a term get boosted (they're more focused)
- Long documents that mention a term get dampened (the term is diluted)
- Parameters: k1 (term frequency saturation, typically 1.2) and b (length normalization, typically 0.75)
- Formula: `score(q,d) = Σ IDF(qi) · (tf(qi,d) · (k1+1)) / (tf(qi,d) + k1 · (1 - b + b · |d|/avgdl))`

### Semantic Matching

Semantic matching captures meaning, not just words. "running shoes" should match "jogging sneakers" even though no tokens overlap.

**Vector Similarity**
- Convert query and document to vectors (embeddings)
- Compute cosine similarity: `cos(θ) = (A·B) / (|A|·|B|)`
- Score ranges from -1 (opposite) to 1 (identical)

**Lightweight Approaches (No ML Framework)**
- Character n-gram overlap (captures subword similarity)
- Synonym dictionaries (curated word mappings)
- Edit distance for fuzzy matching
- WordNet-based similarity (if available)

## Combining Scores

Real re-rankers combine multiple signals:

```
final_score = w1 × token_score + w2 × semantic_score + w3 × freshness_score + ...
```

The weights (w1, w2, w3...) are tuned per use case:
- E-commerce: high weight on token match (users search for exact products)
- Email: high weight on semantic match (users search for concepts)
- Financial: high weight on exact match (ticker symbols, dates)

## Performance Considerations

### Latency Budget
- Interactive search: <50ms total, so re-ranking gets ~10ms
- Batch processing: seconds are fine
- Key insight: O(n·m) where n=documents, m=query terms — must be linear or better

### CPU vs. Quality Tradeoff
- Simple TF-IDF: fast, low CPU, decent quality
- BM25: slightly more CPU, much better quality
- Semantic + BM25: 2-5x CPU, best quality
- Full neural re-ranking: 100x CPU, marginal improvement over BM25+semantic

### Memory
- Token indices: O(vocabulary size)
- Document vectors: O(documents × vector dimensions)
- For 10K documents with 100-dim vectors: ~4MB — fits in L3 cache

## Data Structures for Fast Ranking

### Inverted Index
Maps tokens → list of (document_id, positions). Used for fast token lookup.
```
"blue" → [(doc1, [0, 15]), (doc3, [7])]
"shoes" → [(doc1, [2]), (doc2, [0, 3])]
```

### Priority Queue (Min-Heap)
When you need top-K results from N documents, use a min-heap of size K. Process each document: if its score > heap.peek(), replace. This is O(N·log K) instead of O(N·log N) for full sort.

### Sparse Vectors
Most documents don't contain most terms. Use sparse vector representation (only store non-zero entries) to save memory and speed up dot products.

## Multi-Field Documents

Real documents have multiple fields with different importance:

```json
{
  "title": "Blue Running Shoes",        // weight: 3.0
  "description": "Comfortable shoes...", // weight: 1.0
  "category": "Footwear",               // weight: 2.0
  "brand": "Nike"                       // weight: 1.5
}
```

Score each field independently, then combine with field weights. The title match matters more than the description match because titles are concise and specific.

## Evaluation Metrics

How do you know if your re-ranking is good?

- **NDCG@K** (Normalized Discounted Cumulative Gain): Measures ranking quality with position-aware weighting. The gold standard.
- **MRR** (Mean Reciprocal Rank): How high is the first relevant result? Good for navigational queries.
- **Precision@K**: What fraction of the top-K results are relevant? Simple and intuitive.
- **MAP** (Mean Average Precision): Average precision at each relevant document. Good for comprehensive evaluation.

These metrics are implemented in your `relevancyengineer` repo — the two projects are complementary.

## What We're Building

This library implements a composable re-ranking pipeline:

1. **Tokenizer** — splits text into tokens (pluggable strategies)
2. **Scorers** — compute relevance scores (BM25, cosine, custom)
3. **Aggregator** — combines multiple scores with weights
4. **Pipeline** — wires everything together

The design priorities are:
- **Zero dependencies** for the core engine
- **<10ms latency** for 10K documents
- **Composable** — mix and match scorers
- **Multi-field** — handle complex documents
- **Thread-safe** — immutable data, pure functions
