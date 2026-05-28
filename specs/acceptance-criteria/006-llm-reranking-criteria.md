# Acceptance Criteria: LLM Re-Ranking

## Functional
- [ ] `LlmClient` is a `@FunctionalInterface` in `dev.reranker.fusion` with `String complete(String prompt)`
- [ ] `reranker-core` has NO new external dependencies (no HTTP client, no model SDK)
- [ ] `LlmReRanker` implements `FusionStrategy` and is query-aware
- [ ] `LlmReRankConfig` record with candidateCount (default 10) and mergeRankConstant (default 60), both rejecting values < 1
- [ ] LLM is called at most once per `fuse()` invocation
- [ ] Prompt includes the query text and the candidate documents' content
- [ ] Base ranking and LLM ranking are merged via RRF using mergeRankConstant
- [ ] Candidates outside the LLM response keep only their base-rank contribution

## Merge Correctness ("merge 2 results")
- [ ] A document ranked high by both base and LLM scores highest
- [ ] LLM agreement with base reinforces the top result
- [ ] LLM disagreement is balanced against base (not a full override)

## Graceful Degradation
- [ ] Empty LLM response → final ranking equals base ranking
- [ ] Garbage/unparseable response → final ranking equals base ranking
- [ ] Out-of-range candidate ids ignored
- [ ] Duplicate ids: first occurrence wins
- [ ] Parsing never throws on malformed output

## Edge Cases
- [ ] Empty documents → empty array, LLM NOT called
- [ ] candidateCount >= document count → all documents sent
- [ ] `LlmClient` exception propagates (not silently swallowed)
- [ ] Single document handled correctly

## Code Quality
- [ ] Uses Java records for config
- [ ] Thread-safe given a thread-safe client (immutable reranker state) — caveat documented
- [ ] Deterministic given a deterministic client
- [ ] All public methods have javadoc
- [ ] Reuses RRF reciprocal-contribution logic where practical

## Integration
- [ ] Works as a `.fusion()` strategy in the pipeline
- [ ] Existing FusionStrategy implementations (RRF, MinMax, ZScore) updated to the query-aware signature without behavior change
- [ ] All prior specs' tests still pass (no regression)
- [ ] Respects topK

## Tests
- [ ] All tests in test-requirements doc exist and pass
- [ ] Tests use a deterministic fake `LlmClient` (no network)
- [ ] Worked-example tests verify merge arithmetic
