# Agent Rules — ReRanker

This file contains detailed rules for any coding agent working in this repository. It is agent-agnostic — the same rules apply whether you are Claude Code, Codex, Copilot, Gemini, Q Developer, Cursor, or any other agent.

Agent-specific configuration lives in their respective files:
- Claude Code: `CLAUDE.md` + `.claude/settings.json`
- Copilot: `.github/copilot-instructions.md` (if added)
- Gemini: `GEMINI.md` (if added)
- Q Developer: `.amazonq/rules/` (if added)

## Project Identity

**Name**: ReRanker
**Purpose**: Zero-dependency Java 21 library for re-ranking documents by relevance
**Languages**: Java 21 (core library), Rust (UI/test harness)
**Methodology**: Spec-Driven TDD under HIO (Human-in-Orchestration)

## Directory Layout

```
reranker-core/           Java library (Gradle build)
  src/main/java/         Production code
  src/test/java/         Test code
reranker-ui/             Rust TUI (Cargo build)
  src/                   Rust source
specs/                   Human-written specifications (READ-ONLY)
  features/              Feature behavior specs
  test-requirements/     Required test cases
  acceptance-criteria/   Verification checklists
docs/                    Documentation
  agentic-development/   Agent workflow docs
  human-learning/        Domain and technology guides
  architecture/          Architecture Decision Records
  specs/                 Spec templates
.agent-config/           Generic agent configuration
  scoring/               Agent-readiness scores
```

## Build and Test

```bash
# Java — compile
cd reranker-core && gradle build

# Java — test
cd reranker-core && gradle test

# Rust — compile
cd reranker-ui && cargo build

# Rust — test
cd reranker-ui && cargo test

# Full pipeline
gradle -p reranker-core test && (cd reranker-ui && cargo test)

# Rust lint
cd reranker-ui && cargo clippy -- -D warnings
```

## Spec Protocol

1. **Read the spec first**: Before implementing any feature, read all three files:
   - `specs/features/NNN-feature-name.md`
   - `specs/test-requirements/NNN-feature-name-tests.md`
   - `specs/acceptance-criteria/NNN-feature-name-criteria.md`
2. **Specs are read-only**: Never create, modify, or delete files under `specs/`.
3. **Ambiguity = stop**: If a spec is unclear or contradictory, stop and ask the human.
4. **Match acceptance criteria**: Every item in the acceptance criteria must be satisfied.
5. **No scope creep**: Implement only what the spec describes. No extras.

## TDD Protocol (Strict — Non-Negotiable)

```
1. READ spec and test-requirements
2. WRITE test class with all test methods → tests MUST FAIL (RED)
   - If any test passes before implementation, the test is wrong
3. IMPLEMENT minimum code to make tests pass (GREEN)
   - No cleverness, no optimization — just make it work
4. REFACTOR for quality without changing behavior
   - Tests must still pass after refactoring
5. VERIFY against acceptance criteria
6. COMMIT
```

Never skip step 2. Tests MUST exist before implementation code.

## Code Standards — Java

- **Java 21 features required**: records, sealed interfaces, pattern matching, virtual threads
- **No null returns**: Use `Optional<T>` or throw meaningful exceptions
- **Immutable data**: Use records with defensive copies for collections
- **Javadoc**: All public methods have `@param` and `@return` (one line each)
- **Package structure**: `dev.reranker.<module>` — mirrors the module it belongs to
- **Logging**: No `System.out.println` — use `java.util.logging`
- **Complexity**: Benchmark-sensitive code must document its O(n) complexity
- **Dependencies**: ZERO external runtime dependencies for `reranker-core`
- **Compiler**: Stable Java 21 only — no `--enable-preview`

## Code Standards — Rust

- **Edition**: 2024
- **Errors**: `thiserror` for library errors, `anyhow` for application errors
- **Docs**: All public functions documented with `///` doc comments
- **Lint**: `cargo clippy` must pass with no warnings
- **FFI**: The Rust layer calls Java via JNI — keep the boundary minimal

## Git Conventions

- **Commit format**: `<type>(<scope>): <description>`
  - Types: `feat`, `fix`, `test`, `docs`, `refactor`
  - Scopes: `model`, `tokenizer`, `scoring`, `engine`, `pipeline`, `ui`, `docs`
- **One logical change per commit**
- **Never force push**
- **Never push to `main`**

## Architecture Constraints

- Zero external runtime dependencies for `reranker-core`
- Tokenization, scoring, and semantic matching are separate modules behind interfaces
- Pipeline is composable — users wire together stages
- Latency budget: <10ms for token matching on 10K documents (single thread)
- CPU budget: single-threaded by default, opt-in parallelism via virtual threads
- All data types are immutable Java records
- Scorer interface is open (users can add implementations)

## What Agents Must Not Do

1. Modify files under `specs/` — human-authored, agent-read-only
2. Modify `AGENTS.md`, `CLAUDE.md`, or `.agent-config/security-boundaries.md`
3. Add runtime dependencies to `reranker-core`
4. Change public API signatures without a spec
5. Skip writing tests before implementation
6. Use `System.out.println`
7. Return null from any public method
8. Create mutable data objects
9. Force push or push to `main`
10. Commit secrets, credentials, or API keys
11. Auto-merge PRs
12. Bypass CI quality gates
