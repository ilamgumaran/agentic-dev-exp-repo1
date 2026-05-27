# Workflow for Coding Agents

This document is written FOR coding agents. It describes exactly how to work in this repository.

## Before You Start

1. Read `CLAUDE.md` — it has your rules and build commands
2. Read `AGENTS.md` — it has your permissions and governance rules
3. Identify which spec you're implementing

## Implementing a Feature

### Step 1: Read the Spec

Read all three files for your feature:
- `specs/features/NNN-feature-name.md`
- `specs/test-requirements/NNN-feature-name-tests.md`
- `specs/acceptance-criteria/NNN-feature-name-criteria.md`

If anything is ambiguous, STOP. Ask the human.

### Step 2: Write Tests (RED phase)

Create the test class in the appropriate test package. Write every test listed in the test-requirements file. Each test should:
- Have the exact name from the requirements
- Set up inputs as described
- Call the method under test
- Assert the expected output

Run the tests. They must ALL FAIL. If any test passes, either:
- The feature is already implemented (check with human)
- Your test is wrong (it's asserting nothing)

### Step 3: Implement (GREEN phase)

Create or modify the implementation class. Write the minimum code needed to make all tests pass. Do not:
- Add features not in the spec
- Optimize before tests pass
- Add error handling not in the spec
- Create helper classes not needed by the spec

Run the tests. They must ALL PASS.

### Step 4: Refactor

Improve code quality without changing behavior:
- Extract methods if a method is >20 lines
- Rename variables for clarity
- Remove duplication
- Add javadoc to public APIs

Run the tests again. They must still ALL PASS.

### Step 5: Verify Acceptance Criteria

Go through the acceptance criteria checklist. For each item:
- If it's a functional criterion: verify the test covers it
- If it's a performance criterion: run a benchmark or add a test
- If it's a code quality criterion: inspect the code

### Step 6: Commit

```bash
git add <specific files>
git commit -m "feat(module): implement feature-name per spec NNN"
```

Commit message format: `<type>(<scope>): <description>`
- type: feat, fix, test, docs, refactor
- scope: model, tokenizer, scoring, engine, pipeline, ui
- description: what changed, referencing the spec number

## Common Patterns in This Codebase

### Creating a new type (model package)
```java
public record TypeName(Type1 field1, Type2 field2) {
    public TypeName {
        Objects.requireNonNull(field1, "field1 must not be null");
        // validation
        // defensive copies for collections
    }
}
```

### Implementing an interface (tokenizer, scoring packages)
```java
public final class ConcreteImpl implements TheInterface {
    private final Config config;

    public ConcreteImpl(Config config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public ReturnType method(ParamType param) {
        // implementation
    }
}
```

### Writing a test
```java
@Test
void test_descriptive_name() {
    // Arrange
    var input = createInput();

    // Act
    var result = objectUnderTest.method(input);

    // Assert
    assertEquals(expected, result);
}
```

## Build Commands

```bash
# From repo root
cd reranker-core && gradle test     # Java tests
cd reranker-ui && cargo test        # Rust tests
cd reranker-core && gradle build    # Full Java build
cd reranker-ui && cargo build       # Full Rust build
```

## What NOT to Do

- Do NOT modify files in `specs/`
- Do NOT add external dependencies to `reranker-core`
- Do NOT change `CLAUDE.md` or `AGENTS.md`
- Do NOT skip writing tests before implementation
- Do NOT add features beyond what the spec describes
- Do NOT use `System.out.println` for logging
- Do NOT return null — use Optional or throw
- Do NOT create mutable data objects — use records
