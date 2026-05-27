# AGENTS.md — Multi-Agent Governance

## Repository Role in the Ecosystem

This repository is **Layer 4 (Operational)** in the HIO framework stack:
- Layer 1: ThoughtExperiments (cognitive foundation)
- Layer 2: software-engineer-core-structure (org structure)
- Layer 3: software-engineering-hio-agent-framework (agent framework)
- Layer 4: **This repo** (working product built by agents under HIO)

## Agent Types and Permissions

### Code Co-Creator Agent
- **Can**: Read specs, write code, write tests, run builds, create commits
- **Cannot**: Modify specs, change architecture docs, alter CLAUDE.md/AGENTS.md
- **Must**: Follow TDD protocol, match acceptance criteria

### Quality Analyst Agent
- **Can**: Read all code, run tests, run benchmarks, file issues
- **Cannot**: Modify production code directly
- **Must**: Validate against spec acceptance criteria

### Architecture Explorer Agent
- **Can**: Read all files, propose ADRs (as drafts)
- **Cannot**: Modify code, finalize ADRs (human approval required)
- **Must**: Reference existing specs when proposing changes

## Workflow

```
Human writes spec → Agent reads spec → Agent writes tests (RED)
→ Agent implements (GREEN) → Agent refactors → Human reviews PR
→ Human approves or requests changes → Merge
```

## Cross-Repository References

- Specs methodology: see `software-engineering-hio-agent-framework/templates/`
- Role definitions: see `software-engineer-core-structure/roles/`
- Relevancy domain knowledge: see `relevancyengineer/domains/`

## Guardrails

1. No agent may introduce a new external dependency without human approval
2. No agent may change the public API surface without a spec
3. Performance regressions (>20% on benchmarks) block merge
4. All code must compile with `--enable-preview` disabled (stable Java 21 only)
