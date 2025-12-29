package com.daghanemre.fintech.customer.infrastructure.persistence.jpa.entity;

/**
 * JPA enum for CustomerStatus.
 *
 * Kept separate from domain enum to allow independent evolution:
 * - Domain status may add new values
 * - Infrastructure enum remains stable for database migrations
 */
public enum CustomerStatusJpa {
    PENDING,
    ACTIVE,
    SUSPENDED,
    INACTIVE,
    BLOCKED
}
