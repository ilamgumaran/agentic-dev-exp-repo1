# CLAUDE.md — Agentic Development Instructions

## Project Identity

**Name**: ReRanker — A high-performance document re-ranking library  
**Language**: Java 21+ (core library), Rust (UI/test harness)  
**Methodology**: Spec-Driven TDD under HIO (Human-in-Orchestration)

## Repository Layout

```
/reranker-core/        Java library — the re-ranking engine
/reranker-ui/          Rust binary — UI layer for testing the library via JNI/FFI
/specs/                Human-written specifications (READ-ONLY for agents)
/docs/                 Documentation for humans and agents
  /agentic-development   How the agentic workflow operates
  /human-learning        Concepts explained for human engineers
  /specs/                Spec templates and guidelines
  /architecture/         ADRs and system design documents
```

## Rules for Coding Agents

### Spec Protocol
1. **NEVER modify files under `/specs/`** — these are human-authored
2. **ALWAYS read the relevant spec** before implementing any feature
3. If a spec is ambiguous, stop and ask — do not guess
4. Match implementation exactly to acceptance criteria in the spec

### TDD Protocol (Strict)
1. Read the spec's test-requirements file first
2. Write the test class with all test methods (they will fail)
3. Run the tests — confirm they fail for the right reason
4. Implement the minimum code to make tests pass
5. Refactor only if tests still pass
6. Never skip step 2 — tests must exist before implementation

### Code Standards — Java
- Java 21+ features required: records, sealed interfaces, pattern matching, virtual threads
- No `null` returns — use `Optional<T>` or throw
- All public APIs must have `@param` and `@return` javadoc (one line each)
- Immutable data objects — use records
- Package structure mirrors the module it belongs to
- No `System.out.println` — use `java.util.logging` or SLF4J
- Benchmark-sensitive code must document its O(n) complexity

### Code Standards — Rust
- Edition 2024
- Use `thiserror` for error types, `anyhow` for application errors
- All public functions documented with `///` doc comments
- `clippy` must pass with no warnings
- The Rust layer calls Java via JNI — keep the FFI boundary minimal

### Build & Test Commands
```bash
# Java — build and test
cd reranker-core && gradle build
cd reranker-core && gradle test

# Rust — build and test
cd reranker-ui && cargo build
cd reranker-ui && cargo test

# Full pipeline
gradle -p reranker-core test && cd reranker-ui && cargo test
```

### Git Conventions
- Branch: work on the designated feature branch
- Commit messages: `<type>(<scope>): <description>` where type is feat|fix|test|docs|refactor
- One logical change per commit
- Never force push

### Architecture Constraints
- The Java library must have ZERO external dependencies for the core ranking engine
- Tokenization, scoring, and semantic matching are separate modules behind interfaces
- The pipeline is composable — users wire together stages
- Latency budget: <10ms for token matching on 10K documents
- CPU budget: single-threaded by default, opt-in parallelism via virtual threads
