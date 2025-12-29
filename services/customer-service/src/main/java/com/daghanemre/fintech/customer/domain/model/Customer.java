package com.daghanemre.fintech.customer.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Customer Aggregate Root.
 *
 * <p>
 * Represents a single customer within the Customer bounded context.
 * This aggregate is responsible for enforcing all business invariants
 * related to customer lifecycle, identity, and state transitions.
 * </p>
 *
 * <p>
 * Infrastructure concerns (JPA, serialization, messaging) are explicitly
 * excluded from this class.
 * </p>
 */
public class Customer {

    private final CustomerId id;
    private Email email;
    private CustomerStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /*
     * =========================
     * Factory Methods
     * =========================
     */

    /**
     * Creates a new Customer aggregate.
     *
     * <p>
     * Initial status is {@link CustomerStatus#PENDING}.
     * Audit fields are initialized by the domain.
     * </p>
     */
    public static Customer create(Email email) {
        Objects.requireNonNull(email, "email must not be null");

        LocalDateTime now = LocalDateTime.now();

        return new Customer(
                CustomerId.generate(),
                email,
                CustomerStatus.PENDING,
                now,
                now,
                null // Not deleted
        );
    }

    /**
     * Factory method for reconstituting a Customer from persistence.
     *
     * Used by infrastructure layer to rebuild domain objects from database.
     * Does NOT perform business validation (assumes valid persisted state).
     *
     * @return Customer instance with existing identity
     */
    public static Customer reconstitute(
            CustomerId id,
            Email email,
            CustomerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt) {
        return new Customer(
                id,
                email,
                status,
                createdAt,
                updatedAt,
                deletedAt);
    }

    private Customer(
            CustomerId id,
            Email email,
            CustomerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.deletedAt = deletedAt;
    }

    /*
     * =========================
     * Domain Behaviors
     * =========================
     */

    public void activate() {
        ensureNotDeleted();

        if (this.status != CustomerStatus.PENDING &&
                this.status != CustomerStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Customer cannot be activated from status: " + status);
        }

        this.status = CustomerStatus.ACTIVE;
        touch();
    }

    public void suspend(String reason) {
        ensureNotDeleted();
        requireReason(reason);

        if (this.status != CustomerStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE customers can be suspended");
        }

        this.status = CustomerStatus.SUSPENDED;
        touch();
    }

    public void block(String reason) {
        ensureNotDeleted();
        requireReason(reason);

        if (this.status == CustomerStatus.BLOCKED) {
            return; // idempotent
        }

        this.status = CustomerStatus.BLOCKED;
        touch();
    }

    public void markInactive() {
        ensureNotDeleted();

        if (this.status == CustomerStatus.INACTIVE) {
            return; // idempotent
        }

        this.status = CustomerStatus.INACTIVE;
        touch();
    }

    public void changeEmail(Email newEmail) {
        ensureNotDeleted();
        Objects.requireNonNull(newEmail, "newEmail must not be null");

        if (this.email.equals(newEmail)) {
            return; // no-op
        }

        this.email = newEmail;
        touch();
    }

    /**
     * Soft-deletes this customer.
     *
     * <p>
     * After deletion, no state-changing behavior is allowed.
     * Status is intentionally NOT changed to INACTIVE; deletion is an orthogonal
     * lifecycle state.
     * </p>
     */
    public void delete() {
        if (this.deletedAt != null) {
            return; // idempotent
        }

        this.deletedAt = LocalDateTime.now();
        touch();
    }

    /*
     * =========================
     * Guard Methods
     * =========================
     */

    private void ensureNotDeleted() {
        if (this.deletedAt != null) {
            throw new IllegalStateException(
                    "Operation not allowed on deleted customer");
        }
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason must be provided");
        }
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /*
     * =========================
     * Getters (Read-only)
     * =========================
     */

    public CustomerId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Customer customer = (Customer) o;
        return id.equals(customer.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
