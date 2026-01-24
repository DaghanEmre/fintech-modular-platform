# Enum Guidelines - FinTech Modular Platform

This document provides quick reference guidelines for enum usage. See [ADR-0006](../adr/ADR-0006-enum-ownership.md) for full rationale.

## 🎯 Quick Decision Tree
Is this enum domain-specific?
- **YES** → Tier 1 (Service-Local)
- **NO** → Is it based on an external standard (ISO/RFC)?
    - **YES** → Tier 2 (Common Contract)
    - **NO** → Is it cross-service technical?
        - **YES** → Tier 2 (Common Contract)
        - **NO** → Tier 1 (Service-Local)

## 📋 Tier Classification
| Tier | Ownership | Location | Serialization | Evolution |
| :--- | :--- | :--- | :--- | :--- |
| **Tier 1** | Service-Local | `{service}/domain/model/` | String | Independent |
| **Tier 2** | Platform-Common | `fintech-common/contract/` | Enum or String | Coordinated |

## ✅ Tier 1: Domain-Internal Enums
- Place in the service's `domain/model/` package.
- **NEVER** expose the enum type in API contracts or events.
- **ALWAYS** use `String` serialization at boundaries.
- **ALWAYS** implement `safeParse(String)` to ensure forward compatibility.

## 📐 Naming Conventions
- **DO:** `CustomerStatus`, `PaymentStatus` (Clean domain names).
- **DON'T:** `CustomerStatusEnum`, `CustomerStatusDto` (Redundant suffixes).
- Clarity comes from the package structure and context, not the name suffix.
