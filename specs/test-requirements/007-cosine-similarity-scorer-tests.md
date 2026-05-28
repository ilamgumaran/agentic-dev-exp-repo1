# Test Requirements: Cosine Similarity Scorer

## NGramConfigTest
- test_default_n_is_3
- test_custom_n
- test_n_below_one_throws

## CosineSimilarityScorerTest
- test_identical_text_scores_one
- test_no_shared_ngrams_scores_zero
- test_partial_overlap_scores_between_zero_and_one
- test_morphological_match_running_runner
- test_scores_are_bounded_zero_to_one (property-based)
- test_empty_document_fields_scores_zero
- test_scores_returned_in_document_order
- test_multi_field_document_concatenated
- test_custom_ngram_size
- test_single_character_query_does_not_throw
- test_case_insensitive
- test_deterministic_same_input_same_output
- test_thread_safe_concurrent_scoring

## Property-Based Tests
- Cosine score is always in [0.0, 1.0]
- Cosine of identical vectors is 1.0
- Cosine is symmetric (not directly testable via Scorer, but vector helper should be)
