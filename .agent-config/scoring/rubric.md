# Agent-Readiness Scoring Rubric

Copied from the HIO framework's universal scoring mechanism. See `software-engineering-hio-agent-framework/research/directory-standards/AGENT_READINESS_SCORING.md` for the full rubric with improvement guidance.

## Quick Reference

### Axis A: Agentic Readiness

| Dim | Name | L1 | L3 | L5 |
|-----|------|----|----|-----|
| A1 | Agent Orientation | No agent file | AGENTS.md exists, incomplete | AGENTS.md + agent-specific configs, CI-validated |
| A2 | Spec Clarity | No specs | Specs exist, no test-reqs | Complete triplets, templated, versioned |
| A3 | Test Infrastructure | No tests | Test suite, documented command | Coverage + benchmarks + CI |
| A4 | Build Simplicity | No build system | Build documented, 2-3 steps | Single command, containerized |
| A5 | Docs for Agents | README only | Architecture + API docs | Agent guides + templates + cross-refs |

### Axis B: Security

| Dim | Name | L1 | L3 | L5 |
|-----|------|----|----|-----|
| B1 | Security Boundaries | No guidance | "Must not" rules listed | Enforced by tooling, tested |
| B2 | Sensitive Surfaces | No inventory | Inventory exists, incomplete | Drives access control |
| B3 | Injection Defenses | No awareness | Some fencing | Red-team tested |
| B4 | Secret Handling | Plaintext secrets | Secret scanning enabled | Zero-secret architecture |
| B5 | Change Reversibility | No protections | Branch protection + reviewers | Policy enforced + audited |

## Scoring

- Agentic Readiness Level = floor(mean(A1..A5))
- Security Level = floor(mean(B1..B5))
- Report as: **A{level} / B{level}**
