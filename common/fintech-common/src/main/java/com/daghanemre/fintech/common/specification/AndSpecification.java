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

        // If not satisfied, at least one of the sub-specifications must be violated.
        // We prioritize the left violation if it exists.
        if (!left.isSatisfiedBy(candidate)) {
            SpecificationViolation violation = left.violation(candidate);
            if (violation.isPresent()) {
                return violation;
            }
        }

        // If left was satisfied or didn't provide a violation, check the right.
        if (!right.isSatisfiedBy(candidate)) {
            SpecificationViolation violation = right.violation(candidate);
            if (violation.isPresent()) {
                return violation;
            }
        }
        
        // Fallback for unexpected consistency issues between isSatisfiedBy and violation
        return new SpecificationViolation(
                "SPEC_AND_FAILED",
                "Both parts of AND failed to provide a violation code.");
    }
}
