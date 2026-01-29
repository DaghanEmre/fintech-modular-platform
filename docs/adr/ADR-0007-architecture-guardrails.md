# ADR-0007: Architecture Guardrails & Static Analysis

**Date:** 2026-01-29

**Status:** Accepted

---

## Context

As the FinTech Modular Platform evolves, maintaining architectural integrity becomes critical:
- Team size may grow (onboarding new developers)
- Codebase complexity increases (multiple bounded contexts)
- Refactoring introduces risk of architectural drift
- ADRs document intent, but don't enforce compliance

Without automated enforcement, architectural violations can:
- Leak infrastructure concerns into domain
- Break hexagonal boundaries
- Violate DDD principles
- Erode specification pattern integrity
- Create tight coupling between layers

**Problem:** How do we prevent architectural erosion at scale?

**Decision:** We adopt **ArchUnit** for automated architecture testing.

---

## Decision

We adopt **ArchUnit** for automated architecture testing in customer-service.

### v4 Production-Grade Refinements
The initial implementation was refined in v4 to address critical DSL edge-cases and enterprise standards:
1. **DSL Logic Protection:** Mandatory use of `DescribedPredicate.or()` for multiple condition grouping to avoid ArchUnit DSL precedence bugs.
2. **Brittle Rule Removal:** Replaced regex-based inner class filtering with semantic `areTopLevelClasses()` checks.
3. **Hardened Naming:** Atomic specifications must match `.*(Is|Not).*Spec` strictly.
4. **Generalized Adapters:** All infrastructure implementations of domain ports are verified to end with `Adapter`.

---

## Rule Categories

### 1️⃣ Hexagonal Architecture Boundaries
- Domain must not depend on infrastructure or API.
- Application must not depend on infrastructure or API.
- **Infrastructure Isolation:** Infrastructure may only depend on Application and Domain (preventing API leakage).
- Layered architecture dependency direction (ADR-0001).

### 2️⃣ Domain-Driven Design Rules
- Value Objects must be immutable (final fields only).
- JPA entities must not reside in the domain package.
- Domain events must be immutable.

### 3️⃣ Specification Pattern Enforcement
- All concrete specifications must override `violation()` and return `SpecificationViolation`.
- Specifications must be **stateless** (only final fields).
- Specifications must reside in the `domain.specification` package.
- Atomic specs must follow `IsXSpec`/`NotXSpec` naming.

### 4️⃣ Enum Ownership & Naming
- Domain enums must reside in `domain.model`.
- Common module must not contain service-specific domain enums (common shield).
- Enums must not have `Enum` suffix.

---

## Hard Rules vs Soft Rules Classification

Not all architectural constraints can be enforced mechanically. We distinguish between:

### Hard Rules (ArchUnit Enforced)
Violations that can be detected via structure, signatures, or annotations.
- ✅ Layer isolation (no forbidden imports)
- ✅ Immutability (final fields)
- ✅ Contract compliance (method overrides)
- ✅ Naming conventions

### Soft Rules (Code Review Enforced)
Semantic rules that require understanding the *intent* of the code.
- ⚠️ **Use cases must not evaluate specifications:** ArchUnit can see the call, but distinguishing between a valid factory reference and an invalid evaluation requires manual review.
- ⚠️ **Aggregates must enforce invariants:** Business logic correctness remains a human concern.
- ⚠️ **Domain services must be stateless:** While we can check for final fields, full thread-safety and side-effect purity are soft rules.

---

## Future Enhancements (Backlog)

### 1. Atomic Specification Marker Interface
Use a marker interface (`AtomicSpecification<T>`) to distinguish atomic vs semantic specs more robustly than naming patterns.

### 2. Platform-Wide Architecture Suite
Replicate these guardrails across all microservices (Payment, Fraud, etc.) and establish platform-level checks in `fintech-common`.

---

## References
- [ArchUnit Documentation](https://www.archunit.org/)
- [ADR-0001: Hexagonal Architecture](./0001-use-hexagonal-architecture.md)
- [ADR-0004: Specification Pattern](./ADR-0004-specification-pattern.md)
- [ADR-0006: Enum Ownership](./ADR-0006-enum-ownership.md)
