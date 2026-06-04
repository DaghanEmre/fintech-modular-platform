---
title: Architecture Handbook
updated: 2026-05-13
version: 2.1
maintainer: Daghan Emre
status: Active / Refactored
---

# FinTech Modular Platform - Architecture Knowledge Base

## 📋 TABLE OF CONTENTS

- [1. PROJECT OVERVIEW](#1-project-overview)
- [2. CORE VISION & MINDSET](#2-core-vision--mindset)
- [3. ARCHITECTURAL FRAMEWORK](#3-architectural-framework)
    - [3.1 HEXAGONAL ARCHITECTURE (ADR-0001)](#31-hexagonal-architecture-adr-0001)
    - [3.2 MICROSERVICE DECOMPOSITION](#32-microservice-decomposition)
    - [3.3 COMMUNICATION PATTERNS](#33-communication-patterns)
- [4. DOMAIN-DRIVEN DESIGN RULES](#4-domain-driven-design-rules)
    - [4.1 DOMAIN LAYER ISOLATION](#41-domain-layer-isolation)
    - [4.2 AGGREGATE DESIGN (ADR-0002)](#42-aggregate-design-adr-0002)
    - [4.3 SPECIFICATION PATTERN (ADR-0004)](#43-specification-pattern-adr-0004)
    - [4.4 APPLICATION LAYER DESIGN (ADR-0003)](#44-application-layer-design-adr-0003)
- [5. CODING PRINCIPLES & STANDARDS](#5-coding-principles--standards)
- [6. CODE REVIEW CHECKLISTS](#6-code-review-checklists)
- [7. TECHNOLOGY STACK](#7-technology-stack)
- [8. CURRENT IMPLEMENTATION STATUS](#8-current-implementation-status)
- [9. DEVELOPMENT ROADMAP DETAIL](#9-development-roadmap-detail)
- [10. ARCHITECTURE DECISION RECORDS (ADRs) SUMMARY](#10-architecture-decision-records-adrs-summary)
- [11. WORKING WITH THIS KNOWLEDGE BASE](#11-working-with-this-knowledge-base)
- [KNOWN OPEN DECISIONS](#known-open-decisions)
- [DOCUMENT GOVERNANCE](#document-governance)
- [STATUS LEGEND](#status-legend)
- [REFERENCE MATERIALS](#reference-materials)

---

## Purpose

This document is a **curated architecture knowledge base** for the FinTech Modular Platform.

It summarizes:
- Accepted architectural decisions (via ADRs)
- Current implementation status (verified through code structure)
- Domain modeling rules and patterns
- Coding and review standards
- Near-term roadmap priorities

**Authority of Truth:**
- **ADRs** remain the source of truth for architectural decisions
- **Codebase and project structure** remain the source of truth for implementation state
- **This knowledge base** is a living index that must be updated when ADRs, implementation, or roadmap priorities change

**Conflict Resolution:**
- If this knowledge base introduces a rule not present in an ADR, it should be treated as **guidance**, not a binding architectural decision
- If this knowledge base conflicts with an ADR, the ADR wins
- If this knowledge base conflicts with the codebase, the codebase wins for implementation status

This document should be treated as a **reference guide for onboarding, code reviews, and architectural consistency** — not as a replacement for ADRs or the actual codebase.

---

## 1. PROJECT OVERVIEW

### What is This Project?

A **living, portfolio-grade FinTech platform** built in Java to continuously improve backend engineering skills while exploring real-world distributed systems challenges.

### Key Characteristics

- **NOT a tutorial project** — Designed to evolve and refactor over time
- **Production-grade principles** — But experimental in approach
- **Long-lived portfolio** — GitHub-hosted, evolving with Java ecosystem
- **Compliance-aware** — Financial system requirements built in from day one
- **Modular and extensible** — Microservices-based architecture

### Why FinTech?

FinTech systems naturally introduce realistic engineering challenges:
- High concurrency and parallel transaction handling
- Consistency vs availability trade-offs
- Event-driven workflows and eventual consistency
- Integration with legacy (SOAP) and modern (REST) APIs
- Strict auditing, traceability, and compliance requirements

### Why Java?

Java is dominant in enterprise and financial systems. This project:
- Leverages classic enterprise Java patterns
- Experiments with modern Java 21+ features (Virtual Threads, Structured Concurrency)
- Compares blocking, reactive, and event-driven approaches
- Applies Java in high-throughput, distributed environments

---

## 2. CORE VISION & MINDSET

### Engineering Philosophy

**You are a senior-level software architect and Java engineer with deep expertise in:**
- Domain-Driven Design (DDD)
- Clean / Hexagonal Architecture
- FinTech / Banking systems
- Event-driven and microservice-based platforms

### Mindset: Evolution Over Perfection

The system is NOT designed to be perfect from day one:
- Architectural decisions may change
- Services may be rewritten or split
- Multiple approaches may coexist for comparison
- Reflects real enterprise systems where architecture evolves with business needs and technology

### Project Goals

1. Serve as a **living portfolio** on GitHub
2. Evolve over years **alongside the Java ecosystem**
3. Be **production-grade**, compliance-aware, and extensible
4. Follow **DDD, clean architecture, and hexagonal principles** strictly
5. Demonstrate:
    - Strong Java fundamentals
    - Advanced backend engineering skills
    - Distributed systems thinking
    - Clean architecture and design decisions
    - Declarative domain rules via Specification Pattern
    - Metrics-first observability strategy
    - Continuous learning mindset

### Core Constraints

- **Domain layer must be pure Java** — No framework dependencies
- **Infrastructure concerns isolated** — JPA, messaging, config, cloud all external
- **Business rules live in domain** — Never in controllers or services
- **Audit fields are domain concerns** — createdAt, updatedAt, deletedAt tracked by domain
- **Identity (UUID) domain-generated** — Not database-generated
- **Overengineering avoided** — But future evolution anticipated
- **Explicit architectural decisions** — Preferred over generic best practices

---

## 3. ARCHITECTURAL FRAMEWORK

### 3.1 HEXAGONAL ARCHITECTURE (ADR-0001)

### Principles

1. **Domain at the Center** — Business logic has zero external dependencies
2. **Ports** — Interfaces defined in the domain layer
3. **Adapters** — Implementations in infrastructure layer
4. **Dependency Rule** — Dependencies point inward (Infrastructure → Application → Domain)

### Package Structure

```
com.daghanemre.fintech.{service}/
├── domain/                    # Pure domain (no framework dependencies)
│   ├── model/                 # Entities, Value Objects, Aggregates, Enums
│   ├── port/                  # Repository interfaces (outbound ports)
│   ├── specification/         # Domain rules (Specification Pattern)
│   └── exception/             # Domain exceptions
├── application/               # Use cases (framework-free orchestration)
│   ├── usecase/               # Business use cases (one class per use case)
│   └── exception/             # Application-level exceptions
└── infrastructure/            # Framework-specific adapters
    ├── adapter/
    │   └── rest/              # Inbound REST adapter
    │       ├── controller/    # HTTP controllers (@RestController)
    │       ├── dto/           # Request/Response DTOs
    │       └── exception/     # Exception handlers, HTTP mappers
    ├── persistence/           # Outbound persistence adapter
    │   └── jpa/
    │       ├── entity/        # JPA entities (@Entity)
    │       ├── mapper/        # Domain ↔ Entity mapping
    │       └── repository/    # Spring Data repositories
    ├── config/                # Spring configurations
    └── event/                 # Asynchronous event adapters (Kafka, etc.)
```

### Communication Layers

| Layer | Contains | Dependencies |
| --- | --- | --- |
| Infrastructure | Adapters, Config, JPA, REST | Can depend on Application, Domain |
| Application | Use Cases, Orchestration | Can depend on Domain only |
| Domain | Entities, Value Objects, Specifications | No external dependencies |

---

### 3.2 MICROSERVICE DECOMPOSITION

### Planned Core Services

| Service | Responsibility |
| --- | --- |
| **customer-service** | Customer lifecycle, identity, profile |
| **payment-service** | Money transfers, transactions, concurrency |
| **fraud-service** | Rule-based risk scoring, fraud detection |
| **notification-service** | Async messaging and user notifications |
| **open-banking-adapter** | REST & SOAP integrations with external APIs |
| **reporting-service** | Immutable audit logs, reporting views |

### Bounded Contexts

Each microservice owns:
- Its complete domain model
- Its data (no shared databases)
- Its API contracts
- Its technology choices (with platform guidelines)

### Data Management Strategy

- **Each service owns its database** — No cross-service database access
- **No shared databases** — Enforced by architecture
- **Data duplication allowed** — When necessary for performance
- **Event-driven replication preferred** — Over tight coupling

---

### 3.3 COMMUNICATION PATTERNS

### Synchronous Communication

**Used when:**
- Immediate response required
- Strong consistency necessary

**Technologies:**
- REST APIs (primary)

### Asynchronous Communication

**Used when:**
- Decoupling preferred
- Workflows can tolerate eventual consistency

**Technologies:**
- Kafka (preferred for high-throughput)
- RabbitMQ (alternative)

**Patterns:**
- Event publishing
- Consumer groups
- Dead-letter queues (DLQ)

### Integration Strategy

**Modern Integrations:**
- REST-based APIs
- JSON payloads
- Versioned endpoints

**Legacy Integrations:**
- SOAP-based services
- XML contracts
- Adapter and Anti-Corruption Layers for protection

---

## 4. DOMAIN-DRIVEN DESIGN RULES

### 4.1 DOMAIN LAYER ISOLATION

### Pure Java Domain

The domain layer is **100% framework-free**:

```java
// ✅ GOOD - Pure domain, no Spring
public class Customer {
    private final CustomerId id;
    private Email email;
    private CustomerStatus status;

    public void activate() {
        // Idempotency check: safe to call multiple times
        if (this.status == CustomerStatus.ACTIVE) {
            return;
        }

        // Enforce business rules via specification
        ensure(CustomerSpecifications.canBeActivated());
        this.status = CustomerStatus.ACTIVE;
        touch();  // Update timestamp
    }

    private void ensure(Specification<Customer> spec) {
        if (!spec.isSatisfiedBy(this)) {
            throw new SpecificationException(spec.violation(this));
        }
    }
}
```

```java
// ❌ BAD - Framework leakage into domain
public class Customer {
    @Transactional
    @Cacheable
    public void activate() {
        // ...
    }
}
```

### Testability Without Infrastructure

Domain logic must be testable **without Spring, without database, without HTTP**:

```java
// ✅ GOOD - Domain unit test
@Test
void activateCustomer() {
    Customer customer = Customer.create(new Email("test@example.com"));
    customer.activate();
    assertThat(customer.status()).isEqualTo(ACTIVE);
}

// No Spring, no @Transactional, no database needed
```

---

### 4.2 AGGREGATE DESIGN (ADR-0002)

### Aggregate Root: Customer

**Customer is the only aggregate root** in the customer bounded context.

### Identity Strategy

- **CustomerId**: UUID-based value object
- **Generation**: Domain-generated using `UUID.randomUUID()` (v4)
- **Why domain-generated?**
    - Aggregate complete before persistence
    - Enables event-driven architecture
    - No database coupling
    - Testable without infrastructure

### Value Objects

Domain-significant primitives should be wrapped in value objects when they carry identity, validation, normalization, invariants, or business meaning:

| Value Object | Purpose | Validation |
| --- | --- | --- |
| **CustomerId** | Customer identity | UUID uniqueness |
| **Email** | Contact information | Pragmatic RFC-inspired validation + normalization |
| **StateChangeReason** | Audit information | Non-empty string |

### Audit Fields as Domain Concern

Audit fields are **business requirements**, not technical metadata:

```java
public class Customer {
    private final LocalDateTime createdAt;      // When created
    private LocalDateTime updatedAt;            // Last modification
    private LocalDateTime deletedAt;            // Soft delete timestamp

    // Domain controls audit lifecycle
    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

**Why domain concern?**
- Regulatory requirement (financial compliance, GDPR)
- Soft delete is business logic
- Domain behaviors update timestamps explicitly
- No JPA @CreatedDate magic — domain controls its lifecycle

### Customer Status Lifecycle

```java
enum CustomerStatus {
    PENDING,      // KYC pending
    ACTIVE,       // Normal operations
    SUSPENDED,    // Temporarily restricted (fraud investigation)
    INACTIVE,     // Customer-initiated closure
    BLOCKED       // Permanent ban (AML/compliance)
}
```

**Current Intended State Transitions (subject to refinement):**

The distinction between `INACTIVE` and soft-delete semantics is still under architectural clarification and may evolve through future ADRs.

| From | To | Condition |
| --- | --- | --- |
| PENDING | ACTIVE | KYC verified |
| ACTIVE | SUSPENDED | Fraud suspected |
| SUSPENDED | ACTIVE | Investigation cleared |
| ACTIVE, SUSPENDED | BLOCKED | AML/compliance decision |
| Any | INACTIVE | Customer request |
| BLOCKED, INACTIVE | N/A | Terminal states |

**Soft Delete Semantics:**
- Soft delete is represented by `deletedAt != null`, NOT by a `DELETED` enum value
- A customer can be in any status (ACTIVE, SUSPENDED, etc.) and separately be marked as deleted
- Business logic checks: `if (customer.isDeleted()) { reject operation }`
- If a future ADR introduces `CustomerStatus.DELETED` as an explicit enum value, it will supersede this soft-delete model, but that decision has not yet been made

### Idempotency Pattern

```java
public void activate() {
    // Idempotency check (NOT a specification - this is aggregate behavior)
    if (this.status == ACTIVE) {
        return;  // Safe to call multiple times
    }

    // Then validate business rules via specification
    ensure(CustomerSpecifications.canBeActivated());
    this.status = ACTIVE;
    touch();
}
```

**Why idempotency is NOT a specification:**
- Specifications answer: “Can this operation be performed?”
- Idempotency defines: “What happens if already in this state?”
- Idempotency is aggregate behavior, not a rule about eligibility

---

### 4.3 SPECIFICATION PATTERN (ADR-0004)

### Core Principle

Domain-level eligibility rules and reusable business invariants SHOULD be expressed declaratively using the Specification Pattern.

Specifications are appropriate when:
- a rule decides whether a domain operation may proceed,
- the rule depends on aggregate state or domain data,
- the failure should produce a stable domain violation code,
- the rule benefits from reuse, isolation, composition, or observability.

Avoid scattering imperative if-else chains across aggregates and use cases for eligibility rules. If conditional branching is needed to produce precise violation semantics, keep it inside semantic specifications.

### What is a Specification?

A pure, side-effect-free object that:
1. Evaluates whether an aggregate satisfies a business rule
2. Returns an immutable `SpecificationViolation` if not satisfied
3. Supports logical composition (AND, OR, NOT)
4. Is testable in isolation

### Core Interface (fintech-common)

```java
public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
    SpecificationViolation violation(T candidate);

    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }

    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }

    default Specification<T> not() {
        return new NotSpecification<>(this);
    }
}

public class SpecificationViolation {
    private final String code;           // Unique business rule ID
    private final String message;        // Human-readable reason
    private final Map<String, Object> context;  // Additional details
}
```

### Specification Categories

### Atomic Specifications

Single business rules that cannot be decomposed:

```java
// ✅ GOOD - Atomic specification
public class CustomerNotDeletedSpec implements Specification<Customer> {
    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.deletedAt() == null;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        return new SpecificationViolation(
            "CUSTOMER_DELETED",
            "Customer has been deleted"
        );
    }
}

// Naming convention:
// Atomic: {Entity}Is/Not{Rule}Spec (e.g. CustomerIsActiveSpec)
// Semantic: {Entity}Can{Action}Spec (e.g. CustomerCanBeActivatedSpec)
```

### Semantic Specifications

Domain-specific rule combinations that represent a complete business condition:

```java
// ✅ GOOD - Semantic specification with atomic delegation and explicit violation codes
public final class CustomerCanBeActivatedSpec implements Specification<Customer> {

    private final Specification<Customer> notDeleted = new CustomerNotDeletedSpec();
    private final Specification<Customer> notBlocked = new CustomerNotBlockedSpec();

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return notDeleted.isSatisfiedBy(customer)
            && notBlocked.isSatisfiedBy(customer)
            && customer.status() == CustomerStatus.PENDING;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        // Explicit violation detection with domain-specific codes
        if (!notDeleted.isSatisfiedBy(customer)) {
            return new SpecificationViolation(
                "CUSTOMER_DELETED",
                "Deleted customers cannot be activated"
            );
        }

        if (!notBlocked.isSatisfiedBy(customer)) {
            return new SpecificationViolation(
                "CUSTOMER_BLOCKED",
                "Blocked customers cannot be activated; administrative action required"
            );
        }

        if (customer.status() == CustomerStatus.ACTIVE) {
            return new SpecificationViolation(
                "CUSTOMER_ALREADY_ACTIVE",
                "Customer is already in ACTIVE state"
            );
        }

        return new SpecificationViolation(
            "INVALID_STATUS_TRANSITION",
            "Customer cannot be activated from current status"
        );
    }
}

// ❌ BAD - Generic OR composition (produces ambiguous violations)
// Don't do: new IsPendingSpec().or(new IsActiveSpec())
// This creates multiple possible failures with unclear domain meaning
```

**Why This Pattern:**
- Semantic specs delegate to atomic specs for primitive truth tests
- Violation codes are explicit and domain-specific
- Each failure path returns a meaningful violation code
- Easier to observe (metrics, logs) and test
- Demonstrates how conditional branching belongs inside specs, not aggregates

### Aggregate Guard Pattern

```java
public class Customer {
    // Central guard method: all specification checks go through here
    private void ensure(Specification<Customer> spec) {
        if (!spec.isSatisfiedBy(this)) {
            throw new SpecificationException(spec.violation(this));
        }
    }

    // Business operations enforce rules before state change
    public void activate() {
        ensure(CustomerSpecifications.canBeActivated());
        this.status = CustomerStatus.ACTIVE;
        touch();
    }

    public void suspend(String reason) {
        ensure(CustomerSpecifications.canBeSuspended());
        this.status = CustomerStatus.SUSPENDED;
        this.suspensionReason = new StateChangeReason(reason);
        touch();
    }

    public void block() {
        ensure(CustomerSpecifications.canBeBlocked());
        this.status = CustomerStatus.BLOCKED;
        touch();
    }
}
```

### When NOT to Use Specification Pattern

**Specifications are NOT used for:**

1. **Value Object Validation** — Use constructors and factory methods
    
    ```java
    // ✅ Value object enforces itself
    public class Email {
        public Email(String value) {
            if (!isValidEmail(value)) throw new IllegalArgumentException();
            this.value = normalize(value);
        }
    }
    ```
    
2. **Aggregate Creation Rules** — Encoding in constructors is fine
    
    ```java
    // ✅ Creation validation
    public static Customer create(Email email) {
        return new Customer(CustomerId.generate(), email);
    }
    ```
    
3. **Idempotency Checks** — Handle inside aggregate
    
    ```java
    // ✅ Idempotency check (not a specification)
    public void activate() {
        if (this.status == ACTIVE) return;
        // Then apply specifications
    }
    ```
    
4. **State Mutation and Side Effects** — Specifications must be pure
    
    ```java
    // ❌ BAD - Specification with side effects
    public class SendWelcomeEmailSpec {
        public boolean isSatisfiedBy(Customer c) {
            emailService.send(...);  // FORBIDDEN
            return true;
        }
    }
    ```
    
5. **Technical or Infrastructure Validation** — Belongs to adapters
    
    ```java
    // ❌ BAD - Infrastructure concerns in specifications
    public class CustomerExistsInDatabaseSpec {
        // Use in Application layer, not domain
    }
    ```
    

---

### 4.4 APPLICATION LAYER DESIGN (ADR-0003)

### Responsibilities

The Application Layer orchestrates domain behaviors **within the context of a use case**.

### Use Case Pattern

One class per use case:

```java
// ✅ GOOD - One use case, one class
public class ActivateCustomerUseCase {
    private final CustomerRepository customerRepository;  // Port only

    public void execute(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Call domain behavior (no business rules duplicated)
        customer.activate();

        // Persist domain state
        customerRepository.save(customer);
    }
}

// ✅ GOOD - Multiple operations in one flow
public class ChangeCustomerEmailUseCase {
    public void execute(UUID customerId, String newEmail) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(...);

        customer.changeEmail(new Email(newEmail));  // Domain validates

        customerRepository.save(customer);
    }
}
```

### Transaction Boundary Ownership

**Conceptual Principle:**
The transaction boundary wraps a complete use-case execution. All state changes within an aggregate (triggered by a single use case) must succeed or fail atomically.

**Implementation Constraint:**
Pure application use-case classes must remain **framework-agnostic**. They must not import or depend on Spring annotations such as `@Transactional`.

Transaction management is applied **outside and around** the use case through one of these mechanisms:

**Option 1: Spring Configuration/Decorator Approach** (Preferred)

```java
// Pure use case - zero framework dependencies
public class ActivateCustomerUseCase {
    private final CustomerRepository customerRepository;

    public void execute(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        customer.activate();
        customerRepository.save(customer);
        // No @Transactional annotation needed
    }
}

Preferred approach:
- Keep use cases pure and framework-agnostic
- Apply transaction management through configuration, decorators, or Spring-managed adapters
- Concrete transaction decorator implementation will be introduced when transaction boundaries become more complex
```

**Option 2: Controller Adapter Approach** (Acceptable Pragmatic Trade-off)

```java
// Use case remains pure
public class ActivateCustomerUseCase {
    public void execute(UUID customerId) {
        // Framework-free logic
    }
}

// Controller owns transaction boundary
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final ActivateCustomerUseCase useCase;

    @PostMapping("/{id}/activate")
    @Transactional  // Transaction boundary here - acceptable trade-off
    public ResponseEntity<?> activate(@PathVariable UUID id) {
        useCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Important Caveat:**
While placing `@Transactional` on the controller is **operationally acceptable** as a pragmatic short-term choice, it should NOT become the default architectural pattern across the codebase. As the system matures, prioritize moving transaction management to configuration or decorator patterns to ensure use cases remain fully framework-free and testable.

### Repository Port Usage

- Application uses only **ports (interfaces)**, never implementations
- Domain unaware of repositories
- Repository implementations are infrastructure adapters

```java
// ✅ GOOD - Application depends on port only
public class ActivateCustomerUseCase {
    private final CustomerRepository customerRepository;  // Port

    public void execute(UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(...);
        // ...
    }
}

// ✅ GOOD - Implementation in infrastructure layer
@Repository
public class CustomerJpaRepositoryAdapter implements CustomerRepository {
    private final SpringDataCustomerRepository springRepo;

    @Override
    public Optional<Customer> findById(UUID id) {
        return springRepo.findById(id).map(this::toDomain);
    }
}
```

### Error Handling Strategy

- Domain exceptions **NOT masked** in Application Layer
- Exceptions **propagated** to upper layer (adapter)
- **Exception mapping** (HTTP status, error codes) happens **after** Application Layer
- Application Layer is framework-agnostic

```java
// ✅ GOOD - Exception propagation
public class ActivateCustomerUseCase {
    public void execute(UUID id) {
        Customer customer = findCustomer(id);

        // SpecificationException from domain bubbles up
        customer.activate();  // May throw SpecificationException

        customerRepository.save(customer);
        // Exception handled by GlobalExceptionHandler
    }
}
```

### Test Strategy for Application Layer

```java
// ✅ GOOD - Unit test with mocked ports
@Test
void shouldActivateCustomer() {
    // Arrange
    UUID customerId = UUID.randomUUID();
    Customer customer = Customer.create(new Email("test@example.com"));

    CustomerRepository mockRepo = mock(CustomerRepository.class);
    when(mockRepo.findById(customerId))
        .thenReturn(Optional.of(customer));

    ActivateCustomerUseCase useCase = new ActivateCustomerUseCase(mockRepo);

    // Act
    useCase.execute(customerId);

    // Assert
    verify(mockRepo).save(argThat(c -> c.status() == ACTIVE));
}
```

**Test Goal:**
- ✅ Was the correct domain method called?
- ✅ Is the transaction flow correct?
- ❌ Don’t re-test domain logic (tested in domain unit tests)

---

## 5. CODING PRINCIPLES & STANDARDS

### 5.1 UNIVERSAL CODING PRINCIPLES

### Domain-Centric Rules

- ✅ **No anemic domain** — Behavior lives in domain, not services
- ✅ Avoid primitive obsession — Use value objects for domain-significant concepts (Email, CustomerId, etc.)
- ✅ **No infrastructure leakage** — Domain contains zero Spring, JPA, HTTP concerns
- ✅ **Domain framework-agnostic** — Testable without Spring
- ✅ **Aggregate invariants enforced inside aggregate** — Not in application layer
- ✅ **Explicit state transitions** — No hidden side effects
- ✅ **Test-first mindset** — Especially for domain logic
- ✅ **Favor clarity over cleverness** — Code reads like business specification

### Specification Pattern Usage

- ✅ **Domain-level eligibility rules SHOULD be modeled as Specifications** when they produce meaningful violation semantics
- ✅ **Specifications pure, stateless, side-effect free**
- ✅ **Composite rules express domain semantics clearly**
- ✅ **Avoid generic OR-composition** when domain semantics require explicit violation codes
- ✅ **Prefer semantic specifications** (e.g., `CustomerCanBeActivatedSpec`) over boolean logic
- ✅ **Conditional branching belongs inside specs, not aggregates** — when needed for violation precision, keep it in semantic spec violation() methods
- ❌ **NOT used for** value object validation, simple aggregate creation, idempotency checks, side effects, technical validation, or trivial one-off checks

---

### 5.2 CODE STYLE STANDARDS

### Naming Conventions

| Concept | Pattern | Example |
| --- | --- | --- |
| Domain Entity | `{Entity}` | `Customer`, `Payment` |
| Value Object | `{Concept}` | `Email`, `CustomerId` |
| Enum | `{Concept}` | `CustomerStatus`, `Currency` |
| Aggregate Root | `{Entity}` | `Customer` |
| Port (Interface) | `{Entity}Repository` | `CustomerRepository` |
| Adapter Implementation | `{Technology}{Entity}RepositoryAdapter` | `CustomerJpaRepositoryAdapter` |
| Use Case | `{Action}{Entity}UseCase` | `ActivateCustomerUseCase` |
| Atomic Positive Spec | `{Entity}Is{State}Spec` | `CustomerIsActiveSpec` |
| Atomic Negative Spec | `{Entity}Not{ForbiddenState}Spec` | `CustomerNotDeletedSpec` |
| Semantic Spec | `{Entity}Can{BusinessOperation}Spec` | `CustomerCanBeActivatedSpec` |
| Specification Factory | `{Entity}Specifications.{method}()` | `CustomerSpecifications.canBeActivated()` |
| Exception | `{Entity}{Condition}Exception` | `CustomerDeletedException` |
| DTO | `{Action}Request`, `{Action}Response` | `ActivateRequest`, `ErrorResponse` |
| Controller | `{Entity}Controller` | `CustomerController` |
| JPA Entity | `{Entity}JpaEntity` | `CustomerJpaEntity` |
| Mapper | `{Entity}Mapper` | `CustomerMapper` |

**Anti-Patterns to Avoid:**
- ❌ `CustomerServiceImpl` (use ports, not implementations)
- ❌ `CustomerStatusEnum` (redundant suffix on enums)
- ❌ `CustomerDto` (use `Customer` for domain; DTOs only at boundaries)
- ❌ `NotDeletedCustomerSpec` (subject-verb order wrong; should be `CustomerNotDeletedSpec`)
- ❌ `CanBeActivatedSpec` (missing entity context; should be `CustomerCanBeActivatedSpec`)
- ❌ `CustomerCanBeActivatedSpec.instance()` (use factory method `CustomerSpecifications.canBeActivated()`)
- ❌ `Service` suffix for domain (use `Specification` for rules or method names for behaviors)

### Code Organization

```java
// ✅ GOOD - Logical organization
public class Customer {
    // 1. Identity and value objects
    private final CustomerId id;
    private final Email email;

    // 2. State
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 3. Invariant enforcement
    private void ensure(Specification<Customer> spec) { }

    // 4. Factory methods
    public static Customer create(Email email) { }
    public static Customer reconstitute(...) { }

    // 5. Business operations
    public void activate() { }
    public void suspend(String reason) { }

    // 6. Queries
    public CustomerStatus status() { }
    public LocalDateTime createdAt() { }

    // 7. Value object accessors
    public Email email() { }
    public CustomerId id() { }
}
```

### Javadoc Standards

Add Javadoc where it improves understanding:

```java
// ✅ GOOD - When adding clarity
/**
 * Activates this customer, transitioning from PENDING to ACTIVE state.
 *
 * This operation enforces the following business rules:
 * - Customer must be in PENDING status
 * - Customer must not be deleted
 * - Customer must not be blocked
 *
 * After activation, the customer can perform all operations.
 *
 * @throws SpecificationException if business rules violated
 */
public void activate() { }

// ❌ BAD - Redundant
/**
 * Gets the customer ID
 */
public CustomerId id() { }
```

---

### 5.3 DESIGN DECISIONS & TRADE-OFFS

### When Writing Code

1. **Always start with DESIGN before CODE:**
    - Clarify domain invariants
    - Clarify responsibilities
    - Clarify future evolution points
2. **Then write code:**
    - Prefer explicit domain behavior
    - Avoid premature abstractions
    - Write production-grade Java (no pseudo-code)
3. **Domain Rule Workflow:**
    - Identify domain invariants before writing code
    - Model each invariant as a Specification
    - Compose rules declaratively instead of branching logic
    - Enforce rules inside the aggregate, not in use cases
    - Add unit tests per specification before integration
    - Ensure violations are observable via metrics

### Always Explain Your Decisions

When making architectural choices:
- ✅ **Why this design?**
- ✅ **What alternatives exist?**
- ✅ **What are the trade-offs?**

Act as a **senior engineer mentoring another senior engineer:**
- Challenge assumptions when needed
- Preserve architectural integrity at all times
- Explain rather than dictate

---

## 6. CODE REVIEW CHECKLISTS

### Domain Review Checklist

Before approving domain code:

- [ ]  Is this a true Aggregate Root or a Value Object?
- [ ]  Are all business invariants enforced inside the aggregate?
- [ ]  Is there any anemic behavior (data without logic)?
- [ ]  Are state transitions explicit and valid?
- [ ]  Is identity handled via value objects (no primitives)?
- [ ]  Is the domain free from framework or infrastructure dependencies?
- [ ]  Are side effects avoided inside the domain?
- [ ]  Is this model testable without Spring or a database?
- [ ]  Does the design allow future evolution without breaking invariants?

### Specification Pattern Review

- [ ]  Are all domain invariants enforced via Specifications?
- [ ]  Are specifications atomic, composable, and domain-specific?
- [ ]  Is any business logic hidden in if/else blocks?
- [ ]  Does each failing rule return a meaningful SpecificationViolation?
- [ ]  Are OR-compositions semantically explicit (no ambiguous violations)?
- [ ]  Can each rule be unit-tested in isolation?

### Application Layer Review

- [ ]  Are there business rules in the Application Layer?
- [ ]  Is there domain logic duplication?
- [ ]  Has repository implementation leaked?
- [ ]  Is the transaction boundary clear?
- [ ]  Are domain exceptions masked?
- [ ]  Do test mocks use domain ports?
- [ ]  Does use case orchestrate without deciding?

### Hexagonal Architecture Review

- [ ]  Do domain classes import from domain only?
- [ ]  Do application classes import from domain and application only?
- [ ]  Do infrastructure classes avoid importing from API layer?
- [ ]  Is every port (repository, etc.) injected via constructor?
- [ ]  Are DTOs and Entities separated?

---

## 7. TECHNOLOGY STACK

### Current Local Implementation

| Layer | Technology | Rationale |
| --- | --- | --- |
| **JDK** | Java 21+ | Modern language features, Virtual Threads for future exploration |
| **Framework** | Spring Boot 3.5.x | Dominant enterprise framework; allows easy swapping |
| **Build** | Maven | Industry standard; coordinates multi-module builds |
| **Local Database** | PostgreSQL 15+ with Testcontainers | Open-source; similar transaction semantics to Oracle |
| **Testing** | JUnit 5, Testcontainers | Industry standard; enables integration testing without external infra |
| **Mapping** | MapStruct | Generates domain ↔︎ entity mappers (used selectively) |
| **Metrics** | Micrometer | Framework-agnostic metrics collection |
| **Logging** | SLF4J + Logback | Standard Java logging abstraction |
| **Observability** | Spring Actuator, Prometheus-ready | Built into Spring Boot; enables metrics export |

### Enterprise Compatibility Targets

The platform is designed to be portable to enterprise databases and deployment models:

- **Database**: Oracle, IBM Db2, or other relational databases with ACID guarantees
- **Cloud**: AWS-compatible architecture (stateless services, containerizable)
- **Framework**: Potential migration to Quarkus, Micronaut, or Virtual Thread-based frameworks in Phase 7

**Technology choices are NOT prescriptive.** Different services may adopt different technologies as long as they maintain:
- Domain layer purity (framework-free)
- Hexagonal architecture
- DDD principles
- Specification Pattern for rules

### Technology Introduction (Phased)

Technologies are introduced **incrementally and deliberately:**

- **Phase 1 (Current)** — Spring Boot, REST APIs, basic relational database
- **Phase 2** — Concurrency patterns, transaction management, optimistic/pessimistic locking
- **Phase 3** — Event-driven (Kafka or RabbitMQ), eventual consistency
- **Phase 4** — SOAP and REST integration patterns, adapter layers
- **Phase 5** — Configuration management (Spring Cloud Config or equivalent)
- **Phase 6** — Structured logging, distributed tracing (OpenTelemetry), metrics aggregation
- **Phase 7** — Modern Java features (Virtual Threads, Structured Concurrency), performance exploration
- **Phase 8 (Optional)** — Cloud deployment (Docker, Kubernetes), cloud-native patterns

---

## 8. CURRENT IMPLEMENTATION STATUS

### ✅ COMPLETED / VERIFIED

**Project Foundation**
- Repository structure established
- Root README with project vision
- Architecture vision document
- Development roadmap
- ADR structure and process established
- Microservice-oriented monorepo layout

**Shared Common Module (fintech-common)**
- Framework-agnostic Specification Pattern primitives:
- `Specification<T>` interface with `and()`, `or()`, `not()` composition
- `AndSpecification`, `OrSpecification`, `NotSpecification`
- `SpecificationViolation` (immutable value object)
- `SpecificationException` (unchecked)

**Customer Domain Model**
- `Customer` aggregate root
- Core value objects:
- `CustomerId` (domain-generated UUID)
- `Email` (pragmatic RFC-inspired validation)
- `StateChangeReason` (audit context)
- `CustomerStatus` enum:
- PENDING, ACTIVE, SUSPENDED, INACTIVE, BLOCKED
- Soft delete represented by `deletedAt` (not a status value)
- Audit fields as domain concerns:
- `createdAt`, `updatedAt`, `deletedAt`
- Domain controls lifecycle, not JPA annotations
- Factory methods for creation and reconstitution

**Customer Specifications (Domain Rules)**
- Atomic specifications:
- `CustomerNotDeletedSpec`
- `CustomerIsActiveSpec`
- `CustomerNotBlockedSpec`
- Semantic specifications:
- `CustomerCanBeActivatedSpec`
- `CustomerCanBeSuspendedSpec`
- `CustomerCanBeMarkedInactiveSpec`
- `CustomerCanBeBlockedSpec`
- `CustomerCanChangeEmailSpec`
- `CustomerSpecifications` factory class for method references

**Infrastructure & Persistence**
- JPA adapter layer:
- `CustomerJpaRepositoryAdapter` (implements `CustomerRepository` port)
- `CustomerJpaEntity`
- `SpringDataCustomerRepository`
- Domain ↔︎ Entity mapping
- PostgreSQL testcontainers configuration
- Environment-specific configuration files

**REST Adapter (HTTP Layer)**
- `CustomerController`
- Basic error handling structure
- DTO layer defined

**Testing Baseline**
- Domain model unit tests
- Specification unit tests
- JPA adapter integration tests
- REST integration tests for implemented flows

**Architecture Enforcement**
- Architecture Decision Records (7 ADRs) documented
- ArchUnit test structure introduced

---

### 🟡 PARTIALLY IMPLEMENTED / NEEDS VERIFICATION

**Application Layer (Use Cases)**
- `ActivateCustomerUseCase` — verified to exist
- `ChangeCustomerEmailUseCase` — verified to exist
- Other lifecycle flows may exist but should be verified:
- Suspend customer
- Reactivate suspended customer
- Block customer
- Mark inactive / soft delete
- Create new customer (if not factory-only)

**REST API Endpoints**
- Confirmed endpoints with integration tests:
- Activate customer
- Change customer email
- Remaining endpoints should be verified:
- Customer creation
- Customer suspension
- Customer blocking
- Customer soft delete
- Fetch customer by ID

**Metrics & Observability**
- Violation code documentation exists
- Metrics collection wiring in GlobalExceptionHandler — should be verified for completeness
- TraceId/MDC propagation — not yet verified as implemented

**ArchUnit Guardrails**
- Architecture test classes exist
- Specific rule hardening (dependency direction, enum ownership, specification contracts) — should be verified for completeness against ADR-0007 requirements

### 🔵 PLANNED / NOT YET STARTED

**Application Layer Completion**
- [ ] Implement or verify suspend customer use case with state machine coverage
- [ ] Implement or verify reactivate customer use case (SUSPENDED → ACTIVE)
- [ ] Implement or verify block customer use case
- [ ] Implement or verify soft delete / mark inactive use case
- [ ] Clarify relationship between `INACTIVE` status and soft delete semantics:
- Are they the same concept?
- Does `INACTIVE` change status while `deletedAt` gates access?
- Or does soft delete occur by setting both?

**REST API Hardening**
- [ ] Verify all lifecycle endpoint implementations
- [ ] Define OpenAPI / Swagger contracts with explicit violation responses
- [ ] Add structured error response documentation:
- Violation codes mapped to HTTP status
- Example payloads for each error case

**Observability Completeness**
- [ ] Implement TraceId / MDC propagation across request/response cycle
- [ ] Add request headers for correlation
- [ ] Verify metrics cardinality constraints (normalized paths, no dynamic IDs in tags)
- [ ] Document metrics collection points

**ArchUnit Rule Hardening (ADR-0007)**
- [ ] Enforce domain purity (no Spring imports in domain)
- [ ] Enforce hexagonal dependency direction
- [ ] Enforce specification pattern contracts (violation override, stateless)
- [ ] Enforce enum ownership (Tier 1 vs Tier 2)

**Specification Terminology Clarity**
- [ ] Ensure all examples in code use `CustomerSpecifications.canX()` factory methods
- [ ] Remove any direct `SomeSpec.instance()` patterns in favor of factory methods
- [ ] Audit all documented examples for accuracy

**Delete / Terminal States Clarification**
- [ ] Decide: Is `CustomerStatus.DELETED` a valid future enum value?
- If YES: Create ADR-??? to introduce it, remove soft-delete-only semantics
- If NO: Keep soft delete as `deletedAt != null`, document this distinction clearly
- [ ] Remove references to `blockedAt` unless formally introduced as a field

**Phase 2 Preparation: Payment Service**
- [ ] Draft ADR-0008: Domain Events and Integration Events Strategy
- [ ] Define event naming conventions, versioning, serialization
- [ ] Clarify cross-service enum boundary rules (Tier 2 contracts)
- [ ] Begin Payment Service skeleton with idempotency and concurrency concerns

---

### 📋 FULL ROADMAP

### Phase 0 ✅ — Project Foundation & Vision

- Repository structure
- README and vision documentation
- Architecture vision and roadmap

### Phase 1 🟡 — Core Domain & Customer Service

Status: Substantially implemented, not fully closed.

**Completed:**
- Customer aggregate baseline and core value objects
- Specification Pattern infrastructure in fintech-common
- Repository port and JPA adapter
- Basic REST adapter
- Test structure baseline

**Remaining (in progress):**
- Full lifecycle use-case coverage (suspend, reactivate, block, soft-delete)
- REST endpoint verification and completion
- OpenAPI/error contract hardening
- TraceId/MDC propagation infrastructure
- ArchUnit guardrail hardening

### Phase 2 — Payment Service & Concurrency Challenges

- Money transfer operations
- Transaction lifecycle management
- Idempotent payment processing
- Concurrency control, locking strategies

### Phase 3 — Event-Driven Architecture

- Payment events publishing
- Asynchronous consumers (fraud, notification)
- Kafka / RabbitMQ integration
- Eventual consistency patterns

### Phase 4 — Open Banking & External Integrations

- REST-based open banking APIs
- SOAP-based legacy services
- Adapter and anti-corruption layers

### Phase 5 — Configuration Management & Dynamic Behavior

- Spring Cloud Config
- Feature toggles
- Runtime rule updates

### Phase 6 — Observability, Logging & Tracing

- OpenTelemetry integration
- Structured logging
- Distributed tracing

### Phase 7 — Modern Java Evolution

- Java 17 → Java 21+ features
- Virtual Threads experiments
- Structured Concurrency
- Performance comparisons

### Phase 8 — Cloud Readiness & Deployment (Optional)

- Dockerization
- Kubernetes (conceptual or local)
- AWS-compatible architecture

---

## 9. DEVELOPMENT ROADMAP DETAIL

### Short-Term Focus (Current)

- Customer aggregate baseline ✅
- Domain unit test baseline ✅
- Repository port + JPA adapter ✅
- Basic REST adapter 🟡
- Integration test baseline 🟡
- Full lifecycle use-case coverage 🔵
- OpenAPI/error contract hardening 🔵
- TraceId/MDC propagation 🔵

### Mid-Term Focus (Next 2-3 Months)

- Event publishing (Kafka) — Phase 3
- Observability (metrics, health checks) — Phase 6
- Fraud & Notification integration — Phase 3
- Multi-service choreography — Phase 3

### Long-Term Focus (6+ Months)

- Open Banking adapters — Phase 4
- Cloud-native deployment (AWS) — Phase 8
- Performance & concurrency challenges — Phase 2
- Modern Java features (Virtual Threads) — Phase 7

---

## 10. ARCHITECTURE DECISION RECORDS (ADRs) SUMMARY

### ADR-0001: Adopt Hexagonal Architecture with Ports and Adapters

**Status:** ✅ Accepted

**Date:** 2025-12-27

**Decision:** Use Hexagonal Architecture for all microservices.

**Key Principles:**
- Domain at center with zero external dependencies
- Ports defined in domain layer
- Adapters in infrastructure layer
- Dependencies point inward

**Benefits:**
- Testability without external dependencies
- Infrastructure flexibility (easy swapping of database, messaging)
- DDD alignment

**Trade-offs:**
- More boilerplate (interfaces and implementations)
- Learning curve for teams
- Initial setup overhead

---

### ADR-0002: Customer Domain Model Design

**Status:** ✅ Accepted

**Date:** 2025-12-27

**Decision:** Customer is the aggregate root with domain-generated UUID identity.

**Key Points:**
- **Identity Strategy:** Domain-generated UUID v4 (future: v7)
- **Value Objects:** CustomerId, Email
- **Customer Status:** PENDING → ACTIVE → SUSPENDED → BLOCKED → terminal states
- **Audit Fields:** Domain concern (createdAt, updatedAt, deletedAt)
- **Factory Methods:** `create()` for new, `reconstitute()` for persistence

**Benefits:**
- Aggregate complete before persistence
- Event-driven ready
- No database coupling

**Non-Implementation:**
- JPA Auditing (@CreatedDate) explicitly NOT used
- Time abstraction deferred (using LocalDateTime.now())

---

### ADR-0003: Application Layer Responsibilities & Design

**Status:** ✅ Accepted

**Date:** 2025-01-03

**Decision:** Application Layer orchestrates use cases and owns transaction boundaries.

**Key Responsibilities:**
1. **Use-case Orchestration** — One class per use case
2. **Transaction Boundary Ownership** — Application/adapter level, not domain
3. **Repository Abstraction** — Uses ports only, never implementations
4. **No Business Rules** — All rules in domain
5. **No Infrastructure Leakage** — Framework-agnostic
6. **Error Handling** — Domain exceptions propagated, not masked
7. **Clear Test Strategy** — Mock ports, don’t re-test domain

**Benefits:**
- Domain isolation
- Clear use-case readability
- Infrastructure changes have minimal impact

---

### ADR-0004: Adoption of Specification Pattern for Domain Rule Enforcement

**Status:** ✅ Accepted

**Date:** 2026-01-11

Decision: Domain-level eligibility rules and reusable business invariants are modeled as composable Specifications.

**Key Decisions:**
- Rules modeled as pure, side-effect-free Specifications
- Violations represented as SpecificationViolation objects
- Specifications composable via AND/OR/NOT
- Aggregates use guard pattern: `ensure(specification)`
- `fintech-common` provides framework-agnostic abstractions
- `SpecificationException` is unchecked

**Benefits:**
- Business rules explicit and reusable
- Aggregate methods short and intention-revealing
- Rule-level unit testing straightforward
- Consistent error mapping
- Significantly improved observability

**Non-Goals:**
- NOT used for value object validation (use constructors)
- NOT used for aggregate creation rules
- NOT used for idempotency checks
- NOT used for technical validation (adapter concern)

---

### ADR-0005: Metrics & Observability Strategy for Domain Rule Violations

**Status:** ✅ Accepted

**Date:** 2026-01-11

**Decision:** Metrics-first observability for domain rule violations.

**Core Decisions:**
- Domain violations are signals, not errors
- Violation codes are primary observability dimension
- Metrics collected at adapter boundary (GlobalExceptionHandler)
- Operation tags normalized to avoid high-cardinality issues

**Metrics Strategy:**
- **Metric Name:** `domain.violation.total`
- **Type:** Counter
- **Tags:** `code` (violation ID), `operation` (normalized API operation)

**Benefits:**
- Distinguishes business friction from technical failures
- Quantifiable insight into rule violations
- Zero domain purity impact
- Cross-service consistency

---

### ADR-0006: Enum Ownership & Cross-Service Compatibility

**Status:** ✅ Accepted

**Date:** 2026-01-24

**Decision:** 3-tier enum classification based on ownership and scope.

**Tier Classification:**

| Tier | Ownership | Location | Serialization | Examples |
| --- | --- | --- | --- | --- |
| **Tier 1** | Service-Local | `{service}/domain/model/` | String | CustomerStatus, PaymentStatus |
| **Tier 2** | Platform-Common | `fintech-common/contract/` | Enum or String | Currency, CountryCode |

**Key Rules:**
- Tier 1: NEVER shared across service boundaries as Java enums
- Tier 2: Platform-owned, coordinated deployment
- ALL: Use String serialization at boundaries
- ALL: Implement `safeParse()` for forward compatibility

**Naming Conventions:**
- ✅ `CustomerStatus`, `PaymentStatus`, `Currency`
- ❌ `CustomerStatusEnum`, `CustomerStatusDto`

**Benefits:**
- Domain autonomy
- Service isolation
- Forward compatibility

---

### ADR-0007: Architecture Guardrails & Static Analysis

**Status:** ✅ Accepted

**Date:** 2026-01-29

**Decision:** Use ArchUnit for automated architecture testing.

**Rule Categories:**

1. **Hexagonal Boundaries**
    - Domain must not depend on infrastructure or API
    - Application must not depend on infrastructure or API
2. **DDD Rules**
    - Value Objects immutable (final fields only)
    - JPA entities NOT in domain package
    - Domain events immutable
3. **Specification Pattern**
    - All concrete specs override `violation()`
    - Specs stateless (final fields only)
    - Specs in `domain.specification` package
    - Atomic specs follow `Is/Not{Rule}Spec` naming
4. **Enum Ownership**
    - Domain enums in `domain.model`
    - No service-specific enums in common module
    - No `Enum` suffix

**Hard Rules vs Soft Rules:**
- **Hard Rules** (ArchUnit enforces) — Layer isolation, immutability, naming
- **Soft Rules** (Code review enforces) — Use case specification evaluation, semantic correctness

**Benefits:**
- Prevents architectural erosion
- Enables team scaling
- Reduces onboarding risk
- Maintains DDD integrity

---

## 11. WORKING WITH THIS KNOWLEDGE BASE

### How to Use This Document

1. **For New Features** — Review relevant sections (Domain Rules, Specifications, ADRs)
2. **For Code Reviews** — Use the Checklists section
3. **For Architectural Decisions** — Consult the ADRs Summary and trade-offs
4. **For Testing** — Follow test patterns in Domain-Driven Design Rules
5. **For Onboarding** — Start with Project Overview and Core Vision & Mindset

### When Something is Unclear

- Check the **ADRs Summary** for the rationale
- Review the **Code Review Checklists** for standards
- Reference **Coding Principles & Standards** for style
- Consult **Specification Pattern** section for domain rule modeling

### Updating This Document

This Knowledge Base should be updated when:
- New ADRs are accepted
- New coding standards are established
- Project roadmap progresses
- Implementation status changes
- Best practices are refined

---

## KNOWN OPEN DECISIONS

The following topics require explicit ADRs or architectural clarification before being treated as final decisions:

- Clarification of `INACTIVE` vs soft-delete semantics
- Whether `CustomerStatus.DELETED` will ever exist
- Domain Event vs Integration Event separation strategy
- Final transaction management implementation pattern
- Payment-service idempotency and concurrency model
- Criteria for introducing Tier 2 enums into `fintech-common`
- Future event serialization and schema versioning strategy

These topics are intentionally left flexible while the platform evolves through incremental implementation and ADR-driven refinement.

---

## DOCUMENT GOVERNANCE

This document is a **curated architecture knowledge base**, not the source of truth for architectural decisions or implementation state.

**Rules:**
- Do not mark a feature as completed unless code, tests, and project structure confirm it.
- Do not introduce new architectural rules here before they are captured in an ADR.
- If this document conflicts with an ADR, the ADR wins.
- If this document conflicts with the codebase, the codebase wins for implementation status.

**This document must be updated when:**
- a new ADR is accepted or superseded,
- implementation status verifiably changes (confirmed via build report or code review),
- project structure changes materially,
- a planned feature becomes implemented,
- architectural rules are enforced by new ArchUnit tests,
- technology decisions evolve.

**Update frequency:** After significant implementation progress or ADR acceptance.

---

## STATUS LEGEND

- ✅ **Completed / Verified:** Present in codebase and covered by tests or structure; independently verifiable.
- 🟡 **Partially Implemented / Needs Verification:** Some code exists, but flow or coverage is incomplete; requires code review to confirm.
- 🔵 **Planned / Not Yet Started:** Accepted as roadmap item but not implemented; may require ADR or design work.
- ⚠️ **Design Decision Needed:** Requires ADR or explicit architectural decision before implementation.

---

## 📚 REFERENCE MATERIALS

### Key Documentation in Notion

- **README** — Project vision and high-level overview
- **foundation-prompt** — Engineering mindset and expectations
- **architecture-vision** — Architectural goals and principles
- **roadmap** — Step-by-step development phases
- **enum-guidelines** — Quick reference for enum classification

### External References

- Eric Evans — *Domain-Driven Design*
- Vaughn Vernon — *Implementing Domain-Driven Design*
- Alistair Cockburn — *Hexagonal Architecture*
- Joshua Kerievsky — *Refactoring to Patterns*
- Martin Fowler — *Specification Pattern*

### Online Resources

- [ArchUnit Documentation](https://www.archunit.org/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Micrometer Metrics](https://micrometer.io/)

---

## 5. OBSERVABILITY & DIAGNOSTICS (ADR-0005)

- **Metrics-First Strategy**: Domain rule violations increment `domain.violation.total`.
- **Violation Codes**: Used as primary dimension (tags).
- **Correlation**: TraceId propagation across logs and responses.

## 6. STRATEGIC GOALS & ROADMAP

### Current Priorities (Sprint 1-2)

- **Architecture Hardening**: Enforcing ArchUnit guardrails across services.
- **Domain Completeness**: Implementing reversible business flows (ACTIVE ↔ SUSPENDED).
- **Distributed Tracing**: Standardizing OpenTelemetry and TraceId propagation.

### Long-Term Vision

- **Modern Java Evolution**: Experimenting with Virtual Threads and Structured Concurrency.
- **Cross-Service Events**: Establishing ADR-0008 for event-driven coordination.
