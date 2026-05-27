# Self-Score — ReRanker Repository

**Scored**: May 2026
**Scorer**: Initial assessment during repo setup

## Axis A: Agentic Readiness

| Dimension | Score | Evidence |
|-----------|-------|----------|
| **A1. Agent Orientation** | **L4** | `AGENTS.md` follows AAIF spec with all required sections; `CLAUDE.md` present for Claude Code; `.agent-config/AGENT_RULES.md` for generic agents. Not yet CI-validated (would be L5). |
| **A2. Spec Clarity** | **L4** | Complete spec triplets in `specs/` (feature + test-requirements + acceptance-criteria). Templates in `docs/specs/`. 4 specs written. Not yet programmatically discoverable (would be L5). |
| **A3. Test Infrastructure** | **L4** | 29 passing tests (27 Java, 2 Rust). Single-command test execution. <5 second unit test runtime. No coverage reporting yet (would be L5). |
| **A4. Build Simplicity** | **L4** | Single-command build for both Java (`gradle build`) and Rust (`cargo build`). Dependencies managed by build tools. No containerized dev environment (would be L5). |
| **A5. Docs for Agents** | **L5** | Agent workflow guide, human learning guides, spec maintenance guide, ADRs, templates. Cross-referenced from AGENTS.md trace links table. |

**Agentic Readiness Level: L4** (floor of mean: (4+4+4+4+5)/5 = 4.2 → L4)

## Axis B: Security

| Dimension | Score | Evidence |
|-----------|-------|----------|
| **B1. Security Boundaries** | **L4** | Dedicated `security-boundaries.md` with explicit may/must-not rules. References OWASP Agentic Top 10. Least-Agency principle applied. Not yet enforced by tooling (would be L5). |
| **B2. Sensitive Surfaces** | **L3** | `specs/`, `AGENTS.md`, `CLAUDE.md`, `.agent-config/security-boundaries.md` identified as protected. No formal complete inventory yet. |
| **B3. Injection Defenses** | **L4** | `prompt-injection-defenses.md` with content classification, fencing rules, scope validation, and red-team scenarios. Not yet tested with automated red-team suite (would be L5). |
| **B4. Secret Handling** | **L3** | `.gitignore` excludes common secret patterns. No automated secret scanning (pre-commit hooks or CI). No secrets in repo (zero-dependency project). |
| **B5. Change Reversibility** | **L3** | Branch protection expected (PR-based workflow). Decision Spectrum documented. No tooling enforcement yet. |

**Security Level: L3** (floor of mean: (4+3+4+3+3)/5 = 3.4 → L3)

## Summary

**This repo is A4 / B3.**

### Strengths
- Comprehensive agent orientation (AGENTS.md + CLAUDE.md + generic rules)
- Full spec triplets for all planned features
- Detailed prompt injection defenses with red-team scenarios
- Well-documented TDD and code standards

### Improvement Priorities
1. **B4 → L4**: Add pre-commit secret scanning hooks
2. **B5 → L4**: Enable branch protection and required reviewers on GitHub
3. **B2 → L4**: Create formal sensitive surface inventory
4. **A3 → L5**: Add test coverage reporting
5. **A1 → L5**: Add CI validation of AGENTS.md format
