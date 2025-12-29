package com.daghanemre.fintech.customer.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for Customer aggregate persistence.
 *
 * This is a data carrier, NOT a domain object.
 * - No business logic
 * - No invariant enforcement
 * - Used only by infrastructure layer
 *
 * Scope (Phase 1):
 * - Identity (CustomerId)
 * - Contact (email)
 * - Lifecycle (status)
 * - Audit (timestamps)
 *
 * Intentionally excluded:
 * - firstName/lastName (profile concern - future CustomerProfile aggregate)
 * - phone, address (profile concern)
 *
 * Mapping to/from domain is handled by CustomerJpaRepositoryAdapter.
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customers_email", columnNames = "email")
})
public class CustomerJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CustomerStatusJpa status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Default constructor for JPA.
     */
    protected CustomerJpaEntity() {
    }

    /**
     * Full constructor for explicit instantiation.
     */
    public CustomerJpaEntity(
            UUID id,
            String email,
            CustomerStatusJpa status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public CustomerStatusJpa getStatus() {
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

    // Setters (for JPA state management)
    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(CustomerStatusJpa status) {
        this.status = status;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
