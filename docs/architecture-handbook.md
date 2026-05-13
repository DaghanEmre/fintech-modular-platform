---
title: Architecture Handbook
updated: 2026-05-13
status: living-document
---

# Architecture Handbook

This document is a curated architecture knowledge base for the FinTech Modular Platform. It summarizes accepted architectural decisions, implementation status, and strategic goals.

---

## 1. Project Overview
A living, portfolio-grade FinTech platform built in Java to continuously improve backend engineering skills.
- **NOT a tutorial project**: Designed to evolve and refactor over time.
- **Production-grade principles**: But experimental in approach.
- **Why FinTech?**: Realistic challenges like concurrency, consistency, and strict auditing.

## 2. Core Vision & Mindset
- **Evolution Over Perfection**: The system is not designed to be perfect from day one.
- **Senior-level Mindset**: Domain-Driven Design (DDD), Hexagonal Architecture, Event-Driven workflows.

## 3. Architectural Framework
### 3.1 Hexagonal Architecture (ADR-0001)
- **Domain at the Center**: Zero external dependencies.
- **Ports & Adapters**: Interfaces in domain, implementations in infrastructure.

### 3.2 Architecture Guardrails (ADR-0007)
We use **ArchUnit** for automated architecture testing to prevent architectural drift.
- **Hard Rules**: Layer isolation, immutability, naming conventions.
- **Soft Rules**: Semantic intent (Use cases not evaluating specs), manual code review.

## 4. Domain-Driven Design Rules
### 4.1 Specification Pattern (ADR-0004)
Business rules are expressed as composable, reusable specifications.
- **Aggregates enforce rules**: Use `ensure(spec)` guard methods.
- **Rule Violations**: First-class concepts (`SpecificationViolation`).
- **Purity**: Specifications must be side-effect free.

### 4.2 Enum Ownership (ADR-0006)
- **Tier 1 (Domain-Internal)**: Never shared across boundaries as types.
- **Tier 2 (Contract)**: Shared in `fintech-common` (e.g., ISO standards).
- **Tier 3 (Externalized)**: String serialization + safe parsing.

## 5. Observability & Diagnostics (ADR-0005)
- **Metrics-First Strategy**: Domain rule violations increment `domain.violation.total`.
- **Violation Codes**: Used as primary dimension (tags).
- **Correlation**: TraceId propagation across logs and responses.

## 6. Strategic Goals & Roadmap
### Current Priorities (Sprint 1-2)
- **Architecture Hardening**: Enforcing ArchUnit guardrails across services.
- **Domain Completeness**: Implementing reversible business flows (ACTIVE ↔ SUSPENDED).
- **Distributed Tracing**: Standardizing OpenTelemetry and TraceId propagation.

### Long-Term Vision
- **Modern Java Evolution**: Experimenting with Virtual Threads and Structured Concurrency.
- **Cross-Service Events**: Establishing ADR-0008 for event-driven coordination.
