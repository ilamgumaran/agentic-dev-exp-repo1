# Feature: Standard Tokenizer

## Status: READY

## Summary

Implement a standard tokenizer that splits text into lowercase tokens, removing punctuation and stopwords, producing positioned Token objects.

## Motivation

Tokenization is the first stage of the re-ranking pipeline. All scorers operate on tokens, not raw text. The standard tokenizer provides a sensible default that works for most document types.

## Behavior

### Interface: Tokenizer
- Method: `List<Token> tokenize(String text, String fieldName)`
- All tokenizer implementations must implement this interface
- Package: `dev.reranker.tokenizer`

### StandardTokenizer
- Splits on whitespace and punctuation (regex: `[\\s\\p{Punct}]+`)
- Converts all tokens to lowercase
- Filters out tokens that are blank after trimming
- Filters out stopwords (configurable set, default English stopwords)
- Assigns sequential positions starting from 0
- Assigns the provided fieldName to each token

### Default Stopwords
the, a, an, and, or, but, in, on, at, to, for, of, with, by, from, is, are, was, were, be, been, being, have, has, had, do, does, did, will, would, could, should, may, might, shall, can, this, that, these, those, it, its

### Configuration
- `StandardTokenizer()` — uses default stopwords
- `StandardTokenizer(Set<String> stopwords)` — custom stopwords
- `StandardTokenizer.withNoStopwords()` — no filtering

## Edge Cases
- Null text: throw IllegalArgumentException
- Empty/blank text: return empty list
- Text with only stopwords: return empty list
- Text with only punctuation: return empty list
- Single word: return list with one token at position 0
- Unicode text: process normally (lowercase using Locale.ROOT)

## Constraints
- No external dependencies
- O(n) where n = text length
- Thread-safe (immutable configuration, no shared mutable state)
- Package: `dev.reranker.tokenizer`
