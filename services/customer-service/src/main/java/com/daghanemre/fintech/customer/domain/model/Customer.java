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
    private String firstName;
    private String lastName;
    private CustomerStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // Optimistic locking version
    private Long version;

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
    public static Customer create(Email email, String firstName, String lastName) {
        Objects.requireNonNull(email, "email must not be null");
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        LocalDateTime now = LocalDateTime.now();

        return new Customer(
                CustomerId.generate(),
                email,
                firstName.trim(),
                lastName.trim(),
                CustomerStatus.PENDING,
                now,
                now,
                null,
                0L // Initial version
        );
    }

    /**
     * Reconstitutes an existing Customer aggregate from persistence.
     *
     * <p>
     * No validation or business rules are executed.
     * This method is intended strictly for repository adapters.
     * </p>
     */
    public static Customer reconstitute(
            CustomerId id,
            Email email,
            String firstName,
            String lastName,
            CustomerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            Long version) {
        return new Customer(id, email, firstName, lastName, status, createdAt, updatedAt, deletedAt, version);
    }

    private Customer(
            CustomerId id,
            Email email,
            String firstName,
            String lastName,
            CustomerStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            Long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.deletedAt = deletedAt;
        this.version = version;
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

    public void changeName(String newFirstName, String newLastName) {
        ensureNotDeleted();
        validateName(newFirstName, "First name");
        validateName(newLastName, "Last name");

        this.firstName = newFirstName.trim();
        this.lastName = newLastName.trim();
        touch();
    }

    /**
     * Soft-deletes this customer.
     *
     * <p>
     * After deletion, no state-changing behavior is allowed.
     * </p>
     */
    public void delete() {
        if (this.deletedAt != null) {
            return; // idempotent
        }

        this.status = CustomerStatus.INACTIVE;
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

    private static void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum length");
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    public Long getVersion() {
        return version;
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
