# Setting Up an Agentic Development Workflow

This guide walks through setting up a repository for spec-driven, test-first agentic development — the kind of workflow used by engineering teams at tech-forward companies.

## Prerequisites

- A git repository
- A coding agent (Claude Code, GitHub Copilot, Cursor, etc.)
- A build system with test support (Gradle, Maven, Cargo, etc.)
- An engineer who will write specs and review code

## Step 1: Create the Control Files

### CLAUDE.md (or equivalent agent config)

This file is the agent's "onboarding document." It tells the agent:
- What the project is
- Where things live
- What rules to follow
- How to build and test

```markdown
# CLAUDE.md
## Project: [name]
## Layout: [directory structure]
## Rules:
1. Read specs before implementing
2. TDD: tests first, then code
3. Never modify specs
## Build: [commands]
## Test: [commands]
```

**Why it matters**: Without this file, the agent will guess. Guessing produces inconsistent, unreviewable code. This file is the difference between a useful agent and a liability.

### AGENTS.md (multi-agent governance)

If multiple agent types work on your repo (code agents, review agents, analysis agents), this file defines their permissions and boundaries.

## Step 2: Create the Spec Structure

```
specs/
  features/           Feature specifications
  test-requirements/   What tests must exist
  acceptance-criteria/  How to verify correctness
```

### Writing a Good Spec

A spec must answer these questions:
1. **What** does this feature do? (behavior, not implementation)
2. **Why** does it exist? (business or technical motivation)
3. **What are the inputs and outputs?** (concrete examples)
4. **What are the edge cases?** (empty input, nulls, huge datasets)
5. **What are the constraints?** (performance, memory, dependencies)

Example:

```markdown
# Feature: BM25 Scoring

## What
Given a query and a document, compute a BM25 relevance score.

## Why
BM25 is the standard baseline for text retrieval. It must be
available as a built-in scorer.

## Inputs / Outputs
- Input: Query (string), Document (map of field→text), Config (k1, b)
- Output: Score (double, 0.0 to unbounded positive)

## Edge Cases
- Empty query → score 0.0
- Empty document → score 0.0
- Query term not in document → that term contributes 0.0
- Single-character query → valid, process normally

## Constraints
- Must process 10,000 documents in <10ms for single-term queries
- No external dependencies
- Thread-safe (immutable state)
```

### Writing Test Requirements

```markdown
# Test Requirements: BM25 Scoring

## Unit Tests
- test_empty_query_returns_zero
- test_empty_document_returns_zero
- test_single_term_single_document
- test_multi_term_scoring
- test_term_frequency_increases_score
- test_document_length_normalization
- test_idf_weighting
- test_custom_k1_b_parameters
- test_thread_safety_concurrent_scoring

## Property-Based Tests
- Score is always non-negative
- Higher term frequency → higher or equal score
- Score with all query terms present ≥ score with subset
```

## Step 3: Set Up the Build System

The build must support:
1. **Compilation** — fast feedback on syntax/type errors
2. **Unit tests** — run in <30 seconds
3. **Integration tests** — run in <2 minutes
4. **Benchmarks** — on-demand, not on every build

For this project:
```bash
# Java (Gradle)
gradle test          # unit + integration tests
gradle benchmark     # JMH benchmarks

# Rust (Cargo)
cargo test           # all tests
cargo bench          # benchmarks
```

## Step 4: Establish the Workflow

### The Daily Loop

```
Morning:
  1. Human reviews overnight PRs from agents
  2. Human writes 1-3 new specs for the day's work
  3. Human prioritizes specs

Working session:
  4. Point the agent at a spec: "Implement specs/features/bm25-scoring.md"
  5. Agent reads spec → writes tests → implements → commits
  6. Human reviews the PR
  7. Approve, request changes, or discuss

End of day:
  8. Human reviews all merged code holistically
  9. Human updates specs if requirements evolved
  10. Human plans tomorrow's specs
```

### Branch Strategy

```
main                          ← stable, tested, reviewed
  └── feat/bm25-scoring       ← agent works here
  └── feat/tokenizer-ngram    ← different agent, different feature
  └── feat/rust-ui-basic      ← another agent, Rust work
```

Each spec gets its own branch. Agents never work on main.

## Step 5: Quality Gates

### Pre-Merge Checklist (enforced by CI or human review)

- [ ] All tests pass
- [ ] Test coverage ≥ 80% for new code
- [ ] No new compiler warnings
- [ ] Implementation matches spec acceptance criteria
- [ ] No new external dependencies without approval
- [ ] Performance within spec constraints
- [ ] Code follows project standards (CLAUDE.md)

### Post-Merge Validation

- [ ] Integration tests pass on main
- [ ] Benchmarks show no regression
- [ ] Documentation updated if public API changed

## Step 6: Iterate and Improve

After every 5-10 specs implemented:
1. Review what the agent got right and wrong
2. Update CLAUDE.md with new rules based on patterns you see
3. Update spec templates if you find yourself repeating instructions
4. Tighten or loosen quality gates based on actual defect rates

The goal is to **teach the system, not individual agents**. Your CLAUDE.md and specs get better over time, and every agent that reads them benefits.
