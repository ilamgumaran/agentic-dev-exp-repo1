# Agentic Development: Core Concepts

## 1. Specification-Driven Development (SDD)

### What It Is
Every code change begins with a human-written specification. The spec is a contract between the human (who decides *what*) and the agent (who decides *how*). No spec → no code.

### How It Differs from Requirements Documents
Traditional requirements docs are aspirational narratives written once and forgotten. Specs in SDD are:
- **Executable**: they directly produce tests
- **Atomic**: one spec = one feature = one PR
- **Living**: updated when requirements change (by humans only)
- **Machine-readable**: structured enough for an agent to parse unambiguously

### The Spec Lifecycle
```
DRAFT → READY → IN_PROGRESS → IMPLEMENTED → VERIFIED → DONE
  ↑                                              │
  └──────────────── REVISION ◄───────────────────┘
```

- **DRAFT**: Human is still writing it
- **READY**: Human has finalized; agent may begin
- **IN_PROGRESS**: Agent is implementing
- **IMPLEMENTED**: Agent believes it's done; tests pass
- **VERIFIED**: Human has reviewed and approved
- **DONE**: Merged to main
- **REVISION**: Human updates spec; cycle restarts

---

## 2. Test-Driven Development (TDD) in Agentic Context

### Why TDD Is Non-Negotiable for Agents

When a human writes code, they carry implicit understanding of the requirements in their head. When an agent writes code, it has only what's written down. TDD bridges this gap:

1. **Tests are the spec's shadow**: If the agent can write a correct test from the spec, it understood the requirement. If it can't, the spec is ambiguous.
2. **Tests prevent hallucination**: An agent might generate plausible-looking code that doesn't actually work. Tests catch this immediately.
3. **Tests enable fearless refactoring**: Agents don't know what they broke. Tests do.

### The RED-GREEN-REFACTOR Cycle for Agents

```
RED:      Agent writes test based on spec
          → Test MUST fail (proves it's testing something real)
          → If test passes immediately, it's testing nothing

GREEN:    Agent writes minimum code to pass the test
          → No cleverness, no optimization, no extras
          → Just make it work

REFACTOR: Agent improves structure without changing behavior
          → Tests must still pass
          → This is where clean code happens
```

### What "Test First" Means Concretely

The agent creates a test file BEFORE the implementation file exists. The test imports a class that doesn't exist yet. The test calls methods that aren't written yet. The test compiles (the class must exist as a stub) but fails (the methods return wrong values or throw).

This is not a formality. It is the mechanism that prevents agents from writing code that "looks right" but doesn't match the spec.

---

## 3. Human-in-Orchestration (HIO)

### The Spectrum of Human Involvement

```
Full automation ◄─────────────────────────► Full manual
     ↑                    ↑                      ↑
  Dangerous         HIO (sweet spot)        No leverage
```

- **Full automation**: Agent does everything. No human oversight. This produces codebases that diverge from intent, accumulate technical debt invisibly, and become unmaintainable.
- **Full manual**: Human does everything. No AI leverage. This is the traditional model and it works, but it doesn't scale.
- **HIO**: Human controls the *what* and *why*. Agent handles the *how*. Human reviews the output. This gives you 5-10x throughput while maintaining architectural coherence.

### What Humans Own

| Domain | Examples |
|--------|----------|
| Architecture | System design, module boundaries, dependency choices |
| Specifications | Feature specs, acceptance criteria, test requirements |
| Quality gates | Review standards, performance budgets, security rules |
| Prioritization | What to build next, what to defer |
| Knowledge | Why decisions were made (captured in ADRs) |

### What Agents Own

| Domain | Examples |
|--------|----------|
| Implementation | Writing code that matches specs |
| Testing | Writing tests from test requirements |
| Refactoring | Improving code structure within constraints |
| Build management | Running builds, fixing compilation errors |
| Boilerplate | Generating repetitive but necessary code |

---

## 4. The Spec Stack

