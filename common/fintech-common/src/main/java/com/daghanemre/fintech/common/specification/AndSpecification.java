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
        if (isSatisfiedBy(candidate)) {
            return SpecificationViolation.none();
        }

        // AND failed - return the FIRST failing specification's violation
        // We trust the specification contract: if isSatisfiedBy is false, violation is valid
        if (!left.isSatisfiedBy(candidate)) {
            return left.violation(candidate);
        }
        
        // Left passed, so right must have failed
        return right.violation(candidate);
    }
}
