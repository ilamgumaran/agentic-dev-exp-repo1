# GitHub Copilot Instructions — ReRanker

This file configures GitHub Copilot for this repository. For agent-agnostic rules shared across all coding agents, see `.agent-config/AGENT_RULES.md`.

## Project

ReRanker is a zero-dependency Java 21 library for re-ranking documents by relevance, with a Rust terminal UI for testing.

## Key Rules

1. Read the spec in `specs/features/` before implementing any feature
2. Follow TDD: write failing tests first, then implement, then refactor
3. Never modify files under `specs/` — they are human-authored
4. Use Java 21 records for all data types; never return null
5. Zero external runtime dependencies for `reranker-core`

## Build Commands

- Java test: `cd reranker-core && gradle test`
- Rust test: `cd reranker-ui && cargo test`
- Java build: `cd reranker-core && gradle build`

## Code Style

- Java: records, sealed interfaces, pattern matching, Optional over null
- Rust: edition 2024, thiserror/anyhow, clippy clean
- Commits: `<type>(<scope>): <description>`

## Detailed Rules

See `.agent-config/AGENT_RULES.md` for complete rules, TDD protocol, and architecture constraints.
See `.agent-config/security-boundaries.md` for security rules.
