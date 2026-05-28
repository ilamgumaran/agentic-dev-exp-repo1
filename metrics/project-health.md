# Project Health Metrics — ReRanker

Operational metrics for this project, following the structure from `software-engineering-hio-agent-framework/metrics/operational-health.md`. Tailored for a library project (no service availability — replaced with build health).

---

## Current Values (as of 2026-05-28)

### Volume & Throughput
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Specs Completed | 7/7 (001-007) | 7/7 | Achieved |
| Merge Rate | 4 PRs merged | 3+/week | On Track |
| Agent vs Human Ratio | 6 specs agent-implemented | >50% agent | On Track |

### Build Health
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Java Build Success | 100% | >95% | On Track |
| Rust Build Success | 100% | >95% | On Track |
| Java Test Pass Rate | 187/187 (100%) | >95% | On Track |
| Rust Test Pass Rate | 2/2 (100%) | >95% | On Track |
| Build Duration | ~2s (Java, clean test) | <30s | On Track |

### Testability
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Test Count | 187 | grows with specs | On Track |
| Test Coverage (overall) | High (every public class has tests) | >80% | On Track |
| Test Flakiness | 0% | <2% | On Track |
| Test Suite Duration | <3s | <30s | On Track |
| TDD Compliance | 100% (tests written before impl) | 100% | On Track |

### Enhancement Velocity
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Spec-to-Merge Time | <1 day (specs 002-007) | <3 days | On Track |
| Agent Success Rate | 6/6 specs agent-implemented green | >70% | On Track |
| Rework Cycles | 1 (agent self-corrected a buggy test) | <2 | On Track |
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
