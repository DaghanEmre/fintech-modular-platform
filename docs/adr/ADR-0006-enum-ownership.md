# ADR-0006: Enum Ownership & Cross-Service Compatibility

**Date:** 2026-01-24

**Status:** Accepted

---

## Context

In a microservices architecture with multiple bounded contexts, enum types pose a challenge:
- **Domain enums** (e.g., `CustomerStatus`) represent domain-specific state.
- **Contract enums** (e.g., `Currency`) represent shared technical/business concepts.
- **Serialization** across service boundaries requires a compatibility strategy.

Without clear guidelines, teams may move all enums to `fintech-common` (violates bounded context isolation) or use enums in API contracts (breaks compatibility).

## Decision

We adopt a **3-tier enum classification** based on ownership and usage scope:

### Tier 1: Domain-Internal Enums (NEVER SHARED)
- **Rule:** Domain enums belong to their aggregate and NEVER cross service boundaries as compiled types.
- **Ownership:** Specific microservice (e.g., `customer-service`).
- **Location:** `{service}/domain/model/`.
- **Serialization:** String (across boundaries).
- **Forward Compatibility:** Use `safeParse()` to tolerate unknown future values.

### Tier 2: Contract Enums (SHARED IN COMMON)
- **Rule:** Cross-cutting enums that MUST be interpreted identically across all services.
- **Ownership:** Platform (`fintech-common`).
- **Location:** `fintech-common/contract/`.
- **Evolution:** Requires coordinated deployment as changes are rare (e.g., ISO standards like `Currency`).

### Tier 3: Externalized Representation (STRING + VALIDATION)
- **Rule:** Domain enums are never exposed as Java enums in REST or Event contracts.
- **Pattern:** Internal enum + String serialization + Safe parsing.

## Naming Conventions
- **CORRECT:** `CustomerStatus`, `PaymentStatus`, `Currency`.
- **ANTI-PATTERN:** `CustomerStatusEnum`, `CustomerStatusDto`.

## Consequences
- **Positive:** Domain autonomy, service isolation, and forward compatibility.
- **Negative:** No compile-time validation at service boundaries; requires runtime parsing.
