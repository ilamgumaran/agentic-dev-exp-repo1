# Tracking — ReRanker Project

End-to-end traceability from objectives → use cases → specs → code.

## Structure

```
tracking/
  objectives/    Why we're building (goals + key results)
  use-cases/     What we're building (business-justified features)
  decisions/     What we chose and why (architectural + process)
```

## The Chain

```
OBJ-001: Build a production-quality re-ranking library
  ├── KR: 4 core specs implemented with >80% test coverage
  │     └── Metric: metrics/project-health.md → Specs Completed
  ├── UC-001: Core Data Model
  │     └── specs/features/001-core-data-model.md ✅ IMPLEMENTED
  ├── UC-002: Text Tokenization
  │     └── specs/features/002-standard-tokenizer.md → READY
  ├── UC-003: BM25 Scoring
  │     └── specs/features/003-bm25-scorer.md → READY
  ├── UC-004: Composable Pipeline
  │     └── specs/features/004-rerank-pipeline.md → READY
  └── DR-001: Zero external dependencies
        └── Rationale: Embeddable, no conflicts, own every algorithm
```

## How to Keep Updated

### Humans (weekly)
- Review objectives — are key results on track?
- Check use case statuses — any stuck?
- Record new decisions when made

### Agents (per task)
- Check if your spec traces to a use case
- After implementation, update the spec status in the use case file
- Reference decisions that constrain your implementation

## Templates

See `software-engineering-hio-agent-framework/tracking/` for templates.
