package com.daghanemre.fintech.common.specification;

import java.util.Objects;

/**
 * Exception thrown when a domain specification is violated.
 */
public class SpecificationException extends RuntimeException {

    private final SpecificationViolation violation;

    public SpecificationException(SpecificationViolation violation) {
        super(Objects.requireNonNull(violation, "violation must not be null").message());
        this.violation = violation;
    }

    public SpecificationException(String message, SpecificationViolation violation) {
        super(message);
        this.violation = Objects.requireNonNull(violation, "violation must not be null");
    }

    public SpecificationViolation getViolation() {
        return violation;
    }

    public String getCode() {
        return violation.code();
    }
}
