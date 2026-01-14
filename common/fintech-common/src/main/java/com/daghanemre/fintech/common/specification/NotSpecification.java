package com.daghanemre.fintech.common.specification;

import java.util.Objects;
import java.util.Map;

/**
 * Composite specification for logical NOT.
 * 
 * <p><b>Contract:</b> If {@code isSatisfiedBy} returns false, 
 * {@code violation} MUST return a valid violation (never null/empty).
 * 
 * <p><b>Violation Strategy:</b>
 * Returns a generic negation violation with context about the wrapped specification.
 * 
 * <p><b>Design Recommendation:</b>
 * Prefer explicit atomic specifications over NOT composition for better semantics:
 * <pre>
 * // Anti-pattern
 * new CustomerIsActiveSpec().not()
 * 
 * // Better - explicit domain meaning
 * new CustomerNotActiveSpec()
 * </pre>
 */
public final class NotSpecification<T> implements Specification<T> {

    private final Specification<T> wrapped;

    public NotSpecification(Specification<T> wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped specification must be provided");
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return !wrapped.isSatisfiedBy(candidate);
    }

    @Override
    public SpecificationViolation violation(T candidate) {
        if (isSatisfiedBy(candidate)) {
            return SpecificationViolation.none();
        }

        // NOT failed - the wrapped condition WAS satisfied (but shouldn't be)
        // We don't call wrapped.violation() because it WAS satisfied
        return new SpecificationViolation(
            "SPEC_NOT_FAILED",
            "Negated condition must NOT be satisfied",
            Map.of(
                "wrappedSpecification", wrapped.getClass().getSimpleName(),
                "hint", "Consider using an explicit atomic specification instead of NOT composition"
            )
        );
    }
}
