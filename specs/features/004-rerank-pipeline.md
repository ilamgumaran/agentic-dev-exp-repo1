# Feature: ReRank Pipeline

## Status: READY

## Summary

Implement a composable pipeline that wires together a Tokenizer, one or more Scorers, and an Aggregator to produce ranked results from a query and documents.

## Motivation

The pipeline is the user-facing API. Users should be able to build a ranking pipeline by composing components, not by manually orchestrating tokenization, scoring, and aggregation. The builder pattern makes this intuitive.

## Behavior

### ReRankPipeline
- Built using a builder pattern
- Components:
  - `tokenizer` (required, exactly one)
  - `scorers` (required, at least one)
  - `aggregator` (required, exactly one)
  - `topK` (optional, default: return all results)
- Method: `List<RankedResult> rank(Query query, List<Document> documents)`

### Builder API

```java
var pipeline = ReRankPipeline.builder()
    .tokenizer(new StandardTokenizer())
    .scorer(new BM25Scorer(tokenizer))
    .scorer(new CosineSimilarityScorer())   // optional: multiple scorers
    .aggregator(new WeightedAggregator(0.7, 0.3))
    .topK(10)                               // optional
    .build();
```

### Pipeline Execution Flow
1. Accept Query and List<Document>
2. Pass each scorer the query and documents → get List<Score> per scorer
3. For each document, collect its scores from all scorers
4. Pass scores to Aggregator → get final score per document
5. Create RankedResult for each document with final score and component scores
6. Sort by RankedResult natural order (score descending, id ascending tiebreak)
7. If topK is set, truncate to topK results
8. Return the sorted list

### Aggregator Interface
- `double aggregate(List<Score> scores)`
- Package: `dev.reranker.engine`

### WeightedAggregator
- Takes weights as varargs doubles matching the number of scorers
- `aggregate(scores)` = Σ(weight_i × score_i.value())
- Validates that weights count matches scores count at aggregation time

### MaxAggregator
- Returns the maximum score value from the list

### AverageAggregator
- Returns the arithmetic mean of all score values

## Edge Cases
- build() with no tokenizer: throw IllegalStateException
- build() with no scorers: throw IllegalStateException
- build() with no aggregator: throw IllegalStateException
- rank() with empty documents list: return empty list
- rank() with empty query: return all documents with score 0.0, sorted by id
- topK = 0: return empty list
- topK > document count: return all documents
- WeightedAggregator with wrong number of weights: throw IllegalArgumentException at build time if weights count != scorers count

## Constraints
- No external dependencies
- Thread-safe (pipeline is immutable after build)
- Pipeline builder validates at build() time, not at rank() time
- Package: `dev.reranker.pipeline` (pipeline), `dev.reranker.engine` (aggregators)
