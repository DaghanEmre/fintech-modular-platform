# 1. Adopt Hexagonal Architecture with Ports and Adapters

**Date:** 2025-12-27

**Status:** Accepted

## Context

We are building a modular FinTech platform with multiple microservices. Each service needs to:
- Be independently deployable
- Have clear boundaries and responsibilities
- Be testable without external dependencies
- Support different infrastructure choices (database, messaging, etc.)

Traditional layered architecture often leads to:
- Strong coupling between business logic and infrastructure
- Difficulty in testing domain logic independently
- Database-driven design instead of domain-driven design
- Unclear separation of concerns

## Decision

We will adopt **Hexagonal Architecture (Ports and Adapters)** for all microservices, starting with Customer Service.

### Key Principles:
1. **Domain at the Center**: Business logic has no dependencies on external frameworks
2. **Ports**: Interfaces defined in the domain layer
3. **Adapters**: Implementations in infrastructure layer
4. **Dependency Rule**: Dependencies point inward (Infrastructure → Domain)

### Package Structure:
```
com.daghanemre.fintech.customer/
├── domain/
│   ├── model/           # Entities, Value Objects, Aggregates
│   ├── port/            # Repository interfaces (ports)
│   └── service/         # Domain services
├── application/
│   └── service/         # Use cases, orchestration
├── infrastructure/
│   └── persistence/     # JPA implementations (adapters)
└── api/
    ├── controller/      # REST controllers (adapters)
    └── dto/             # API contracts
```

## Consequences

### Positive
- **Testability**: Domain logic can be tested with simple mocks
- **Flexibility**: Easy to swap database or add new adapters (e.g., Kafka)
- **Independence**: Domain doesn't know about Spring, JPA, or HTTP
- **Clear Boundaries**: Each layer has well-defined responsibilities
- **DDD Alignment**: Natural fit for Domain-Driven Design

### Negative
- **More Boilerplate**: Need interfaces and implementations
- **Learning Curve**: Team needs to understand the pattern
- **Initial Overhead**: Takes longer to set up initially

### Neutral
- **Mapping Required**: DTO ↔ Domain mapping (we use MapStruct)
- **More Files**: More classes/interfaces than traditional layered

## Alternatives Considered

### 1. Traditional Layered Architecture
```
controller → service → repository → database
```
**Rejected because:**
- Tight coupling to infrastructure
- Difficult to test domain logic
- Database often drives the design

### 2. Clean Architecture (Uncle Bob)
**Very similar to Hexagonal, but:**
- More layers (Use Cases, Entities, Gateways)
- We chose Hexagonal for simplicity
- Can evolve to Clean Architecture if needed

### 3. Transaction Script
**Rejected because:**
- Suitable for simple CRUD
- Not scalable for complex business logic
- Poor fit for DDD

## References
- [Hexagonal Architecture by Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Get Your Hands Dirty on Clean Architecture](https://www.packtpub.com/product/get-your-hands-dirty-on-clean-architecture/9781839211966)
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)
