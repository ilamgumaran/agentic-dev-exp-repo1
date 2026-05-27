# Agentic Development: Overview

## What Is Agentic Development?

Agentic development is a software engineering paradigm where **AI coding agents perform implementation work** while **human engineers control the architecture, specifications, and quality gates**. The human is the architect; the agent is the builder.

This is not "vibe coding" (letting AI generate whatever it wants). It is a disciplined workflow where every line of code traces back to a human-written specification, and every feature is validated against human-defined acceptance criteria.

## Why It Matters

Large tech-forward companies (Google, Anthropic, Stripe, Shopify) are already operating this way:

| Traditional | Agentic |
|-------------|---------|
| Engineer writes code | Engineer writes specs; agent writes code |
| Code review catches bugs | Tests catch bugs; code review validates design |
| Knowledge lives in engineers' heads | Knowledge lives in specs and docs |
| Onboarding takes weeks | Agent reads specs and starts in minutes |
| One engineer, one task | One engineer, many agents, many tasks |

## The HIO Model (Human-in-Orchestration)

HIO is the governance model for agentic development. It defines:

1. **Who decides what**: Humans decide *what* to build and *why*. Agents decide *how* to implement it.
2. **Quality gates**: Every transition (spec → test → code → review) has a gate.
3. **Spec authority**: The spec is the single source of truth. Agents cannot deviate from it.
4. **Reversibility**: Every agent action must be reversible (git revert, branch delete).

### The Orchestration Loop

```
┌──────────────────────────────────────────────────┐
│                 HUMAN DOMAIN                      │
│                                                   │
│  1. Identify need                                 │
│  2. Write specification                           │
│  3. Define acceptance criteria                    │
│  4. Review agent's work                           │
│  5. Approve or redirect                           │
└──────────┬───────────────────────────┬────────────┘
           │ spec                      │ feedback
           ▼                          ▲
┌──────────────────────────────────────────────────┐
│                 AGENT DOMAIN                      │
│                                                   │
│  1. Read specification                            │
│  2. Write failing tests (RED)                     │
│  3. Implement code (GREEN)                        │
│  4. Refactor (REFACTOR)                           │
│  5. Submit for review                             │
└──────────────────────────────────────────────────┘
```

## How This Repo Implements It

This repository is a working example of agentic development:

- **`/specs/`** — Human-written specifications (the "what")
- **`/reranker-core/`** — Agent-implemented Java code (the "how")
- **`/reranker-ui/`** — Agent-implemented Rust UI (the "how")
- **`/docs/`** — Shared knowledge for both humans and agents
- **`CLAUDE.md`** — Instructions that configure agent behavior
- **`AGENTS.md`** — Multi-agent governance rules

## Key Principles

### 1. Specs Are Law
The specification is the contract. An agent that deviates from the spec has produced a bug, even if the code "works." This is the fundamental discipline that makes agentic development reliable.

### 2. Tests Are the Handshake
Tests translate specs into machine-verifiable assertions. When an agent writes a test from a spec, it's proving it understood the requirement. When the test passes, it's proving the implementation is correct. This is why TDD is non-negotiable in agentic workflows.

### 3. Small, Atomic Specs
Each spec describes one feature, one behavior, one change. A spec that says "build the ranking engine" is too big. A spec that says "implement BM25 scoring for single-field documents" is right-sized. Small specs = small PRs = easy review = fast iteration.

### 4. The Agent Has No Memory
Every session starts fresh. The agent reads CLAUDE.md, reads the spec, and implements. It does not remember yesterday's discussion. This is a feature: it means the spec must be complete and self-contained, which makes the project more maintainable.

### 5. Humans Review, Not Rubber-Stamp
Code review in agentic development is architectural review. You're checking: Does this match the spec? Does the design fit the system? Are there performance implications the agent missed? You're not checking for typos.
