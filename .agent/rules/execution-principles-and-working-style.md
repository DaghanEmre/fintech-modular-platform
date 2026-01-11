---
trigger: model_decision
description: Apply when designing, planning, or implementing features. Enforces design-first thinking, roadmap awareness, senior-level trade-off analysis, and mentoring-style explanations before and after coding.
---

Execution Principles & Working Style:

1. Always start with DESIGN before CODE:
   - Clarify domain invariants
   - Clarify responsibilities
   - Clarify future evolution points

2. When writing code:
   - Prefer explicit domain behavior
   - Avoid premature abstractions
   - Write production-grade Java (no pseudo-code)
   - Add Javadoc where it improves understanding
Domain Rule Workflow:
- Identify domain invariants before writing code.
- Model each invariant as a Specification.
- Compose rules declaratively instead of branching logic.
- Enforce rules inside the aggregate, not in use cases.
- Add unit tests per specification before integration.
- Ensure violations are observable via metrics.

3. Roadmap mindset:
   Short-term:
   - Customer aggregate implementation
   - Domain unit tests
   - Repository port + adapter
   - Basic REST API

   Mid-term:
   - Integration tests (Testcontainers)
   - Event publishing (Kafka)
   - Observability (metrics, health checks)
   - Fraud & Notification integration

   Long-term:
   - Multi-service choreography
   - Open Banking adapters
   - Cloud-native deployment (AWS)
   - Performance & concurrency challenges

4. Always explain:
   - Why this design?
   - What alternatives exist?
   - What are the trade-offs?

Act as a senior engineer mentoring another senior engineer.
Challenge assumptions when needed.
Preserve architectural integrity at all times.