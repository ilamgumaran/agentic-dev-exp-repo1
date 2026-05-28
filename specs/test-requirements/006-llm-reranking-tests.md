# Test Requirements: LLM Re-Ranking

## LlmClientTest

- test_llm_client_is_functional_interface (can be implemented as a lambda)

## LlmReRankConfigTest

- test_default_candidate_count_is_10
- test_default_merge_rank_constant_is_60
- test_custom_config_values
- test_candidate_count_below_one_throws
- test_merge_rank_constant_below_one_throws

## LlmReRankerTest

### Core behavior
- test_fuse_calls_llm_once_per_invocation
- test_fuse_passes_query_text_in_prompt
- test_fuse_includes_candidate_documents_in_prompt
- test_llm_reordering_changes_final_ranking
- test_merge_combines_base_and_llm_rankings
- test_llm_agreeing_with_base_reinforces_top_result

### Merge semantics
- test_document_ranked_high_by_both_wins
- test_base_only_documents_get_base_contribution_only
- test_merge_uses_configured_rank_constant

### Candidate selection
- test_only_top_candidates_sent_to_llm
- test_candidate_count_larger_than_docs_sends_all
- test_documents_outside_candidates_keep_base_rank

### Graceful degradation (parsing robustness)
- test_empty_llm_response_falls_back_to_base
- test_garbage_llm_response_falls_back_to_base
- test_llm_response_with_out_of_range_ids_ignored
- test_llm_response_with_duplicate_ids_first_wins
- test_llm_omitting_candidates_gives_base_contribution
- test_parsing_never_throws_on_malformed_output

### Edge cases
- test_empty_documents_returns_empty_without_calling_llm
- test_single_document
- test_llm_client_exception_propagates

### Determinism / thread safety
- test_deterministic_with_deterministic_client
- test_reranker_holds_only_immutable_state

## Worked Example Test Detail

`test_llm_reordering_changes_final_ranking`:
- 3 documents A, B, C with base scores giving base ranking: A(1), B(2), C(3)
- Fake LlmClient returns "3,2,1" (reverses: candidate 3=C first, 2=B, 1=A)
- So LLM ranking: C(1), B(2), A(3)
- Merge with k=60:
  - A: 1/(60+1) + 1/(60+3) = base rank 1 + llm rank 3
  - B: 1/(60+2) + 1/(60+2) = base rank 2 + llm rank 2
  - C: 1/(60+3) + 1/(60+1) = base rank 3 + llm rank 1
- A and C are symmetric → tie, broken by id (A before C); B is in the middle
- Assert the final scores reflect the merge (B's score is between, A==C by symmetry)

`test_llm_agreeing_with_base_reinforces_top_result`:
- Base ranking: A(1), B(2), C(3); LLM returns "1,2,3" (agrees)
- A gets 1/(61)+1/(61), highest; assert A is top with reinforced score

## Pipeline Integration Tests (in ReRankPipelineFusionTest or new test)

- test_pipeline_with_llm_reranker_end_to_end (using a fake LlmClient)
- test_pipeline_llm_reranker_respects_topk

## Fake LlmClient for Tests

Tests use a deterministic fake, e.g.:
```java
LlmClient fake = prompt -> "3,1,2";              // fixed ordering
LlmClient echo = prompt -> "";                   // empty -> base fallback
LlmClient capturing = prompt -> { captured = prompt; return "1,2,3"; };  // assert prompt content
LlmClient throwing = prompt -> { throw new RuntimeException("LLM down"); };
```
No network access in any test.
