package com.daghanemre.fintech.common.specification;

import java.util.Objects;

/**
 * Composite specification for logical NOT.
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

        // Negated specification failed, meaning the wrapped spec IS satisfied.
        return new SpecificationViolation(
                "SPEC_NOT_FAILED",
                "Negated rule is satisfied: " + wrapped.getClass().getSimpleName());
    }
}
