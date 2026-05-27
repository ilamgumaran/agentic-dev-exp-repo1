# ADR-001: Zero External Dependencies for Core Engine

## Status: Accepted

## Context

The re-ranking core library needs to be embeddable in any Java application. External dependencies create version conflicts, increase JAR size, and add supply chain risk.

## Decision

The `reranker-core` module will have **zero external dependencies** at runtime. Only test dependencies (JUnit 5) are allowed.

All tokenization, scoring, and data structures are implemented from scratch using only `java.base` module APIs.

## Consequences

**Positive:**
- No dependency conflicts when embedded
- Minimal JAR size (~50KB)
- No supply chain risk
- Forces us to understand and own every algorithm

**Negative:**
- More code to write and maintain
- No ready-made tokenizers (Lucene), no ML libraries
- Semantic matching limited to non-neural approaches without external embeddings

**Mitigations:**
- The Scorer interface allows users to bring their own implementation that uses external libraries
- The pipeline is composable — users can inject neural scorers alongside our built-in ones
