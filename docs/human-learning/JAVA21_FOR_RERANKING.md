# Java 21 Features for Re-Ranking: A Learning Guide

## Why Java 21?

Java 21 is an LTS (Long-Term Support) release with features that make it excellent for building high-performance libraries:

1. **Records** — immutable data carriers, perfect for Score, Document, Query types
2. **Sealed interfaces** — exhaustive type hierarchies for Tokenizer, Scorer variants
3. **Pattern matching** — clean, readable dispatching on types
4. **Virtual threads** — lightweight parallelism for concurrent scoring
5. **SequencedCollection** — ordered collections with first/last access

## Records: Your Data Model

Records replace boilerplate data classes. They're immutable, have auto-generated `equals()`, `hashCode()`, and `toString()`.

```java
// Old way: 50 lines of boilerplate
public class Document {
    private final String id;
    private final Map<String, String> fields;
    // constructor, getters, equals, hashCode, toString...
}

// Java 21 way: 1 line
public record Document(String id, Map<String, String> fields) {}
```

### When to Use Records in This Project

- `Document` — the input document with fields
- `Query` — the search query with terms
- `Token` — a parsed token with position info
- `Score` — a relevance score with metadata
- `RankedResult` — a document + its final score
- `ScorerConfig` — parameters like k1, b for BM25

### Record Gotcha: Defensive Copies

Records don't deep-copy their arguments. If you pass a mutable Map, someone can modify it after construction:

```java
public record Document(String id, Map<String, String> fields) {
    // Compact constructor — make defensive copy
    public Document {
        fields = Map.copyOf(fields); // immutable snapshot
    }
}
```

## Sealed Interfaces: Your Extension Points

Sealed interfaces define a closed set of implementations. The compiler knows all subtypes, enabling exhaustive pattern matching.

```java
public sealed interface Scorer permits BM25Scorer, TfIdfScorer, CosineSimilarityScorer {
    Score score(Query query, Document document);
}

public sealed interface Tokenizer permits StandardTokenizer, NGramTokenizer, StemmingTokenizer {
    List<Token> tokenize(String text);
}
```

### Why Sealed for This Project

- We want a known set of scorer types for optimization
- Pattern matching can dispatch without instanceof chains
- New implementations require modifying the sealed interface (intentional friction — human approval needed)

### When NOT to Use Sealed

If you want users of the library to add their own implementations, use a regular interface. For this project: `Scorer` will be a regular interface (users add custom scorers), but `AggregationStrategy` will be sealed (we control the combining logic).

## Pattern Matching: Clean Dispatching

```java
// Old way
if (scorer instanceof BM25Scorer) {
    BM25Scorer bm25 = (BM25Scorer) scorer;
    bm25.configure(k1, b);
} else if (scorer instanceof TfIdfScorer) {
    // ...
}

// Java 21 way
switch (scorer) {
    case BM25Scorer bm25 -> bm25.configure(k1, b);
    case TfIdfScorer tfidf -> tfidf.normalize();
    case CosineSimilarityScorer cos -> cos.setThreshold(0.5);
}
```

### Pattern Matching with Records

```java
public record ScorerResult(String scorerName, double score) {}

// Destructuring in switch
String describe(ScorerResult result) {
    return switch (result) {
        case ScorerResult(var name, var s) when s > 0.9 -> name + ": excellent";
        case ScorerResult(var name, var s) when s > 0.5 -> name + ": good";
        case ScorerResult(_, var s) -> "low: " + s;
    };
}
```

## Virtual Threads: Parallel Scoring

Virtual threads are lightweight (not OS threads). You can create millions of them. Perfect for scoring documents in parallel:

```java
// Score 10,000 documents across virtual threads
List<Score> scores;
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var futures = documents.stream()
        .map(doc -> executor.submit(() -> scorer.score(query, doc)))
        .toList();

    scores = futures.stream()
        .map(f -> {
            try { return f.get(); }
            catch (Exception e) { throw new RuntimeException(e); }
        })
        .toList();
}
```

### When to Use Virtual Threads in This Project

- **Default: single-threaded**. Most scoring is CPU-bound and fast enough sequentially.
- **Opt-in parallelism**: For >1000 documents with expensive scorers (semantic matching), offer a parallel pipeline mode.
- **Never for token matching**: Token matching on 10K docs takes <5ms single-threaded. Thread creation overhead would exceed the savings.

### Virtual Threads Gotcha

Virtual threads are bad for:
- CPU-intensive tight loops (they don't help — CPU is the bottleneck)
- synchronized blocks (they pin the carrier thread)

Use them for I/O-bound work or coarse-grained parallelism (score chunk 1 while scoring chunk 2).

## Useful Java 21 APIs for This Project

### `List.of()`, `Map.of()` — Immutable Collections
```java
var stopWords = Set.of("the", "a", "an", "is", "are");
var defaultWeights = Map.of("title", 2.0, "description", 1.0);
```

### `Stream.toList()` — Unmodifiable List from Stream
```java
var topK = scores.stream()
    .sorted(Comparator.reverseOrder())
    .limit(k)
    .toList(); // unmodifiable
```

### `String.strip()`, `String.isBlank()` — Text Processing
```java
// For tokenization
if (text == null || text.isBlank()) return List.of();
var cleaned = text.strip().toLowerCase();
```

### `Math.log()`, `Math.sqrt()` — Scoring Math
```java
// IDF calculation
double idf = Math.log((totalDocs - docFreq + 0.5) / (docFreq + 0.5) + 1.0);
```

### `Optional<T>` — No Nulls
```java
public Optional<RankedResult> topResult(List<RankedResult> results) {
    return results.stream()
        .max(Comparator.comparingDouble(RankedResult::score));
}
```

## Performance Patterns for Re-Ranking

### Pre-compute What You Can
```java
// Build IDF cache once, reuse across queries
record IdfCache(Map<String, Double> idfValues) {
    static IdfCache build(List<Document> corpus) {
        // compute IDF for every term in the corpus
        // this is O(corpus_size) but done once
    }
}
```

### Use Primitive Arrays for Hot Paths
```java
// For scoring inner loops, avoid boxing
double[] scores = new double[documents.size()];
for (int i = 0; i < documents.size(); i++) {
    scores[i] = computeScore(query, documents.get(i));
}
```

### Avoid Allocation in Scoring
```java
// Bad: creates a new ArrayList per document
List<Double> termScores = new ArrayList<>();

// Good: reuse a double array
double[] termScores = new double[query.terms().size()];
```
