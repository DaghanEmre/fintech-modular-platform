package com.daghanemre.fintech.common.specification;

import java.util.Objects;

/**
 * Composite specification for logical AND.
 */
public final class AndSpecification<T> implements Specification<T> {

    private final Specification<T> left;
    private final Specification<T> right;

    public AndSpecification(Specification<T> left, Specification<T> right) {
        this.left = Objects.requireNonNull(left, "left specification must be provided");
        this.right = Objects.requireNonNull(right, "right specification must be provided");
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
    }

    @Override
    public SpecificationViolation violation(T candidate) {
        if (!left.isSatisfiedBy(candidate)) {
            return left.violation(candidate);
        }
        if (!right.isSatisfiedBy(candidate)) {
            return right.violation(candidate);
        }
        return SpecificationViolation.none();
    }
}
