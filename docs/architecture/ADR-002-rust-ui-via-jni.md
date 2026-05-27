# ADR-002: Rust UI Communicates with Java via JNI

## Status: Accepted

## Context

We need a Rust-based UI that tests the Java re-ranking library. The options for cross-language communication are:

1. **JNI** — direct in-process calls
2. **gRPC/HTTP** — run Java as a server, Rust calls via network
3. **Subprocess + JSON** — run Java as a CLI, pipe JSON
4. **Shared memory** — memory-mapped files

## Decision

Use **JNI** (Java Native Interface) via the `jni` crate in Rust.

## Rationale

- JNI gives the lowest latency (no serialization, no network)
- We're measuring re-ranking performance, so the bridge must not add overhead
- The `jni` crate is mature and well-documented
- The FFI surface is small: one function (rank) with structured input/output

## Consequences

**Positive:**
- Microsecond-level call overhead
- No need to run a separate Java process
- Direct access to Java objects

**Negative:**
- JNI is complex and error-prone (memory management, exception handling)
- Requires JDK installed on the machine running the UI
- Platform-specific (JNI libraries are OS-dependent)

**Mitigations:**
- Keep the JNI surface minimal: serialize to/from JSON at the boundary
- Wrap all JNI calls in Rust error types using `thiserror`
- Provide a subprocess+JSON fallback for environments without JDK
