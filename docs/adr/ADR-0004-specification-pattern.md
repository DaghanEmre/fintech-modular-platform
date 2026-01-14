# ADR-0004: Adoption of Specification Pattern for Domain Rule Enforcement

## Status
Accepted

## Date
2026-01-11

## Context
The customer-service domain contained multiple business rules implemented as imperative if-else chains inside aggregate methods and, in some cases, in application use cases.

This approach led to several architectural and operational issues:
- Business rules were scattered across aggregates and use cases
- Conditional logic became hard to read and reason about
- Reuse of rules across operations was not possible
- Testing individual business rules in isolation was difficult
- Mapping domain errors to HTTP responses and metrics was inconsistent
- Observability at the domain level was limited

As the platform evolves toward multiple microservices with complex domain rules, a declarative, composable, and reusable approach was required.

## Decision
We adopt the Specification Pattern (as described by Eric Evans and Martin Fowler) as the standard mechanism for expressing and enforcing domain business rules.

Key decisions:
- **Domain rules are modeled as Specifications**: Each rule is a standalone, composable object.
- **Specifications are pure, side-effect free, and testable in isolation**.
- **Composite specifications are built using logical operators**: AND, OR, NOT composition supported natively. Complex rules are expressed declaratively.
- **Rule violations are represented explicitly**: Violations are modeled as value objects (`SpecificationViolation`). Exceptions are used only as a transport mechanism (`SpecificationException`).
- **A shared common module is introduced**: `fintech-common` hosts framework-agnostic specification abstractions. Ensures consistency across all microservices.
- **Aggregates enforce rules internally**: Use cases orchestrate only. All business invariants live inside the domain model.
- **Unchecked Exceptions**: `SpecificationException` is intentionally unchecked to keep domain models free from infrastructural concerns and to avoid exception leakage into application flow control.

## Implementation

### Common Module (fintech-common)
Introduced a reusable specification infrastructure:
- `Specification<T>`: Core interface supporting `and()`, `or()`, `not()` composition.
- `SpecificationViolation`: Immutable value object (fields: `code`, `message`, `context`).
- `SpecificationException`: Runtime exception wrapping a violation.

This module has no framework dependencies and can be reused across services.

### Domain Integration (customer-service)

#### Aggregate Guard Pattern
Aggregates use a centralized guard method:
```java
private void ensure(Specification<Customer> specification) {
    if (!specification.isSatisfiedBy(this)) {
        throw new SpecificationException(specification.violation(this));
    }
}
```

#### Declarative Business Rules
Example: Customer activation
```java
public void activate() {
    ensure(CustomerSpecifications.canBeActivated());
    this.status = CustomerStatus.ACTIVE;
    touch();
}
```

#### Semantic Specifications
To avoid ambiguity in composite rules (especially OR), semantic domain-specific specifications are introduced when needed:
- `CustomerCanBeActivatedStatusSpec`: Returns explicit error codes such as `INVALID_STATUS_TRANSITION`, `CUSTOMER_ALREADY_ACTIVE`.

This prevents error swallowing and preserves domain intent.

### Error Handling & Observability

#### HTTP Mapping
Domain violations are mapped centrally via `SpecificationHttpStatusMapper`:
| Violation Code | HTTP Status |
| :--- | :--- |
| CUSTOMER_DELETED | 410 GONE |
| CUSTOMER_BLOCKED | 403 FORBIDDEN |
| INVALID_STATUS_TRANSITION | 409 CONFLICT |
| Default | 422 UNPROCESSABLE ENTITY |

#### Metrics
All specification violations are recorded using Micrometer:
- **Metric**: `domain.violation.total`
- **Tags**: 
    - `code`: violation code
    - `operation`: normalized request path (e.g., `/customers/{id}/activate`)

> [!NOTE]
> Operation is normalized to avoid high-cardinality metrics.

## Non-Goals: When NOT to Use Specification Pattern

The Specification Pattern is intentionally not applied universally. It is reserved for expressing domain-level business rules and transient invariants, not for all forms of validation or control flow.

The following cases are explicitly excluded from Specification Pattern usage:

### 1. Value Object Validation
Value objects (e.g., `CustomerId`, `Email`, `StateChangeReason`) are responsible for enforcing their own structural invariants at construction time using compact constructors and factory methods.
- **Rationale**: These are structural invariants, not business rules. They must be enforced unconditionally. Specifications are conditional and contextual by design.

### 2. Aggregate Creation Rules
Specifications should not be used for simple aggregate creation unless a complex, cross-field business rule exists. 
- **Example**: `Customer.create(email)` is a construction concern. Ensuring a valid `Email` is handled by the `Email` value object itself.
- **Rationale**: Creation is about building a valid initial state. Preconditions should be encoded in constructors or static factories.

### 3. Idempotency Checks
Idempotency is an aggregate responsibility.
- **Example**: In `activate()`, the check `if (this.status == ACTIVE) return;` remains inside the aggregate.
- **Rationale**: Idempotency defines behavior, not eligibility. Specifications answer: *"Can this operation be performed?"* Aggregates decide: *"What happens if it's already in this state?"*

### 4. State Mutation and Side Effects
Specifications must be pure functions and side-effect free. They must NEVER perform state changes, event publication, or field assignments.
- **Rationale**: Aggregate roots own mutation and consistency. Specifications only provide the "Yes/No" decision and the reason for failure.

### 5. Technical or Infrastructure Validation
Specifications are not used for null checks, authorization, API schema validation, or database existence checks. These belong to Controllers, Application Services, or Infrastructure layers.

### 6. Over-Specification of Simple Rules
Avoid creating specifications for trivial one-off checks that lack reuse or semantic depth (e.g., comparing two fields for equality in a single method).

## When Specification Pattern IS Appropriate

A specification SHOULD be used when:
1. A business rule governs whether an operation may proceed.
2. The rule depends on the current aggregate state or external domain data.
3. The rule produces a domain-specific violation code for observability.
4. The rule is reusable or composable across different use cases.

**Design Principle**: Specifications protect the aggregate; Aggregates change themselves.

## Consequences

### Positive
- Business rules are explicit, composable, and reusable.
- Aggregate methods are short and intention-revealing.
- Rule-level unit testing is straightforward.
- Domain → API error mapping is consistent.
- Observability at the domain level is significantly improved.
- Pattern can be reused across all microservices.

### Negative / Trade-offs
- Initial learning curve for developers unfamiliar with the pattern.
- Slight increase in number of classes.
- Requires discipline to avoid over-generic specifications.

## Alternatives Considered
1. **Keep if-else logic inside aggregates**: Rejected due to poor readability, reusability, and testability.
2. **Strategy Pattern**: Rejected as it models behavior variation rather than rule composition.
3. **Validation Frameworks (Bean Validation)**: Rejected because it is not expressive enough for complex domain invariants and tightly coupled to infrastructure concerns.

## References
- Eric Evans — Domain-Driven Design
- Eric Evans & Martin Fowler — Specification Pattern
- Martin Fowler — Replacing Conditional Logic with Polymorphism
- Joshua Kerievsky — Refactoring to Patterns

## Follow-ups
- Apply Specification Pattern to other domains (Payment, Account, Limits).
- Extend violation context usage for richer diagnostics.
- Generate OpenAPI error schemas from violation codes.
- Consider cross-service standardization of violation codes.
