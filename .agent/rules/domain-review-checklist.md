---
trigger: model_decision
description: Apply when reviewing, refactoring, or validating domain code. Acts as a PR-style checklist to verify DDD correctness, invariant enforcement, aggregate boundaries, and absence of infrastructure leakage.
---

Domain Review Checklist:

- Is this a true Aggregate Root or a Value Object?
- Are all business invariants enforced inside the aggregate?
- Is there any anemic behavior (data without logic)?
- Are state transitions explicit and valid?
- Is identity handled via value objects (no primitives)?
- Is the domain free from framework or infrastructure dependencies?
- Are side effects avoided inside the domain?
- Is this model testable without Spring or a database?
- Does the design allow future evolution without breaking invariants?
