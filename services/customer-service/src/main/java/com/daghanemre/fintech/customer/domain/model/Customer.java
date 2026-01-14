package com.daghanemre.fintech.customer.domain.model;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationException;
import com.daghanemre.fintech.common.specification.SpecificationViolation;

import com.daghanemre.fintech.customer.domain.specification.CustomerSpecifications;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Customer Aggregate Root (Refactored with Specification Pattern).
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

    public static Customer create(Email email) {
        Objects.requireNonNull(email, "email must not be null");

        LocalDateTime now = LocalDateTime.now();

        return new Customer(
                CustomerId.generate(),
                email,
                CustomerStatus.PENDING,
                now,
                now,
                null);
    }

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
        ensure(CustomerSpecifications.canBeActivated());

        if (this.status == CustomerStatus.ACTIVE) {
            return; // idempotent
        }

        this.status = CustomerStatus.ACTIVE;
        touch();
    }

    public void suspend(StateChangeReason reason) {
        ensure(CustomerSpecifications.canBeSuspended());
        Objects.requireNonNull(reason, "Reason for suspension is mandatory");

        if (this.status == CustomerStatus.SUSPENDED) {
            return; // idempotent
        }

        this.status = CustomerStatus.SUSPENDED;
        touch();
    }

    public void block(StateChangeReason reason) {
        ensure(CustomerSpecifications.canBeBlocked());
        Objects.requireNonNull(reason, "Reason for blocking is mandatory");

        if (this.status == CustomerStatus.BLOCKED) {
            return; // idempotent
        }

        this.status = CustomerStatus.BLOCKED;
        touch();
    }

    public void markInactive() {
        ensure(CustomerSpecifications.canBeMarkedInactive());

        if (this.status == CustomerStatus.INACTIVE) {
            return; // idempotent
        }

        this.status = CustomerStatus.INACTIVE;
        touch();
    }

    public void changeEmail(Email newEmail) {
        ensure(CustomerSpecifications.canChangeEmail());
        Objects.requireNonNull(newEmail, "newEmail must not be null");

        if (this.email.equals(newEmail)) {
            return; // no-op
        }

        this.email = newEmail;
        touch();
    }

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

    /**
     * Enforces that the aggregate satisfies the given specification.
     *
     * <p><b>Specification Contract Enforcement:</b>
     * <ul>
     *   <li>If specification is satisfied → return normally</li>
     *   <li>If specification fails → MUST provide a valid violation</li>
     *   <li>If specification fails but provides no violation → throw IllegalStateException</li>
     * </ul>
     *
     * <p>This strict contract prevents poorly implemented specifications from reaching production.
     *
     * @param specification the business rule to enforce
     * @throws SpecificationException if specification is violated (business error)
     * @throws IllegalStateException if specification fails but provides no violation (developer error)
     */
    private void ensure(Specification<Customer> specification) {
        Objects.requireNonNull(specification, "specification must not be null");

        // Happy path: specification satisfied
        if (specification.isSatisfiedBy(this)) {
            return;
        }

        // Specification failed - violation MUST be present
        SpecificationViolation violation = specification.violation(this);

        if (violation == null || !violation.isPresent()) {
            // This is a DEVELOPER ERROR - specification implementation is broken
            throw new IllegalStateException(
                "Specification failed but did not provide a violation: " +
                specification.getClass().getSimpleName() +
                ". All failing specifications MUST return a valid SpecificationViolation. " +
                "This indicates a bug in the specification implementation."
            );
        }

        // Business rule violation - throw domain exception
        throw new SpecificationException(violation);
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
