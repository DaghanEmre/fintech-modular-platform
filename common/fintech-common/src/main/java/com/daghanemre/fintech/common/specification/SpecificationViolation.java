package com.daghanemre.fintech.common.specification;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a specification violation with detailed error information.
 *
 * <p>Immutable and context-aware value object following Null Object Pattern.
 * 
 * <p><b>Usage:</b>
 * <ul>
 *   <li>{@code new SpecificationViolation(code, message)} - Standard violation</li>
 *   <li>{@code new SpecificationViolation(code, message, context)} - With diagnostic context</li>
 *   <li>{@code SpecificationViolation.none()} - Null object (no violation)</li>
 * </ul>
 */
public record SpecificationViolation(
        String code,
        String message,
        Map<String, Object> context) {

    /**
     * Singleton null object representing "no violation".
     * Avoids repeated object allocation.
     */
    private static final SpecificationViolation NONE = 
        new SpecificationViolation(null, null, Collections.emptyMap());

    public SpecificationViolation {
        context = context == null ? Collections.emptyMap() : Map.copyOf(context);
    }

    public SpecificationViolation(String code, String message) {
        this(code, message, Collections.emptyMap());
    }

    /**
     * Returns the singleton null object representing "no violation".
     * 
     * @return singleton instance (memory-efficient)
     */
    public static SpecificationViolation none() {
        return NONE;
    }

    /**
     * Checks if this violation represents an actual failure.
     * 
     * @return true if code or message is present
     */
    public boolean isPresent() {
        return code != null || message != null;
    }
}
