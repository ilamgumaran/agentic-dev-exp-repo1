# Acceptance Criteria: Rank Fusion

## Functional
- [ ] `FusionStrategy` interface defined in `dev.reranker.fusion` with `fuse(List<Document>, List<List<Score>>)` returning `double[]`
- [ ] `ReciprocalRankFusion` implements the RRF formula `Σ 1/(k + rank)` with k default 60
- [ ] RRF assigns 1-based ranks per scorer, ties broken by document id ascending
- [ ] `RrfConfig` rejects rankConstant < 1
- [ ] `MinMaxNormalizedFusion` normalizes each scorer's scores to [0,1] then weighted-sums
- [ ] `ZScoreNormalizedFusion` normalizes by mean/stddev then weighted-sums
- [ ] Normalized fusions handle the zero-variance case (all equal scores → 0 contribution)
- [ ] Pipeline supports `.fusion(strategy)` as alternative to `.aggregator(agg)`
- [ ] Pipeline rejects both-set and neither-set at build() time

## Core Motivation Verified
- [ ] `test_minmax_handles_incomparable_scales` proves normalized fusion correctly handles BM25 + cosine scale mismatch (the document strong on both relative scales wins)
- [ ] `test_rrf_worked_example_consensus_wins` proves RRF rewards cross-scorer consensus

## Edge Cases
- [ ] RRF with single scorer preserves that scorer's order
- [ ] RRF with empty documents returns empty array
- [ ] MinMax with all-equal scores contributes zero
- [ ] ZScore with zero stddev contributes zero
- [ ] Wrong weight count throws IllegalArgumentException
- [ ] Pipeline with both aggregator and fusion throws IllegalStateException

## Code Quality
- [ ] Uses Java records for RrfConfig
- [ ] No external dependencies
- [ ] Thread-safe (immutable config, no shared state)
- [ ] All public methods have javadoc
- [ ] RRF complexity documented as O(S × D log D)
- [ ] Normalized fusion complexity documented as O(S × D)

## Integration
- [ ] Existing `Aggregator` path still works unchanged (no regression)
- [ ] All 4 existing specs' tests still pass
- [ ] Fusion produces RankedResult with component scores preserved
- [ ] Fusion respects topK truncation

## Tests
- [ ] All tests in test-requirements doc exist and pass
- [ ] Property-based tests verify RRF positivity and MinMax [0,1] bounds
