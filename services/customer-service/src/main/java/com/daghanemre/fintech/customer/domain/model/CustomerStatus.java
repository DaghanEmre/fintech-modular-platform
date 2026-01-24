package com.daghanemre.fintech.customer.domain.model;

import java.util.Optional;

/**
 * Represents the lifecycle status of a Customer within the system.
 *
 * <p>This enum is a <b>Tier 1 (Domain-Internal)</b> enum as defined in ADR-0006.
 * It is owned by the customer-service domain and should never be exposed as
 * a Java enum across service boundaries.
 *
 * <p>Behavioral rules:
 * <ul>
 *   <li>Boundary communication: Use {@code String} values in DTOs and events.</li>
 *   <li>Forward compatibility: Use {@link #safeParse(String)} for external input.</li>
 * </ul>
 */
public enum CustomerStatus {

    /**
     * Customer has been created but KYC verification is not completed yet.
     */
    PENDING,

    /**
     * Customer is fully verified and allowed to perform normal operations.
     */
    ACTIVE,

    /**
     * Customer is temporarily restricted due to fraud suspicion or investigation.
     */
    SUSPENDED,

    /**
     * Customer account has been closed by the customer.
     */
    INACTIVE,

    /**
     * Customer is permanently blocked due to AML or severe fraud.
     */
    BLOCKED;

    /**
     * Safely parses an external status string into a {@code CustomerStatus}.
     *
     * <p>Provides <b>Forward Compatibility</b> by returning {@code Optional.empty()}
     * for unknown status values (e.g., from a newer version of the service).
     *
     * @param value the raw string value to parse
     * @return Optional containing the enum constant if found, empty otherwise
     */
    public static Optional<CustomerStatus> safeParse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the external string representation for cross-service communication.
     */
    public String toExternalValue() {
        return this.name();
    }
}

