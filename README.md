# ReRanker

A high-performance, zero-dependency Java 21 library for re-ranking documents by relevance. Combines token matching and semantic similarity scoring with composable pipelines, optimized for low latency and moderate CPU usage.

## What It Does

Given a list of documents (product catalog entries, financial records, email snippets, or any structured text) and a query keyword, ReRanker finds the most relevant documents using:

- **Token matching** — exact and fuzzy keyword overlap with TF-IDF weighting
- **Semantic matching** — vector similarity using lightweight embeddings
- **Composite scoring** — weighted combination of multiple signals
- **Pipeline composition** — plug together tokenizers, scorers, and rankers

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  Pipeline                        │
│  ┌───────────┐  ┌──────────┐  ┌──────────────┐ │
│  │ Tokenizer │→ │ Scorer[] │→ │ Aggregator   │ │
│  └───────────┘  └──────────┘  └──────────────┘ │
│       ↓              ↓              ↓           │
│   Token[]      Score[][]      RankedResult[]    │
└─────────────────────────────────────────────────┘
         ↑                              │
    Document[]                    sorted output
    + Query
```

### Modules

| Module | Purpose |
|--------|---------|
| `model` | Core data types: Document, Query, Token, Score, RankedResult |
| `tokenizer` | Text tokenization strategies (whitespace, n-gram, stemming) |
| `scoring` | Scoring algorithms (TF-IDF, BM25, cosine similarity) |
| `semantic` | Lightweight semantic matching (no ML framework dependency) |
| `engine` | Ranking engine that orchestrates the pipeline |
| `pipeline` | Composable pipeline builder |
| `config` | Configuration and tuning parameters |

## Quick Start

```java
var pipeline = ReRankPipeline.builder()
    .tokenizer(new StandardTokenizer())
    .scorer(new BM25Scorer())
    .scorer(new CosineSimilarityScorer())
    .aggregator(new WeightedAggregator(0.6, 0.4))
    .build();

var documents = List.of(
    Document.of("SKU-001", Map.of("title", "Blue Running Shoes", "category", "Footwear")),
    Document.of("SKU-002", Map.of("title", "Red Hiking Boots", "category", "Footwear")),
    Document.of("SKU-003", Map.of("title", "Blue Denim Jacket", "category", "Apparel"))
);

var results = pipeline.rank(Query.of("blue shoes"), documents);
// results: [SKU-001 (0.92), SKU-003 (0.41), SKU-002 (0.23)]
```

## Building

```bash
# Java library
cd reranker-core && gradle build

# Rust UI
cd reranker-ui && cargo build

# Run all tests
gradle -p reranker-core test && cd reranker-ui && cargo test
```

## Development Methodology

This project uses **spec-driven TDD** under the **HIO (Human-in-Orchestration)** framework:

1. Humans write specifications in `/specs/`
2. Coding agents implement following TDD (red → green → refactor)
3. Humans review and approve via PR

See `docs/agentic-development/` for the full workflow guide.

## Project Structure

```
reranker-core/          Java 21 library (Gradle)
reranker-ui/            Rust UI for testing (Cargo)
specs/                  Human-written feature specifications
docs/                   Documentation
  agentic-development/  How the agent workflow operates
  human-learning/       Concepts for human engineers
  architecture/         Architecture Decision Records
```

## License

MIT
