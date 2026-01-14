# ADR-0004 Appendix A: Specification Naming Conventions

## Status
ACCEPTED

## Date
2026-01-13

## Context
As the Specification Pattern adoption spreads across microservices, inconsistent naming can lead to:
- Ambiguous intent (Is `CanXSpec` atomic or composite?)
- SRP violations (Atomic specs doing multiple checks)
- Non-deterministic violation codes (Metrics become unreliable)
- Difficult code reviews (Can't spot anti-patterns from names alone)

This appendix establishes **mandatory naming conventions** to ensure consistency, maintainability, and observability.

---

## Naming Taxonomy

### 1️⃣ Atomic Specifications: `IsXSpec` and `NotXSpec`

#### `IsXSpec` - Positive State Assertion
**Purpose:** Verify that an entity IS in a specific state or HAS a specific attribute.

**Characteristics:**
- Tests **ONE** condition only (SRP)
- No side effects
- Deterministic result
- Reusable in compositions

**Examples:**
```java
CustomerIsActiveSpec
CustomerIsPendingSpec
CustomerIsSuspendedSpec
EmailIsVerifiedSpec
AccountHasSufficientBalanceSpec
```

**Violation Code Pattern:**
```
{ENTITY}_NOT_{STATE}
```

**Examples:**
- `CUSTOMER_NOT_ACTIVE`
- `CUSTOMER_NOT_PENDING`
- `EMAIL_NOT_VERIFIED`

---

#### `NotXSpec` - Negative Invariant
**Purpose:** Verify that an entity is NOT in a forbidden state.

**Rule of Thumb:** `NotXSpec` = "This entity MUST NEVER be in state X"

**Examples:**
```java
CustomerNotDeletedSpec
CustomerNotBlockedSpec
AccountNotFrozenSpec
```

**Violation Code Pattern:**
```
{ENTITY}_{FORBIDDEN_STATE}
```

**Examples:**
- `CUSTOMER_DELETED`
- `CUSTOMER_BLOCKED`
- `ACCOUNT_FROZEN`

**Design Note:**  
`NotXSpec` is semantically stronger than `IsXSpec.not()`:
```java
// Prefer explicit NotXSpec
new CustomerNotDeletedSpec()

// Avoid NOT composition (less semantic)
new CustomerIsDeletedSpec().not()
```

---

### 2️⃣ Semantic Wrapper Specifications: `CanXSpec`

#### `CanXSpec` - Multi-Rule Business Intent
**Purpose:** Combine multiple atomic rules into a single business-meaningful check.

**Key Properties:**
- **NOT atomic** (delegates to atomic specs)
- Returns **domain-specific** violation codes (not generic composite codes)
- Named after **business operation**, not technical state
- Lives in `domain/specification/` package

**Examples:**
```java
CustomerCanBeActivatedSpec
CustomerCanBeSuspendedSpec
CustomerCanChangeEmailSpec
PaymentCanBeProcessedSpec
AccountCanBeClosedSpec
```

**Violation Code Pattern:**
```
{DOMAIN_SPECIFIC_ERROR}
```

**Examples:**
- `INVALID_STATUS_TRANSITION` (not `CUSTOMER_NOT_PENDING`)
- `CUSTOMER_ALREADY_ACTIVE` (not `SPEC_OR_FAILED`)
- `INSUFFICIENT_BALANCE` (not `ACCOUNT_NOT_FUNDED`)

**Implementation Pattern:**
```java
public final class CustomerCanBeActivatedSpec implements Specification<Customer> {
    
    // Delegate to atomic specs
    private final Specification<Customer> notDeleted = new CustomerNotDeletedSpec();
    private final Specification<Customer> notBlocked = new CustomerNotBlockedSpec();
    
    @Override
    public boolean isSatisfiedBy(Customer customer) {
        // Combine atomic checks
        return notDeleted.isSatisfiedBy(customer)
            && notBlocked.isSatisfiedBy(customer)
            && (customer.getStatus() == PENDING || customer.getStatus() == SUSPENDED);
    }
    
    @Override
    public SpecificationViolation violation(Customer customer) {
        // Priority-based semantic violation
        if (customer.isDeleted()) {
            return new SpecificationViolation("CUSTOMER_DELETED", "...");
        }
        if (customer.getStatus() == BLOCKED) {
            return new SpecificationViolation("CUSTOMER_BLOCKED", "...");
        }
        if (customer.getStatus() == ACTIVE) {
            return new SpecificationViolation("CUSTOMER_ALREADY_ACTIVE", "...");
        }
        // Semantic error, not generic OR failure
        return new SpecificationViolation("INVALID_STATUS_TRANSITION", "...");
    }
}
```

**When to Use Semantic Wrappers:**
- ✅ OR composition with ambiguous failure semantics
- ✅ Business operation that combines multiple rules
- ✅ When generic `SPEC_OR_FAILED` is not actionable

**When NOT to Use:**
- ❌ Simple AND composition (use raw composition)
- ❌ Single atomic check (use `IsXSpec` or `NotXSpec`)

---

### 3️⃣ Factory Methods: `canX()`

#### Purpose
Expose domain rules through intention-revealing factory methods.

**Naming:**
- Method name: `canX()` (lowercase)
- Returns: `Specification<T>` (may be semantic wrapper or raw composition)

**Examples:**
```java
public final class CustomerSpecifications {
    
    public static Specification<Customer> canBeActivated() {
        return new CustomerCanBeActivatedSpec(); // Semantic wrapper
    }
    
    public static Specification<Customer> canChangeEmail() {
        return new CustomerCanChangeEmailSpec(); // Semantic wrapper
    }
    
    public static Specification<Customer> canBeBlocked() {
        return new CustomerCanBeBlockedSpec(); // Simple wrapper
    }
}
```

**Why Factory Methods?**
- ✅ Aggregate methods don't import concrete spec classes
- ✅ Encapsulates composition logic
- ✅ Easier to refactor (change implementation without breaking clients)

---

## Anti-Patterns

### ❌ ANTI-PATTERN 1: Atomic `CanXSpec`
```java
// WRONG - "Can" implies multiple conditions, but class is atomic
public class CustomerCanBeActiveSpec implements Specification<Customer> {
    public boolean isSatisfiedBy(Customer c) {
        return c.getStatus() == ACTIVE; // Single check
    }
}
```

**Why Wrong:**
- Misleading name (sounds like multi-rule check)
- Use `CustomerIsActiveSpec` instead

---

### ❌ ANTI-PATTERN 2: Conditional Logic in Atomic Specs
```java
// WRONG - Atomic spec doing multiple checks
public class CustomerIsEligibleSpec implements Specification<Customer> {
    public boolean isSatisfiedBy(Customer c) {
        if (c.isDeleted()) return false;
        if (c.getStatus() == BLOCKED) return false;
        return c.getStatus() == ACTIVE || c.getStatus() == PENDING;
    }
}
```

**Why Wrong:**
- Violates SRP
- Multiple violation scenarios → ambiguous error code
- Not reusable

**Fix:** Use composition or semantic wrapper:
```java
// Atomic specs
new CustomerNotDeletedSpec()
    .and(new CustomerNotBlockedSpec())
    .and(new CustomerIsActiveSpec().or(new CustomerIsPendingSpec()))

// Or semantic wrapper
new CustomerCanBeActivatedSpec()
```

---

### ❌ ANTI-PATTERN 3: Generic Violation Codes in Semantic Wrappers
```java
// WRONG - Semantic wrapper returning generic code
public SpecificationViolation violation(Customer c) {
    return new SpecificationViolation("SPEC_OR_FAILED", "...");
}
```

**Why Wrong:**
- Defeats the purpose of semantic wrappers
- Metrics become useless
- HTTP clients can't handle error properly

**Fix:** Return domain-specific codes:
```java
return new SpecificationViolation("INVALID_STATUS_TRANSITION", "...");
```

---

## Naming Cheat Sheet

| Pattern | Type | Example | Violation Code |
|---------|------|---------|----------------|
| `IsXSpec` | Atomic | `CustomerIsActiveSpec` | `CUSTOMER_NOT_ACTIVE` |
| `NotXSpec` | Atomic | `CustomerNotDeletedSpec` | `CUSTOMER_DELETED` |
| `CanXSpec` | Semantic Wrapper | `CustomerCanBeActivatedSpec` | `INVALID_STATUS_TRANSITION` |
| `canX()` | Factory Method | `CustomerSpecifications.canBeActivated()` | (returns wrapper) |

---

## Enforcement

### Build-Time Enforcement (ArchUnit)
See `SpecificationContractTest` for automated enforcement:
- All concrete specifications MUST override `violation()`
- Naming patterns are validated in code review

### Code Review Checklist
- [ ] Atomic specs use `IsXSpec` or `NotXSpec` naming
- [ ] Semantic wrappers use `CanXSpec` naming
- [ ] No `CanXSpec` for single-condition checks
- [ ] Violation codes are domain-specific (not `SPEC_OR_FAILED`)
- [ ] Factory methods use lowercase `canX()` naming

---

## Migration Guide

### Existing Code
If you have existing specifications with non-standard names:

1. **Refactor in phases** (don't break existing tests)
2. **Rename classes** to follow conventions:
   - `CustomerActiveSpec` → `CustomerIsActiveSpec`
   - `CustomerCanActivate` → `CustomerCanBeActivatedSpec`
3. **Update imports** in aggregate classes
4. **Run full test suite** to verify behavior unchanged

### New Code
All new specifications MUST follow these conventions from day one.

---

## References
- [ADR-0004: Specification Pattern](./ADR-0004-specification-pattern.md)
- [Eric Evans - Specification Pattern](https://www.martinfowler.com/apsupp/spec.pdf)
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)

---

## Revision History
- 2026-01-13: Initial naming conventions (based on customer-service implementation)
