# ADR-0008: Spring Boot Upgrade Strategy & Dependency Governance

## Status
Accepted

## Date
2026-05-13

## Context

Spring Boot 3.2.x reached the end of its Open Source Support (OSS) on **2024-12-31**. Continued use introduces unpatched CVEs, missing bug fixes, and incompatibility with actively maintained libraries in the Spring ecosystem.

Additionally, the platform's prior dependency model was fragmented:
- Each service declared versions independently via `spring-boot-starter-parent` inheritance
- Testcontainers, ArchUnit, and MapStruct versions were hardcoded per module
- No central enforcement point existed for dependency drift

This ADR documents the decision to upgrade Spring Boot and restructure dependency governance as a platform-level concern.

---

## Decision

**Upgrade to Spring Boot 3.5.14 and Java 21, and adopt an explicit BOM-based dependency governance model.**

Specifically:

1. **Version Catalog in Root POM**: All dependency and plugin versions live in `<properties>` of the root `pom.xml` as the single source of truth.
2. **Explicit BOM Strategy** (see "Why BOM Instead of Parent Inheritance?" below).
3. **Java 21 (LTS) Baseline**: Enables virtual threads, pattern matching, record patterns, and modern performance improvements.
4. **Plugin Governance**: `maven-compiler-plugin`, `maven-surefire-plugin`, and `maven-failsafe-plugin` centralized in root `<pluginManagement>`.
5. **Compiler Flags**: `--release ${java.version}` replaces `source/target` pair; `parameters=true` added for Spring MVC and JPA proxy compatibility.

---

## Why BOM Instead of Parent Inheritance?

| Approach | How It Works | Problem |
|---|---|---|
| `spring-boot-starter-parent` as parent | Each service inherits Spring's opinionated defaults directly | Tightly couples each service to Spring's build lifecycle; blocks custom parent hierarchy |
| Root POM as parent + `spring-boot-dependencies` BOM imported | Spring version managed via `dependencyManagement`; services inherit from platform root | Platform owns governance; Spring is a versioned dependency, not a structural parent |

The distinction matters at platform scale:

- With parent inheritance, adding a second BOM (e.g., Testcontainers, Quarkus-compat) requires careful ordering and creates implicit coupling.
- With explicit BOM import, the platform root controls the full dependency resolution order.
- `fintech-common` remains a **pure Java module** with zero Spring coupling at runtime — impossible to enforce cleanly if every module must inherit from `spring-boot-starter-parent`.

---

## Annotation Processor Order (Critical)

Incorrect processor ordering in `maven-compiler-plugin` causes **silent compile success with broken mapper output at runtime**. The correct order is:

```xml
<annotationProcessorPaths>
    <!-- 1. Lombok first — generates boilerplate (getters/setters/builders) -->
    <path>org.projectlombok:lombok:${lombok.version}</path>
    <!-- 2. Binding bridge — ensures MapStruct sees Lombok-generated methods -->
    <path>org.projectlombok:lombok-mapstruct-binding:${lombok-mapstruct-binding.version}</path>
    <!-- 3. MapStruct last — generates mappers using Lombok-generated accessors -->
    <path>org.mapstruct:mapstruct-processor:${mapstruct.version}</path>
</annotationProcessorPaths>
```

Reversing steps 1 and 3 results in mapper methods that compile but silently produce `null` or incomplete mappings at runtime.

---

## Consequences

### Positive
- **Security**: Eliminates exposure from EOL Spring Boot 3.2.x.
- **Consistency**: Version drift between services is structurally prevented.
- **Maintainability**: Upgrading the entire platform requires a single change in the root POM.
- **DDD Purity**: `fintech-common` remains framework-free — enforced by the module structure, not convention.
- **Compiler Correctness**: `--release` flag prevents accidental use of newer JDK APIs; `parameters=true` removes reflection ambiguity.

### Risks

| Risk | Severity | Mitigation |
|---|---|---|
| Spring Security `SecurityFilterChain` behavior change | High | Gate 3 (ArchUnit) + Gate 5 (Integration) |
| Jackson enum serialization default drift | Medium | Gate 4: `CustomerStatusSerializationTest` |
| Hibernate Validator / `jakarta.validation` version change | Medium | Gate 5: Integration tests with real DB |
| Annotation processor order breaks mapper generation | High | Enforced and documented in compiler config |
| `parameters=true` flag not applied transitively | Low | Centralized in root `pluginManagement` |
| Actuator / Micrometer API change | Medium | Gate 6: Observability smoke test |

