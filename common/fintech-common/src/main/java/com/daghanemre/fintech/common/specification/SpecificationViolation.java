package com.daghanemre.fintech.common.specification;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a specification violation with detailed error information.
 *
 * <p>
 * Immutable and context-aware value object.
 */
public record SpecificationViolation(
        String code,
        String message,
        Map<String, Object> context) {

    public SpecificationViolation {
        context = context == null ? Collections.emptyMap() : Map.copyOf(context);
    }

    public SpecificationViolation(String code, String message) {
        this(code, message, Collections.emptyMap());
    }

    public static SpecificationViolation none() {
        return new SpecificationViolation(null, null, null);
    }

    public boolean isPresent() {
        return code != null || message != null;
    }
}
