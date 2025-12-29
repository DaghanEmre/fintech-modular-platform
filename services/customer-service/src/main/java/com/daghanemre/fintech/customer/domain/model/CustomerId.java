package com.daghanemre.fintech.customer.domain.model;

import java.util.UUID;

/**
 * Value Object representing the unique identity of a Customer.
 *
 * Identity is generated inside the domain to ensure that:
 * - The aggregate is complete before persistence
 * - The domain is not coupled to infrastructure or database concerns
 * - Event-driven architecture can be supported
 *
 * UUID v4 is used by default.
 * Future optimization: UUID v7 for better index locality.
 */
public record CustomerId(UUID value) {

    /**
     * Compact constructor validating that the UUID value is not null.
     *
     * @throws IllegalArgumentException if value is null
     */
    public CustomerId {
        if (value == null) {
            throw new IllegalArgumentException("CustomerId cannot be null");
        }
    }

    /**
     * Generates a new CustomerId using a random UUID (v4).
     *
     * This is the primary way to create identities for new Customer aggregates.
     *
     * @return a new CustomerId instance
     */
    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    /**
     * Recreates a CustomerId from a String representation.
     *
     * Intended for API and persistence layers to convert string-based
     * identifiers back into domain objects.
     *
     * The method performs defensive validation and normalization:
     * - Rejects null or blank strings
     * - Trims whitespace
     * - Validates UUID format
     *
     * @param value UUID string
     * @return CustomerId instance
     * @throws IllegalArgumentException if value is null, blank, or not a valid UUID
     */
    public static CustomerId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId string cannot be null or blank");
        }

        try {
            return new CustomerId(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid CustomerId format: " + value, ex);
        }
    }

    /**
     * Creates a CustomerId from a UUID instance.
     *
     * Used primarily by infrastructure layer when reconstituting
     * domain objects from persistence.
     *
     * @param value UUID instance
     * @return CustomerId instance
     * @throws IllegalArgumentException if value is null
     */
    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }

    /**
     * Returns the string representation of this CustomerId.
     *
     * @return UUID as string
     */
    @Override
    public String toString() {
        return value.toString();
    }
}
