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

Specification Pattern Usage:
- Do NOT implement business rules with nested if/else statements.
- Each business rule MUST be modeled as a Specification<T>.
- Specifications MUST be pure, stateless, and side-effect free.
- Composite rules MUST express domain semantics clearly.
- Avoid generic OR-composition when domain semantics require explicit meaning.
- Prefer semantic specifications (e.g. CustomerCanBeActivatedSpec) over boolean logic.