A complete spec consists of three files:

### Feature Spec (`specs/features/`)
Describes WHAT the feature does, WHY it exists, and WHAT the inputs/outputs are. Written in plain language with concrete examples.

### Test Requirements (`specs/test-requirements/`)
Lists every test that must exist. Each test is a single assertion about behavior. The agent writes the test code; the human defines what to test.

### Acceptance Criteria (`specs/acceptance-criteria/`)
Defines HOW to verify the feature is complete. These are the conditions a human reviewer checks during PR review.

### Example: How They Work Together

**Feature spec** says: "BM25 scoring must handle multi-field documents by computing per-field scores and combining them with field weights."

**Test requirement** says: "test_multi_field_weighted_scoring: A document with title='blue shoes' and description='comfortable running shoes' scored against query='shoes' with title_weight=2.0 and description_weight=1.0 must score higher than the same document with equal weights."

**Acceptance criteria** says: "Multi-field scoring is complete when: (1) all multi-field tests pass, (2) field weights are configurable, (3) adding a field with weight 0.0 has no effect on score, (4) benchmark shows <1ms overhead per additional field."

---

## 5. Agent Memory and Statefulness

### Agents Are Stateless

A coding agent does not remember previous sessions. Every invocation starts fresh. It reads CLAUDE.md, reads the spec, and works. This has profound implications:

1. **Everything must be written down**: If it's not in a file, the agent doesn't know it
2. **CLAUDE.md is your institutional memory**: Rules, conventions, patterns — all captured here
3. **Specs are self-contained**: A spec cannot say "as discussed yesterday" — it must include all context
4. **ADRs capture decisions**: Why you chose BM25 over TF-IDF, why you avoided external dependencies — these go in architecture decision records

### Why Statelessness Is a Feature

- **Reproducibility**: Any agent, any time, same spec → same result
- **No tribal knowledge**: The project doesn't depend on one agent's "experience"
- **Fresh perspective**: Each session re-evaluates the spec without bias from previous attempts
- **Parallelism**: Multiple agents can work on different specs simultaneously without coordination

---

## 6. Quality Gates and Review

### The Three Levels of Verification

**Level 1: Automated (Agent-Run)**
- Compilation succeeds
- All tests pass
- No lint/style warnings
- Coverage meets threshold

**Level 2: Automated (CI-Run)**
- All Level 1 checks on clean environment
- Integration tests pass
- Benchmarks show no regression
- Security scanning (dependency audit, static analysis)

**Level 3: Human Review**
- Implementation matches spec intent (not just letter)
- Design fits the broader architecture
- Performance characteristics are acceptable
- No subtle correctness issues the tests missed
- Acceptance criteria met

### What to Look for in Agent-Written Code

1. **Spec fidelity**: Does it do what the spec says? Not more, not less.
2. **Edge case handling**: Did the agent handle the edge cases listed in the spec?
3. **Naming**: Are types and methods named consistently with the rest of the codebase?
4. **Complexity**: Did the agent over-engineer? (Agents tend to add abstractions)
5. **Performance**: Is the approach O(n) when the spec says it must be? Did the agent use the right data structure?

---

## 7. Scaling Agentic Development

### One Engineer, Multiple Agents

An experienced engineer can manage 3-5 concurrent agent tasks:
- Write a spec (10-20 minutes)
- Agent implements (5-30 minutes)
- Review the PR (5-15 minutes)
- Pipeline: while one PR is in review, another agent is implementing

### Team of Engineers, Fleet of Agents

- Each engineer owns a set of specs
- Agents are interchangeable — any agent can implement any spec
- CLAUDE.md ensures consistency across all agents
- Specs prevent conflicts because each spec touches defined files/modules

### When to NOT Use an Agent

- Exploratory prototyping (you don't know what you want yet)
- Security-critical cryptographic code (use audited libraries instead)
- One-off scripts (faster to write yourself)
- Deep debugging of production incidents (agents lack production context)
