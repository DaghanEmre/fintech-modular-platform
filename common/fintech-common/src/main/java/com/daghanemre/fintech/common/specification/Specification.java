package com.daghanemre.fintech.common.specification;

/**
 * Core specification interface for declarative domain rule evaluation.
 *
 * @param <T> the type of candidate to be evaluated
 */
public interface Specification<T> {

    /**
     * Evaluates whether the candidate satisfies this specification.
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * Returns a detailed violation report if the specification is not satisfied.
     */
    default SpecificationViolation violation(T candidate) {
        return SpecificationViolation.none();
    }

    /**
     * Logical AND combination.
     */
    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }

    /**
     * Logical OR combination.
     */
    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }

    /**
     * Logical NOT negation.
     */
    default Specification<T> not() {
        return new NotSpecification<>(this);
    }
}
