# 2. Customer Domain Model Design

**Date:** 2025-12-27

**Status:** Accepted

## Context

We need to design the Customer aggregate following Domain-Driven Design principles while preparing for future event-driven architecture and maintaining regulatory compliance requirements for FinTech.

Key requirements:
- Customer identity must be domain-generated (not database-dependent)
- Audit fields are business requirements, not technical metadata
- Aggregate must be complete before persistence
- Prepare for event-driven communication without implementing it now

## Decision

### Aggregate Root
**Customer** is the only aggregate root in the Customer bounded context.

### Identity Strategy
- **CustomerId**: UUID-based value object
- **Generation**: Domain-generated using `UUID.randomUUID()` (v4)
- **Future consideration**: UUID v7 for better database index performance

**Why domain-generated?**
- Aggregate is complete before persistence
- Enables event-driven architecture
- No database coupling
- Testable without infrastructure

### Value Objects
- **CustomerId**: UUID wrapper with validation
- **Email**: RFC-compliant validation with normalization

### Customer Status
Enum with FinTech-appropriate states:
```java
enum CustomerStatus {
    PENDING,      // KYC pending
    ACTIVE,       // Normal operations
    SUSPENDED,    // Temporarily restricted (fraud investigation)
    INACTIVE,     // Customer-initiated closure
    BLOCKED       // Permanent ban (AML/compliance)
}
```

### Audit Fields as Domain Concern
Audit fields are **business requirements**, not technical metadata:
```java
private final LocalDateTime createdAt;
private LocalDateTime updatedAt;
private LocalDateTime deletedAt;  // Soft delete
```

**Why domain concern?**
- Regulatory requirement (financial compliance, GDPR)
- Soft delete is business logic
- Domain behaviors update timestamps explicitly
- Example: `changeEmail()` updates `updatedAt`

**JPA Auditing (@CreatedDate, @LastModifiedDate) is explicitly NOT used:**
- Domain controls its own lifecycle
- JPA only maps to database columns
- No hidden framework magic

### Time Management (Domain currently acts as its own time source)
- **Current**: Direct `LocalDateTime.now()` usage
- **Rationale**: Simple, sufficient for current requirements
- **Future**: Clock abstraction if SLA-based logic or complex time-window calculations are needed

### Domain Events (Prepared, Not Implemented)
- Package `domain/event/` created
- Not implementing events yet
- Preparing for future event-driven communication:
  - CustomerCreated
  - CustomerEmailChanged
  - CustomerDeactivated

### Factory Methods
Two distinct creation patterns:
```java
public static Customer create(...)        // New aggregate
public static Customer reconstitute(...)  // From persistence
```

This separation is critical for:
- Event sourcing
- Snapshot restoration
- Clear persistence mapping

## Consequences

### Positive
- **Type Safety**: No primitive obsession (UUID, Email)
- **Testability**: Domain logic testable without database
- **Compliance Ready**: Audit fields support regulatory requirements
- **Event-Driven Ready**: Structure supports future events
- **Clear Boundaries**: Aggregate controls all state transitions
- **Hexagonal Alignment**: Domain has zero infrastructure dependencies

### Negative
- **More Classes**: More boilerplate than anemic domain model
- **UUID Index Performance**: v4 UUIDs are not index-friendly (future: v7)
- **Learning Curve**: Team must understand DDD patterns

### Neutral
- **Time Abstraction Deferred**: Using `LocalDateTime.now()` directly
- **Status Transition Rules**: Not enforced yet (future enhancement)

## Alternatives Considered

### 1. Database-Generated Identity (e.g., @GeneratedValue)
**Rejected because:**
- Aggregate incomplete before persistence
- Violates DDD principle of domain independence
- Difficult for event-driven architecture
- Couples domain to infrastructure

### 2. JPA Auditing (@CreatedDate, @LastModifiedDate)
**Rejected because:**
- Hides domain behavior
- Audit is business requirement in FinTech, not technical detail
- Domain should explicitly control its lifecycle
- Makes testing harder (requires Spring context)

### 3. String-Based IDs
**Rejected because:**
- Primitive obsession
- No type safety
- No self-validation

### 4. Anemic Domain Model
**Rejected because:**
- Violates DDD principles
- Business logic scattered across service layer
- Harder to maintain invariants

## Future Enhancements

### Planned
1. **UUID v7**: Consider migration for better index performance
2. **Status Transition Rules**: Enforce valid state transitions
3. **Domain Events**: Implement and publish events
4. **Clock Abstraction**: If time-based SLA logic is needed
5. **Explicit Status Transition Rules**
   - Prevent invalid transitions (e.g., BLOCKED → ACTIVE)
   - Centralize state transition validation inside aggregate

### Deferred
- Complex validation rules (fraud scoring, duplicate detection)
- Customer merge logic

## References
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)
- [Implementing Domain-Driven Design by Vaughn Vernon](https://vaughnvernon.com/)
- [UUID v7 Specification](https://datatracker.ietf.org/doc/html/draft-peabody-dispatch-new-uuid-format)
