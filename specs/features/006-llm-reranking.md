# Feature: LLM Re-Ranking

## Status: READY

## Summary

Add an optional LLM-based re-ranking capability that takes a base ranking, asks a Large Language Model to re-rank the top candidates by relevance to the query, and merges the LLM's judgment with the base ranking. Implemented as a query-aware `FusionStrategy` so it composes with the existing pipeline. Keeps `reranker-core` zero-dependency by depending only on a user-supplied `LlmClient` functional interface — no HTTP client or model SDK in core.

## Motivation

Lexical (BM25) and semantic scorers are fast but shallow — they match tokens and vectors, not meaning-in-context. A cross-encoder or LLM re-ranker reads the query and each candidate together and produces far more calibrated relevance judgments. This is the standard high-quality final stage in production search and RAG pipelines (Cohere Rerank, cross-encoders).

The catch: LLM calls are expensive and require network access. So LLM re-ranking is:
1. **Optional** — only used when the user opts in
2. **Top-N only** — applied to a small candidate set from a cheaper base stage, not the full corpus
3. **A merge of two results** — combines the LLM ranking with the base ranking, so a single bad LLM call cannot completely override solid base signals

This "merge 2 results" design (base ranking + LLM ranking, fused via RRF) gives the quality lift of LLM judgment with robustness against LLM errors and hallucinations.

See `docs/human-learning/RANK_FUSION_AND_HYBRID_SEARCH.md` (Learning-to-Rank section) for background.

## Behavior

### Interface: LlmClient

A functional interface the user implements to call their LLM. Core provides NO implementation — this keeps `reranker-core` zero-dependency. The user wires in OpenAI, Anthropic, a local model, or a fake (for tests).

```java
package dev.reranker.fusion;

@FunctionalInterface
public interface LlmClient {
    /**
     * @param prompt the fully-constructed re-ranking prompt
     * @return the raw LLM completion text
     */
    String complete(String prompt);
}
```

### LlmReRanker (implements FusionStrategy)

A query-aware fusion strategy that wraps a base strategy and refines its top candidates with an LLM.

**Configuration**: `LlmReRankConfig` — a record
- `candidateCount` (int): how many top base-ranked documents to send to the LLM (default 10, must be >= 1)
- `mergeRankConstant` (int): the RRF rank constant used to merge base and LLM rankings (default 60, must be >= 1)

**Construction**:
- `LlmReRanker(LlmClient client, FusionStrategy base)` — default config
- `LlmReRanker(LlmClient client, FusionStrategy base, LlmReRankConfig config)` — custom config

The `base` strategy produces "result 1" (e.g., a `ReciprocalRankFusion` over BM25 + cosine). If the user has only one scorer and wants the LLM to refine raw scorer order, they can pass a base that is a single-scorer RRF.

**Algorithm** (`fuse(query, documents, scoresByScorer)`):
1. Compute base scores: `baseScores = base.fuse(query, documents, scoresByScorer)`.
2. Rank documents by `baseScores` descending (ties broken by document id ascending). This is the **base ranking** (result 1).
3. Take the top `candidateCount` documents from the base ranking as candidates.
4. Build a prompt (see Prompt Construction) containing the query and the candidate documents.
5. Call `client.complete(prompt)` → raw LLM response text.
6. Parse the response into an **LLM ranking** (result 2) — an ordered list of candidate document ids (see Response Parsing).
7. Merge the base ranking and the LLM ranking via RRF using `mergeRankConstant`:
   - For each document `d`: `final(d) = 1/(k + baseRank(d)) + 1/(k + llmRank(d))`
   - `baseRank(d)` is from step 2 (every document has one).
   - `llmRank(d)` is from step 6, but only candidates have an LLM rank. Documents not in the candidate set (or omitted by the LLM) receive only the base-rank term (the LLM term is omitted, contributing 0). This is the standard RRF treatment of missing entries.
8. Return the merged `double[]` scores in document order.

### Prompt Construction

The prompt must:
- State the query
- List each candidate with a stable identifier and its text content (concatenate the document's field values, or a configured subset)
- Ask the LLM to return the candidate identifiers ordered from most to least relevant
- Request a parseable format (one id per line, or comma-separated)

Example prompt template:
```
You are a search relevance expert. Given the query and the candidate documents,
rank the documents from MOST to LEAST relevant to the query.

Query: "{query.text}"

Candidates:
[1] {doc1 fields concatenated}
[2] {doc2 fields concatenated}
...

Return ONLY the candidate numbers in ranked order, most relevant first,
comma-separated. Example: 3,1,2
```

Use sequential candidate numbers ([1], [2], ...) mapped to document ids internally, rather than exposing raw document ids — this is more robust to parsing and avoids the LLM echoing arbitrary id strings.

### Response Parsing

- Extract integers from the response in order of appearance.
- Map each integer back to its candidate document (1-based index into the candidate list).
- The first valid integer found is LLM rank 1, the second is LLM rank 2, etc.
- Ignore integers outside the candidate range `[1, candidateCount]`.
- Ignore duplicate integers (keep first occurrence).
- Any candidate NOT mentioned in the response gets no LLM rank (only base contribution).
- Parsing must never throw on malformed LLM output — it degrades gracefully (a fully unparseable response means the result equals the base ranking).

## Edge Cases

- Empty documents: return empty `double[]` without calling the LLM.
- `candidateCount` >= document count: send all documents to the LLM.
- LLM returns empty/garbage/unparseable text: no LLM ranks applied; final ranking equals base ranking (graceful degradation).
- LLM returns ids out of range: ignored.
- LLM returns duplicates: first occurrence wins, rest ignored.
- LLM omits some candidates: omitted candidates get base-only contribution.
- Single document: base ranks it 1; LLM (if called) ranks it 1; merged normally.
- `LlmReRankConfig` with candidateCount < 1 or mergeRankConstant < 1: IllegalArgumentException.
- `LlmClient` throws: the exception propagates (the user's client is responsible for its own error handling/retries). Document this — do NOT silently swallow client exceptions, as that would hide outages.

## Constraints

- `reranker-core` stays zero-dependency: `LlmClient` is a functional interface, no HTTP/SDK in core.
- The LLM is called at most ONCE per `fuse()` invocation (batch all candidates in one prompt).
- Thread-safe IF the supplied `LlmClient` is thread-safe (document this caveat — `LlmReRanker` itself holds only immutable state).
- Deterministic given a deterministic `LlmClient` (enables testing with a fake client).
- Package: `dev.reranker.fusion`
- Reuses the RRF merge logic from `ReciprocalRankFusion` where practical (rank → reciprocal contribution).

## Pipeline Usage

```java
LlmClient myLlm = prompt -> myOpenAiCall(prompt);   // user-supplied

var pipeline = ReRankPipeline.builder()
    .tokenizer(new StandardTokenizer())
    .scorer(new BM25Scorer(tokenizer))
    .scorer(new CosineSimilarityScorer())
    .fusion(new LlmReRanker(
        myLlm,
        new ReciprocalRankFusion(),          // base = RRF of BM25 + cosine
        new LlmReRankConfig(10, 60)))         // top 10 to LLM, merge k=60
    .topK(10)
    .build();
```

## Non-Goals

- Built-in HTTP clients or model SDKs (user supplies `LlmClient`)
- Caching of LLM responses (user's `LlmClient` can cache)
- Streaming responses
- Multi-call strategies (pairwise/listwise tournaments) — single batched call only for v1
- Prompt template customization beyond field selection — may be added in a future spec revision
