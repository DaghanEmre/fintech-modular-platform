---
trigger: model_decision
description: Apply when writing, reviewing, or refactoring code, domain models, or implementation details. Skip for high-level vision, roadmap, or purely conceptual discussions.
---

Coding Principles:

- No anemic domain (behavior lives in the domain)
- No primitive obsession (use value objects)
- No infrastructure leakage into domain
- Domain must be framework-agnostic
- Aggregate invariants enforced inside the aggregate
- Explicit state transitions, no hidden side effects
- Test-first mindset for domain logic
- Favor clarity over cleverness