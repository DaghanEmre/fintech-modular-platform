# Customer Service

## Overview
Customer management service for the FinTech Modular Platform. Handles customer lifecycle, profile management, and state transitions.

## Responsibilities
- Customer registration and activation
- Profile management
- State transitions (PENDING → ACTIVE → SUSPENDED → BLOCKED)
- KYC (Know Your Customer) verification (future)

## Technology Stack
- Java 17+
- Spring Boot 3.2.x
- PostgreSQL 15
- Testcontainers (integration testing)
- MapStruct (complex DTO mapping, when needed)
- Lombok (adapter layer only)

## Architecture

This service follows **Hexagonal Architecture** (Ports & Adapters):

```
src/main/java/com/daghanemre/fintech/customer/
├── domain/                    # Pure domain model (no framework dependencies)
│   ├── model/                 # Aggregates, Value Objects, Enums
│   └── port/                  # Repository interfaces (ports)
├── application/               # Use cases (framework-free orchestration)
│   ├── usecase/               # Business use cases
│   └── exception/             # Application-level exceptions
└── infrastructure/            # Framework-specific adapters
    ├── adapter/
    │   └── rest/              # Inbound REST adapter
    │       ├── controller/    # HTTP controllers
    │       ├── dto/           # Request/Response DTOs
    │       └── exception/     # Exception handlers
    ├── persistence/           # Outbound persistence adapter
    │   ├── entity/            # JPA entities
    │   ├── mapper/            # Domain ↔ Entity mapping
    │   └── repository/        # Spring Data repositories
    └── config/                # Spring configurations
```

## API Endpoints

### Customer Activation

```http
POST /api/v1/customers/{id}/activate
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Customer identifier |

**Response Codes:**
| Status | Description |
|--------|-------------|
| 204 | Customer activated successfully |
| 400 | Invalid UUID format |
| 404 | Customer not found |
| 409 | Invalid state transition (e.g., already active) |
| 410 | Customer is deleted |

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/customers/550e8400-e29b-41d4-a716-446655440000/activate
```

## Configuration Profiles

| Profile | Usage |
|---------|-------|
| `dev` | Local development (default) |
| `test` | Integration tests with Testcontainers |
| `prod` | Production environment |
| `local` | Personal developer overrides (git-ignored) |

### Local Developer Configuration

`application-local.yml` is **intentionally ignored** by git:

```bash
# Create your personal config
cp src/main/resources/application-dev.yml src/main/resources/application-local.yml

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Running the Service

### Prerequisites
- Java 17+
- Docker (for integration tests)
- PostgreSQL (for local development)

### Build
```bash
mvn clean compile
```

### Run
```bash
mvn spring-boot:run
```

### Test

```bash
# Unit tests only
mvn test -DskipITs

# Integration tests (requires Docker)
mvn test -Dtest=*IT

# All tests
mvn verify
```

## Integration Testing

This service uses **Testcontainers** for integration testing with real PostgreSQL.

### Setup

1. Ensure Docker is running:
   ```bash
   docker ps
   ```

2. Run integration tests:
   ```bash
   mvn test -Dtest=ActivateCustomerIT
   ```

### Test Coverage

| Category | Tests | Description |
|----------|-------|-------------|
| Happy Path | 2 | PENDING→ACTIVE, SUSPENDED→ACTIVE |
| Error Cases | 6 | 404, 400, 410, 409 scenarios |
| Persistence | 2 | Status change, audit timestamp |

For Docker setup details, see [Docker README](../../infrastructure/docker/README.md).

## Design Decisions

Refer to Architecture Decision Records:
- [ADR-0001: Domain Model Design](../../docs/adr/0001-domain-model-design.md)
- [ADR-0002: Infrastructure Layer Design](../../docs/adr/0002-infrastructure-layer-design.md)
- [ADR-0003: Application Layer Design](../../docs/adr/0003-application-layer-design.md)

## Key Design Choices

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Transaction boundary | Controller level | ADR-0003 compliance |
| UUID validation | Domain factory | Single source of truth |
| MapStruct usage | Conditional | Only for 5+ field mappings |
| Lombok usage | Adapter only | Domain stays pure Java |
