# Acceptance Criteria: Standard Tokenizer

## Functional
- [ ] Tokenizer interface defined with single method
- [ ] StandardTokenizer splits on whitespace and punctuation
- [ ] Tokens are lowercase (Locale.ROOT)
- [ ] Default stopwords filtered
- [ ] Positions are sequential starting at 0 (post-filtering)
- [ ] Field name assigned to every token

## Edge Cases
- [ ] Null text throws IllegalArgumentException
- [ ] Empty/blank text returns empty list
- [ ] Only-stopwords text returns empty list
- [ ] Custom stopwords work
- [ ] No-stopwords mode works

## Code Quality
- [ ] Implements Tokenizer interface
- [ ] Thread-safe (final fields, no mutation)
- [ ] No external dependencies
- [ ] Javadoc on all public members

## Performance
- [ ] O(n) tokenization verified by test with large input
