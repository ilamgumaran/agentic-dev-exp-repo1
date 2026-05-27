# Feature: Core Data Model

## Status: READY

## Summary

Define the foundational data types used throughout the re-ranking library: Document, Query, Token, Score, RankedResult, and FieldWeight.

## Motivation

Every module in the library needs a shared vocabulary of types. These types must be immutable, lightweight, and use Java 21 records. Defining them first enables all other modules to be developed in parallel.

## Behavior

### Document
- Represents a document to be ranked
- Has an `id` (String, non-null, non-blank) and `fields` (Map<String, String>, non-null, immutable)
- Fields represent named text properties (e.g., "title", "description", "category")
- Factory method: `Document.of(String id, Map<String, String> fields)`
- Defensive copy of the fields map on construction

### Query
- Represents a search query
- Has `text` (String, non-null, non-blank) — the raw query string
- Has `terms` (List<String>) — the query split into individual terms (lowercase, trimmed)
- Terms are computed eagerly on construction by splitting on whitespace
- Factory method: `Query.of(String text)`

### Token
- Represents a parsed token from text
- Has `value` (String) — the token text (lowercase)
- Has `position` (int) — zero-based position in the source text
- Has `field` (String) — which document field this token came from

### Score
- Represents a relevance score from a single scorer
- Has `scorerName` (String) — identifies which scorer produced this
- Has `value` (double) — the score value (must be non-negative)
- Construction must reject negative values with IllegalArgumentException

### RankedResult
- Represents a document with its final aggregated score
- Has `document` (Document) — the original document
- Has `score` (double) — the final combined score
- Has `componentScores` (List<Score>) — individual scores from each scorer
- Implements Comparable<RankedResult> — natural ordering is by score descending

### FieldWeight
- Represents the weight assigned to a document field during scoring
- Has `fieldName` (String) and `weight` (double, must be >= 0.0)
- Factory method: `FieldWeight.of(String fieldName, double weight)`

## Edge Cases
- Document with empty fields map: valid (represents a document with no text)
- Query with single character: valid
- Query with multiple spaces between terms: collapsed to single terms, empty strings filtered out
- Score value of 0.0: valid
- Score value of negative: IllegalArgumentException
- RankedResult comparison with equal scores: break tie by document ID ascending
- FieldWeight with weight 0.0: valid (means this field is ignored)

## Constraints
- All types are Java records
- All types are immutable
- No external dependencies
- Package: `dev.reranker.model`
