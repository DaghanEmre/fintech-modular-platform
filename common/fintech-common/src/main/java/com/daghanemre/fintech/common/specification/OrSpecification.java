package com.daghanemre.fintech.common.specification;

import java.util.Objects;
import java.util.Map;

/**
 * Composite specification for logical OR.
 * 
 * <p><b>Contract:</b> If {@code isSatisfiedBy} returns false, 
 * {@code violation} MUST return a valid violation (never null/empty).
 * 
 * <p><b>Violation Strategy:</b>
 * When BOTH sub-specifications fail, this returns a composite violation
 * containing both failure details. This is necessary because OR semantics
 * mean "neither condition was satisfied".
 * 
 * <p><b>Design Trade-off & Usage Guidance:</b>
 * This generic OR implementation returns {@code SPEC_OR_FAILED} with both
 * sub-violations in context. This is appropriate for:
 * <ul>
 *   <li>Framework-level composition (low-level utility specs)</li>
 *   <li>Debugging (need to see both failure reasons)</li>
 *   <li>Non-domain technical validations</li>
 * </ul>
 * 
 * <p><b>For domain business rules:</b> Prefer semantic wrapper specifications
 * that return domain-specific error codes (e.g., {@code INVALID_STATUS_TRANSITION})
 * instead of generic composite codes. See ADR-0004 Appendix A for guidance.
 * 
 * <p><b>Strategic Recommendation:</b>
 * <pre>
 * // ❌ AVOID in domain layer - generic code not actionable
 * new CustomerIsPendingSpec().or(new CustomerIsSuspendedSpec())
 * // Returns: SPEC_OR_FAILED (not helpful for clients)
 * 
 * // ✅ PREFER semantic wrapper - explicit domain code
 * new CustomerCanBeActivatedSpec()
 * // Returns: INVALID_STATUS_TRANSITION (actionable)
 * </pre>
 */
public final class OrSpecification<T> implements Specification<T> {

    private final Specification<T> left;
    private final Specification<T> right;

    public OrSpecification(Specification<T> left, Specification<T> right) {
        this.left = Objects.requireNonNull(left, "left specification must be provided");
        this.right = Objects.requireNonNull(right, "right specification must be provided");
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
    }

    @Override
    public SpecificationViolation violation(T candidate) {
        if (isSatisfiedBy(candidate)) {
            return SpecificationViolation.none();
        }

        // Both failed - create composite violation with both failure codes
        SpecificationViolation leftViolation = left.violation(candidate);
        SpecificationViolation rightViolation = right.violation(candidate);
        
        return new SpecificationViolation(
            "SPEC_OR_FAILED",
            String.format("Neither condition satisfied: [%s] AND [%s]", 
                leftViolation.message(), 
                rightViolation.message()),
            Map.of(
                "leftCode", leftViolation.code(),
                "rightCode", rightViolation.code(),
                "leftMessage", leftViolation.message(),
                "rightMessage", rightViolation.message()
            )
        );
    }
}
