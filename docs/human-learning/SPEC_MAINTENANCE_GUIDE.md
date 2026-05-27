# Spec Maintenance Guide

## For Humans: How to Write and Maintain Specs

This guide teaches you how to write specifications that coding agents can reliably implement, and how to maintain them as the project evolves.

## The Spec Is the Product

In agentic development, your specs ARE your product. The code is a derivation. If the specs are precise, the code will be correct. If the specs are vague, the code will be wrong in unpredictable ways.

Think of specs as the source code of your project's intent.

## Anatomy of a Good Spec

### Feature Spec Template

```markdown
# Feature: [Name]

## Status: [DRAFT | READY | IN_PROGRESS | IMPLEMENTED | VERIFIED | DONE]

## Summary
One sentence describing what this feature does.

## Motivation
Why this feature exists. What problem it solves. What happens without it.

## Behavior

### Input
- Type and structure of inputs
- Valid ranges and constraints
- Concrete examples

### Output
- Type and structure of outputs
- How output relates to input
- Concrete examples with expected values

### Algorithm (if applicable)
Describe the approach the implementation should take.
Include the formula, pseudocode, or step-by-step process.
Be precise — agents follow instructions literally.

## Edge Cases
List every edge case explicitly:
- Empty input
- Single element
- Maximum size
- Invalid data
- Concurrent access

## Constraints
- Performance: time and space complexity
- Dependencies: what can/cannot be used
- Thread safety requirements
- Memory budget

## Non-Goals
What this feature explicitly does NOT do.
(Agents will try to be helpful and add extras — prevent this.)
```

### Common Spec Mistakes

| Mistake | Example | Fix |
|---------|---------|-----|
| Vague behavior | "Sort results by relevance" | "Sort by BM25 score descending, break ties by document ID ascending" |
| Missing edge case | No mention of empty input | "Empty query returns empty list with no error" |
| Implicit knowledge | "Use the standard approach" | Spell out the exact algorithm |
| Over-specification | "Use a HashMap on line 42" | Specify behavior, not implementation details |
| Missing examples | Just prose, no data | Include input/output pairs with concrete values |
| Unbounded scope | "Handle all document types" | "Handle documents with String-typed field values" |

## Writing Test Requirements

### Every Behavior Gets a Test

Map each behavior in the spec to at least one test:

```
Spec says: "BM25 score increases with term frequency"
Test: test_higher_term_frequency_gives_higher_score

Spec says: "Empty query returns score 0.0"
Test: test_empty_query_returns_zero_score

Spec says: "Thread-safe for concurrent reads"
Test: test_concurrent_scoring_produces_consistent_results
```

### Test Naming Convention

```
test_<what>_<condition>_<expected>
```

Examples:
- `test_score_with_empty_query_returns_zero`
- `test_score_with_matching_term_is_positive`
- `test_pipeline_with_no_scorers_throws_exception`

### Property-Based Tests

Some behaviors are better expressed as properties:
- "Score is always non-negative" → fuzz with random inputs
- "Adding a relevant term never decreases score" → test with subset/superset queries
- "Ranking is deterministic" → same input always gives same output

## Writing Acceptance Criteria

Acceptance criteria are what the human reviewer checks. They're the bridge between "tests pass" and "feature is done."

### Format

```markdown
# Acceptance Criteria: [Feature Name]

## Functional
- [ ] [Observable behavior 1]
- [ ] [Observable behavior 2]

## Performance
- [ ] [Benchmark result within budget]

## Code Quality
- [ ] [Standards compliance]

## Integration
- [ ] [Works with existing features]
```

### Example

```markdown
# Acceptance Criteria: BM25 Scoring

## Functional
- [ ] Single-term queries produce correct BM25 scores (verified against reference implementation)
- [ ] Multi-term queries sum per-term scores
- [ ] Custom k1 and b parameters change scores as expected
- [ ] Empty query/document edge cases handled without exceptions

## Performance
- [ ] 10,000 single-field documents scored in <10ms (single thread)
- [ ] Memory allocation <1KB per scoring call (no unnecessary object creation)

## Code Quality
- [ ] Uses Java records for immutable config
- [ ] No null returns — Optional or exception
- [ ] All public methods have javadoc

## Integration
- [ ] Works as a Scorer in the pipeline
- [ ] Composable with other scorers via Aggregator
```

## Maintaining Specs Over Time

### When to Update a Spec

1. **Requirements change**: Business needs evolve. Update the spec, set status to REVISION.
2. **Edge case discovered**: Found a case the spec didn't cover. Add it.
3. **Performance budget changes**: Constraints shift. Update the numbers.
4. **API surface changes**: A new feature requires modifying an existing feature's interface.

### When NOT to Update a Spec

1. **Agent made a different design choice**: If it meets acceptance criteria, it's fine. Don't retrofit the spec to match the code.
2. **Refactoring**: Internal changes that don't affect behavior don't need spec changes.
3. **Bug fixes**: Unless the spec was wrong, the spec doesn't change for bugs.

### Version Control for Specs

- Specs live in git, same as code
- Spec changes require a separate commit from code changes
- Commit message: `spec(<scope>): <description>`
- Example: `spec(bm25): add multi-field scoring edge cases`

## For Coding Agents: How to Use Specs

### Reading a Spec

1. Read the feature spec completely before doing anything
2. Read the test requirements file
3. Read the acceptance criteria
4. If anything is ambiguous, STOP and ask the human

### Implementing from a Spec

1. Create the test file first
2. Write every test listed in test-requirements
3. Run tests — they must all fail (RED)
4. Implement the code — minimum to make tests pass (GREEN)
5. Refactor — clean up without breaking tests (REFACTOR)
6. Run the full test suite — nothing else should break
7. Check your work against acceptance criteria

### What to Do When the Spec Is Incomplete

- If a method's return type isn't specified: use the most restrictive type that works
- If an edge case isn't listed: handle it conservatively (throw, return empty, return zero)
- If performance constraints aren't given: optimize for readability, not speed
- If you're unsure about anything: ASK. Don't guess. Wrong guesses create bugs that pass tests but violate intent.