---

## Upgrade Validation Pipeline

A 6-gate sequential validation ensures upgrade correctness. Each gate targets a distinct failure class.

### Gate 1 — Dependency Resolution
```bash
mvn dependency:tree -Dincludes=org.springframework
```
**Validates**: No old Spring 3.2.x artifacts remain; no transitive version conflicts.

### Gate 2 — Compile
```bash
mvn clean compile
```
**Validates**: All modules compile under Java 21 with `--release 21`.

### Gate 3 — Architecture Integrity
```bash
mvn test -Dtest="*ArchTest,*ArchitectureTest"
```
**Validates**: Hexagonal boundaries, domain purity, Specification pattern usage. See ADR-0007.

### Gate 4 — Behavioral (Domain + Jackson)
```bash
mvn test -Dtest="CustomerStatusSerializationTest,CustomerStatusTest"
```
**Validates**: Enum serialization stability, `safeParse` contract, Jackson deserialization drift.

### Gate 5 — Integration (Persistence + Runtime)
```bash
mvn verify
```
**Validates**: Testcontainers-backed DB tests, Spring context, JPA/Hibernate, REST endpoints. `maven-failsafe-plugin` execution is bound to `verify` phase — no separate profile needed.

### Gate 6 — Observability
```bash
# Note: CI smoke tests run on port 8081 to avoid local port conflicts.
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/metrics
```
**Validates**: Actuator endpoint exposure, metric tag stability (`application: customer-service`), tracing propagation.

---

## Observability Compatibility Requirements

Following this upgrade, the following Micrometer/Actuator contracts must remain stable:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: customer-service
  tracing:
    enabled: true
```

Verify post-upgrade:
- Metric names follow existing conventions (no silent renaming in Micrometer 1.13+)
- `application` tag present on all emitted metrics
- TraceID propagation works through Spring MVC filter chain (see ADR-0005)

---

## Future Evolution

| Concern | Recommendation | Priority |
|---|---|---|
| CI enforcement | Add ArchUnit + Integration + Observability gates to GitHub Actions | High |
| Dependency governance enforcement | Add `maven-enforcer-plugin` (Java 21 required, no duplicate deps, dependency convergence) | High |
| Supply-chain compliance | Add CycloneDX SBOM generation (`cyclonedx-maven-plugin`) | Medium |
| Unknown enum handling | Evaluate `@JsonEnumDefaultValue` on `UNKNOWN` sentinel — track in ADR-0006 | Low |

---

## References

- [Spring Boot 3.5.14 Release Notes](https://spring.io/blog/2026/04/23/spring-boot-3-5-14-available-now)
- [Spring Boot 3.2.x EOL](https://spring.io/projects/spring-boot#support)
## Related ADRs

- [ADR-0001: Hexagonal Architecture & Port/Adapter Model](file:///c:/Users/dagha/workspace/fintech-modular-platform/docs/adr/0001-use-hexagonal-architecture.md)
- [ADR-0002: Customer Domain Model Design](file:///c:/Users/dagha/workspace/fintech-modular-platform/docs/adr/0002-customer-domain-model-design.md)
- [ADR-0003: Application Layer Design](file:///c:/Users/dagha/workspace/fintech-modular-platform/docs/adr/0003-application-layer-design.md)
- [ADR-0004: Specification Pattern](file:///c:/Users/dagha/workspace/fintech-modular-platform/docs/adr/ADR-0004-specification-pattern.md)
- [ADR-0005: Metrics & Observability Strategy](file:///c:/Users/dagha/workspace/fintech-modular-platform/docs/adr/ADR-0005-metrics-observability.md)
- [ADR-0006: Enum Ownership & Serialization Strategy](file:///c:/Users/dagha/workspace/fintech-modular-platform/docs/adr/ADR-0006-enum-ownership.md)
- [ADR-0007: Architecture Guardrails (ArchUnit)](file:///c:/Users/dagha/workspace/fintech-modular-platform/docs/adr/ADR-0007-architecture-guardrails.md)
