# Acceptance Criteria: Core Data Model

## Functional
- [ ] All types are Java records in package `dev.reranker.model`
- [ ] All types enforce non-null constraints via compact constructors
- [ ] Document makes a defensive copy of the fields map
- [ ] Query splits text into lowercase terms on whitespace
- [ ] Score rejects negative values
- [ ] RankedResult implements Comparable with score-descending, id-ascending tiebreak
- [ ] FieldWeight rejects negative weights

## Code Quality
- [ ] No external dependencies
- [ ] All public constructors/factories have javadoc
- [ ] No null returns anywhere — use Optional where needed
- [ ] Uses `Map.copyOf()` and `List.copyOf()` for immutability

## Tests
- [ ] All tests in test-requirements doc exist and pass
- [ ] Tests cover every edge case listed in the feature spec
- [ ] No test uses reflection or internal APIs
