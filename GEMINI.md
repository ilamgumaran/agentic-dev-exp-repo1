# GEMINI.md — Gemini Configuration for ReRanker

This file configures Gemini Code Assist and Gemini CLI for this repository. For agent-agnostic rules shared across all coding agents, see `.agent-config/AGENT_RULES.md`.

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

## Detailed Rules

See `.agent-config/AGENT_RULES.md` for complete rules, TDD protocol, and architecture constraints.
See `.agent-config/security-boundaries.md` for security rules.
