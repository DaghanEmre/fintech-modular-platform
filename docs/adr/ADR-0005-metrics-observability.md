# ADR-0005: Metrics & Observability Strategy for Domain Rule Violations

## Status
ACCEPTED

## Date
2026-01-11

## Related ADRs
- [ADR-0001 (Hexagonal Architecture)](file:///home/daghanemre/fintech-modular-platform/docs/adr/ADR-0001-hexagonal-architecture.md)
- [ADR-0004 (Specification Pattern)](file:///home/daghanemre/fintech-modular-platform/docs/adr/ADR-0004-specification-pattern.md)

## Context
After adopting the Specification Pattern (ADR-0004), domain business rules are enforced declaratively inside aggregates and expressed via `SpecificationViolation` objects. 

While this improved domain purity and testability, it introduced a new requirement: Domain rule failures must be observable, measurable, and actionable in production.

Traditional exception-based logging is insufficient because:
- Business rule violations are expected outcomes, not system failures.
- Logs alone do not provide trend analysis.
- HTTP status codes lack semantic business meaning.

Therefore, a dedicated observability strategy is required to measure domain rule violations and correlate them with API operations while preserving domain purity.

## Decision
We adopt a **metrics-first observability strategy** for domain rule violations, based on Micrometer counters, enriched with domain semantics.

### Core Decisions
- **Domain rule violations are signals, not errors**: Each violation increments a counter instead of just logging a stack trace.
- **Violation codes are the primary observability dimension**: The `code` from `SpecificationViolation` is used as a metric tag.
- **Metrics collection is implemented at the adapter boundary**: Specifically in the `GlobalExceptionHandler`, ensuring no framework leakage into the domain.
- **Operation Tag Normalization**: The `operation` tag must be normalized (e.g., `POST /customers/{id}/activate`) to avoid high-cardinality issues and provide better correlation.

## Design

### Metrics Strategy
- **Metric Name**: `domain.violation.total`
- **Metric Type**: Counter
- **Tags**:
    - `code`: The unique identifier for the business rule violation (e.g., `CUSTOMER_DELETED`).
    - `operation`: The normalized API operation (e.g., `POST /customers/{id}/activate`).

### Implementation Point
Metrics are recorded in the `GlobalExceptionHandler` when handling `SpecificationException`.

```java
meterRegistry.counter("domain.violation.total",
    Tags.of("code", violation.code(),
            "operation", String.format("%s %s", request.getMethod(), normalizedPath)))
    .increment();
```

## Consequences

### Positive
- Clear separation between business friction and technical failures.
- Quantifiable insight into business rule failures (e.g., which rules are most frequently violated).
- Zero impact on domain purity.
- Cross-service consistency.

### Negative / Trade-offs
- dependency on Micrometer in the infrastructure layer.
- Requires discipline in naming violation codes.
- Metrics cardinality must be monitored (normalized paths are critical).

## Operational Guidelines
- **Violation Codes**: Must be stable, uppercase `SNAKE_CASE`, and represent business meaning.
- **Cardinality**: `operation` tag **must** be normalized. Do not include dynamic IDs or usernames in tags.

## References
- Eric Evans — Domain-Driven Design
- Micrometer Documentation
