# Project Health Metrics — ReRanker

Operational metrics for this project, following the structure from `software-engineering-hio-agent-framework/metrics/operational-health.md`. Tailored for a library project (no service availability — replaced with build health).

---

## Current Values (as of 2026-05-27)

### Volume & Throughput
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Specs Completed | 1/4 | 4/4 by end of sprint | At Risk |
| Merge Rate | 2 PRs merged | 3+/week | On Track |
| Agent vs Human Ratio | 0% agent | >50% agent | Not Started |

### Build Health
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Java Build Success | 100% | >95% | On Track |
| Rust Build Success | 100% | >95% | On Track |
| Java Test Pass Rate | 27/27 (100%) | >95% | On Track |
| Rust Test Pass Rate | 2/2 (100%) | >95% | On Track |
| Build Duration | ~19s (Java) | <30s | On Track |

### Testability
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Test Coverage (model) | ~95% | >80% | On Track |
| Test Coverage (overall) | N/A | >80% | Not Measured |
| Test Flakiness | 0% | <2% | On Track |
| Test Suite Duration | <5s | <30s | On Track |
| TDD Compliance | 100% (initial) | 100% | On Track |

### Enhancement Velocity
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Spec-to-Merge Time | N/A | <3 days | Not Started |
| Agent Success Rate | N/A | >70% | Not Started |
| Rework Cycles | N/A | <2 | Not Started |
| Enhancement Ease Score | 4.0 (A2+A3+A4 avg) | >4.0 | On Track |

### Merge Acceptance
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| PR Review Turnaround | N/A | <4 hours | Not Started |
| PR Approval Rate | N/A | >60% | Not Started |
| Review Comment Density | N/A | Decreasing | Not Started |

### Ticket & Issue Health
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Open Tickets | 0 | Stable/decreasing | On Track |
| Average Ticket Age | 0 | <14 days | On Track |
| Bug Backlog | 0 | Decreasing | On Track |

---

## DORA (Adapted for Library)

| Metric | Current | Target |
|--------|---------|--------|
| Release Frequency | 0 (pre-release) | Weekly snapshot builds |
| Spec-to-Merge Lead Time | N/A | <3 days |
| Change Failure Rate | 0% | <5% |
| Recovery Time (broken build) | N/A | <1 hour |

---

## Agent-Readiness Score

| Axis | Current | Target |
|------|---------|--------|
| Agentic Readiness | A4 | A5 |
| Security | B3 | B4 |

See `.agent-config/scoring/self-score.md` for dimension breakdown.

---

## Review Cadence

- **Weekly**: Update Current Values table
- **Per Sprint**: Update DORA and agent metrics
- **Quarterly**: Full re-score of agent-readiness

## Thresholds

| Metric | Green | Yellow | Red |
|--------|-------|--------|-----|
| Build Success | >95% | 90-95% | <90% |
| Test Flakiness | <2% | 2-5% | >5% |
| Spec-to-Merge | <3d | 3-7d | >7d |
| Agent Success | >70% | 50-70% | <50% |
| Review Turnaround | <4h | 4-8h | >8h |
| Ticket Age | <14d | 14-30d | >30d |
