# 3. Application Layer Responsibilities & Design

**Date:** 2025-01-03

**Status:** Accepted

## Context

The Domain Layer within the Customer Bounded Context is complete:

- Customer aggregate
- Email, CustomerId value objects
- Domain invariants and state transitions (activate, suspend, block, delete)
- 100% domain unit test coverage

The next steps require:

- Orchestration of domain behaviors based on use-cases
- Definition of transaction boundaries
- Isolation of the domain from infrastructure

These needs necessitate a clear definition of the Application Layer.

## Decision

The Application Layer will have the following responsibilities:

### 1️⃣ Use-case Orchestration

The Application Layer:

- Executes domain objects within the context of a use-case scenario, not directly.
- Can combine multiple domain behaviors into a single workflow.
- Models each use-case as a separate class.

**Example:**
```java
activateCustomer(customerId)
suspendCustomer(customerId, reason)
deleteCustomer(customerId)
```

### 2️⃣ Transaction Boundary Ownership

- The transaction boundary lies within the Application Layer.
- Domain objects are unaware of transactions.
- Transaction management is **declarative** and handled by the outer layer (e.g., `@Transactional` in adapters), not embedded in use-case logic.

This ensures:
- The Domain remains pure.
- Transaction management remains framework-dependent (Spring, Quarkus, Micronaut, etc.).
- Use-case logic remains independent of transactional annotations.

### 3️⃣ Repository Abstraction Usage

The Application Layer uses only **ports (interfaces)**:

```java
CustomerRepository customerRepository; // Port
```

- It does not know about concrete persistence implementations.
- Domain: Does not know about repositories and carries no persistence concerns.

### 4️⃣ No Business Rules in Application Layer

The Application Layer:
- ❌ Does not enforce rules.
- ❌ Does not define invariants.
- ❌ Does not duplicate validation.

All business rules:
- ✅ Live in the Domain Layer.
- ✅ Domain exceptions are propagated from the Application Layer.

### 5️⃣ No Infrastructure Leakage

The Application Layer does **not** know about:
- JPA Entities
- REST DTOs
- Framework annotations

This layer remains:
- Framework-agnostic
- Testable
- Pure Java

### 6️⃣ Error Handling Strategy

- Errors originating from the domain are **not masked**.
- The Application Layer handles domain exceptions by:
  - Logging them
  - Propagating them to the upper layer
- Exception mapping (HTTP 400/404/409):
  - Occurs **after** the Application Layer (in the adapter/controller layer).

### 7️⃣ Test Strategy

The Application Layer:
- Is tested with mocked **unit tests**.
- Repository ports are mocked.
- Domain logic is **not re-tested**.

Goal:
- ✅ Was the correct domain method called?
- ✅ Is the transaction flow correct?

---

## Package Structure

```
com.daghanemre.fintech.customer/
├── domain/
│   ├── model/           # Customer, Email, CustomerId
│   └── port/            # CustomerRepository (port)
├── application/
│   └── usecase/         # ActivateCustomerUseCase, etc.
└── infrastructure/
    └── persistence/     # JPA adapters
```

### Naming Convention

- **Preferred:** `ActivateCustomerUseCase`
- **Alternative:** `ActivateCustomerService`
- **Decision:** `UseCase` suffix (more explicit intent)

---

## Consequences

### Positive
- ✅ The Domain Layer remains completely isolated.
- ✅ Use-cases become readable and clear.
- ✅ Infrastructure changes cause minimum impact.
- ✅ The test strategy becomes clear per layer.

### Negative / Trade-offs
- ❌ Might seem "too layered" for simple CRUD scenarios.
- ❌ Creates more files and boilerplate initially.

This trade-off is consciously accepted for long-term **maintainability** and **scalability**.

---

## Alternatives Considered

### ❌ Fat Controller Approach
```java
@RestController
class CustomerController {
    // Domain calls inside Controller
    // Transaction + orchestration + validation in the same place
}
```

**Rejected:**
- Testability decreases.
- Domain leakage increases.

### ❌ Anemic Application Layer
```java
class CustomerService {
    // Just a repository pass-through
    return customerRepository.save(customer);
}
```

**Rejected:**
- The concept of use-case is lost.
- Domain behaviors become scattered.

---

## Future Considerations

The Application Layer is not a **"workflow engine"**.

When complex orchestration is needed:
- Domain Service or
- Saga / Process Manager

alternatives will be evaluated.

---

## PR Review Checklist

The following will be checked during Application Layer code reviews:

- [ ] Are there business rules in the Application Layer?
- [ ] Is there domain logic duplication?
- [ ] Has repository implementation leaked?
- [ ] Is the transaction boundary clear?
- [ ] Are domain exceptions masked?
- [ ] Do test mocks use domain ports?

---

## Summary (TL;DR)

The Application Layer:
- ✅ Operates on a use-case basis.
- ✅ Owns the transaction boundary.
- ✅ Calls the Domain, **does not manage** it.
- ✅ Is isolated from Infrastructure.
- ✅ Contains **no** business rules.

---

## References

- [ADR-0001: Hexagonal Architecture](./0001-use-hexagonal-architecture.md)
- [ADR-0002: Customer Domain Model Design](./0002-customer-domain-model-design.md)
- [Domain-Driven Design by Eric Evans - Application Layer](https://www.domainlanguage.com/ddd/)
- [Implementing Domain-Driven Design by Vaughn Vernon](https://vaughnvernon.com/)
