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
        // Only return none if EITHER is satisfied.
        // If both fail, we don't have a specific policy on which violation to return,
        // but typically OR specs are followed by more specific checks or atomic specs.
        return SpecificationViolation.none();
    }
}
