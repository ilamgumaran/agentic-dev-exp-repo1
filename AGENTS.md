# AGENTS

ReRanker — a zero-dependency Java 21 re-ranking library with a Rust terminal UI.

> AI agents: read this file before making changes. For detailed rules, see `.agent-config/AGENT_RULES.md`.

## Family

This repo is part of the HIO repo family.

| Repo | Relationship |
|------|-------------|
| `software-engineering-hio-agent-framework` | Agent framework and governance (Layer 3) |
| `software-engineer-core-structure` | Org structure and role definitions (Layer 2) |
| `relevancyengineer` | Relevancy domain knowledge and metrics |
| `ThoughtExperiments` | Cognitive foundation (Layer 1) |

## Purpose and scope

This repo hosts a document re-ranking library optimized for low latency and moderate CPU usage. It supports multiple document types (e-commerce catalogs, financial records, email snippets) and combines token matching (BM25) with semantic similarity scoring through composable pipelines.

**Not in scope**: production search infrastructure, neural re-ranking models, data ingestion, or indexing.

## Key concepts owned here

- **ReRankPipeline**: Composable pipeline wiring tokenizers, scorers, and aggregators
- **Scorer interface**: Contract for relevance scoring implementations
- **Tokenizer interface**: Contract for text tokenization strategies
- **Aggregator**: Strategy for combining multiple scorer outputs
- **Spec-driven TDD**: Feature specs → test requirements → acceptance criteria → implementation

## How to make changes

1. Read the relevant spec in `specs/features/`
2. Follow TDD: write failing tests, implement, refactor
3. Branch naming: `feat/<feature>`, `fix/<bug>`, `refactor/<scope>`
4. Commit style: `<type>(<scope>): <description>` (types: feat, fix, test, docs, refactor)
5. One logical change per commit; never force push

## Dos and don'ts

**Do:**
- Read the spec AND test requirements AND acceptance criteria before starting
- Write every test listed in test-requirements before writing implementation code
- Use Java 21 records for all data types; use `Optional<T>` instead of null returns
- Keep the Java core library at zero external runtime dependencies
- Run the full test suite before committing

**Don't:**
- Modify files under `specs/` — they are human-authored and read-only for agents
- Add external dependencies to `reranker-core` without human approval
- Use `System.out.println` — use `java.util.logging` or SLF4J
- Skip the RED phase of TDD (tests must fail before you implement)
- Create mutable data objects — use records with defensive copies

## HIO routing

| Task signal | Route | Why |
|-------------|-------|-----|
| Implement a spec'd feature | Agent (II) | Spec provides full context; reversible via branch |
| Write tests from test-requirements | Agent (II) | Mechanical translation of requirements to test code |
| Fix a failing test | Agent (II) | Narrow scope, reversible |
| Add a new external dependency | Human (OI) | Supply chain decision; irreversible architectural impact |
| Change a public API signature | Interactive | Needs spec update (human) then implementation (agent) |
| Modify AGENTS.md or CLAUDE.md | Human (OI) | Governance files; affects all future agent behavior |
| Create or modify a spec | Human (OI) | Specs are human-authored by definition |
| Performance optimization | Interactive | Agent proposes; human validates against benchmarks |
| Security-sensitive change | Human (OI) | Irreversible risk; requires security review |

## Security boundaries

**An agent may:**
- Read all files in the repo
- Create feature branches and open PRs
- Write code, tests, and documentation (outside `specs/`)
- Run build and test commands
- Propose ADRs as drafts

**An agent must not:**
- Push to `main` or force-push to any branch
- Commit secrets, credentials, API keys, or PII
- Add runtime dependencies to `reranker-core`
- Modify specs, AGENTS.md, CLAUDE.md, or `.agent-config/security-boundaries.md`
- Execute arbitrary network requests
- Auto-merge PRs it authored

For org-wide rules, see `software-engineering-hio-agent-framework/multi-repo-orchestration/governance/security-and-safety.md`.

## Trace links

| Need | Look at |
|------|---------|
| How to write a spec | `docs/human-learning/SPEC_MAINTENANCE_GUIDE.md` |
| Re-ranking domain knowledge | `docs/human-learning/RERANKING_FUNDAMENTALS.md` |
| Agent workflow step-by-step | `docs/agentic-development/WORKFLOW_FOR_AGENTS.md` |
| Security policy | `.agent-config/security-boundaries.md` |
| Scoring rubric | `.agent-config/scoring/rubric.md` |
| Java 21 patterns for this project | `docs/human-learning/JAVA21_FOR_RERANKING.md` |
| Architecture decisions | `docs/architecture/` |
| Agent rules (detailed) | `.agent-config/AGENT_RULES.md` |

Spec: AGENTS-SPEC-v1
