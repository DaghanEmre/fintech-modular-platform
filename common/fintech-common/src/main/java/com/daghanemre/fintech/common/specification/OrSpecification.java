package com.daghanemre.fintech.common.specification;

import java.util.Objects;

/**
 * Composite specification for logical OR.
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

        // If both failed, return the left violation as a representative failure.
        // In the future, we might want to combine violations for better diagnostics.
        return left.violation(candidate);
    }
}
