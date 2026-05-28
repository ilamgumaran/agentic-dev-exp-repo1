# Test Requirements: Rank Fusion

## FusionStrategyInterfaceTest

- test_fusion_strategy_returns_score_per_document

## RrfConfigTest

- test_default_rank_constant_is_60
- test_custom_rank_constant
- test_rank_constant_below_one_throws_exception

## ReciprocalRankFusionTest

- test_rrf_single_scorer_preserves_order
- test_rrf_two_scorers_basic_fusion
- test_rrf_worked_example_consensus_wins
- test_rrf_document_in_one_list_ranks_lower
- test_rrf_default_k_is_60
- test_rrf_custom_k_changes_weighting
- test_rrf_ties_broken_by_document_id
- test_rrf_empty_documents_returns_empty
- test_rrf_scores_are_descending_friendly
- test_rrf_is_deterministic
- test_rrf_is_thread_safe

### Worked Example Test Detail
`test_rrf_worked_example_consensus_wins` must verify the canonical example:
- Scorer 1 (BM25) ranks: A=1, B=2, C=3, D=4
- Scorer 2 (cosine) ranks: C=1, A=2, E=3, B=4
- Expected RRF scores (k=60):
  - A: 1/61 + 1/62 = 0.03252
  - C: 1/63 + 1/61 = 0.03226
  - B: 1/62 + 1/64 = 0.03175
  - E: 1/63 = 0.01587 (only in scorer 2's top; in our model E gets a real rank in scorer 1 too — adapt example so A>C>B ordering holds)
- Assert final order: A > C > B (the consensus property)

Note: In our model all documents are scored by all scorers, so adapt the worked example to use the actual scores that produce these ranks. The KEY assertion is that a document ranked well by BOTH scorers (A) beats a document ranked #1 by only ONE scorer (C).

## MinMaxNormalizedFusionTest

- test_minmax_normalizes_to_zero_one_range
- test_minmax_single_scorer
- test_minmax_two_scorers_weighted
- test_minmax_all_equal_scores_contribute_zero
- test_minmax_single_document_contributes_zero
- test_minmax_negative_scores_normalized
- test_minmax_wrong_weight_count_throws
- test_minmax_empty_documents_returns_empty
- test_minmax_handles_incomparable_scales
- test_minmax_is_thread_safe

### Incomparable Scales Test Detail
`test_minmax_handles_incomparable_scales` must verify the core motivation:
- Scorer 1 (BM25-like): scores [25.0, 10.0, 2.0] for docs A, B, C
- Scorer 2 (cosine-like): scores [0.3, 0.9, 0.5] for docs A, B, C
- With equal weights, after normalization, B should win (high on both relative scales)
- Without normalization (raw weighted sum), A would wrongly win due to BM25 magnitude
- Assert B's fused score > A's fused score

## ZScoreNormalizedFusionTest

- test_zscore_normalizes_by_mean_and_stddev
- test_zscore_single_scorer
- test_zscore_two_scorers_weighted
- test_zscore_zero_stddev_contributes_zero
- test_zscore_wrong_weight_count_throws
- test_zscore_empty_documents_returns_empty
- test_zscore_is_outlier_robust
- test_zscore_is_thread_safe

## ReRankPipelineFusionTest

- test_pipeline_with_fusion_strategy_builds
- test_pipeline_with_both_aggregator_and_fusion_throws
- test_pipeline_with_neither_aggregator_nor_fusion_throws
- test_pipeline_rrf_end_to_end
- test_pipeline_minmax_end_to_end
- test_pipeline_fusion_results_include_component_scores
- test_pipeline_fusion_respects_topk
- test_pipeline_fusion_results_sorted_descending

## Property-Based Tests

- RRF score is always positive for any document (since 1/(k+rank) > 0)
- RRF: a document ranked 1 by all scorers has the maximum possible RRF score
- MinMax: all normalized values are in [0, 1]
- Fusion output length always equals document count
