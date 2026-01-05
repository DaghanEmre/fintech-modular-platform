package com.daghanemre.fintech.customer.domain.model;

import com.daghanemre.fintech.customer.domain.exception.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Customer Aggregate Root (Refactored with Domain Exceptions).
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
        ensureNotDeleted();

        if (this.status == CustomerStatus.ACTIVE) {
            throw new CustomerAlreadyActiveException(this.id);
        }

        if (this.status == CustomerStatus.BLOCKED) {
            throw new CustomerBlockedException(this.id);
        }

        if (this.status != CustomerStatus.PENDING &&
                this.status != CustomerStatus.SUSPENDED) {
            throw new InvalidCustomerStatusTransitionException(
                    this.id, this.status, CustomerStatus.ACTIVE);
        }

        this.status = CustomerStatus.ACTIVE;
        touch();
    }

    public void suspend(StateChangeReason reason) {
        ensureNotDeleted();
        Objects.requireNonNull(reason, "reason must not be null");

        if (this.status == CustomerStatus.BLOCKED) {
            throw new CustomerBlockedException(this.id);
        }

        if (this.status != CustomerStatus.ACTIVE) {
            throw new InvalidCustomerStatusTransitionException(
                    this.id, this.status, CustomerStatus.SUSPENDED);
        }

        this.status = CustomerStatus.SUSPENDED;
        touch();
    }

    public void block(StateChangeReason reason) {
        ensureNotDeleted();
        Objects.requireNonNull(reason, "reason must not be null");

        if (this.status == CustomerStatus.BLOCKED) {
            return; // idempotent
        }

        this.status = CustomerStatus.BLOCKED;
        touch();
    }

    public void markInactive() {
        ensureNotDeleted();

        if (this.status == CustomerStatus.BLOCKED) {
            throw new CustomerBlockedException(this.id);
        }

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
            throw new CustomerDeletedException(this.id);
        }
    }

    // TODO: Consider Clock abstraction if time-based SLA rules are added
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
