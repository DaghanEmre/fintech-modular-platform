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

    private static final java.util.regex.Pattern UUID_PATTERN = java.util.regex.Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    /**
     * Compact constructor validating that the UUID value is not null.
     *
     * @throws IllegalArgumentException if value is null
     */
    public CustomerId {
        if (value == null) {
            throw new IllegalArgumentException("CustomerId cannot be null");
        }
        
        if (value.getMostSignificantBits() == 0 && value.getLeastSignificantBits() == 0) {
            throw new IllegalArgumentException("CustomerId cannot be Nil UUID (all zeros)");
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

        String trimmedValue = value.trim();
        if (!UUID_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("Invalid CustomerId format: " + value);
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(trimmedValue);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid CustomerId format: " + value, ex);
        }

        return new CustomerId(uuid);
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
