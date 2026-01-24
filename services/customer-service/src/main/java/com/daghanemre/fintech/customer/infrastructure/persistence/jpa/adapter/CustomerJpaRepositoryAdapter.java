package com.daghanemre.fintech.customer.infrastructure.persistence.jpa.adapter;

import com.daghanemre.fintech.customer.domain.model.*;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;
import com.daghanemre.fintech.customer.infrastructure.persistence.jpa.entity.CustomerJpaEntity;
import com.daghanemre.fintech.customer.infrastructure.persistence.jpa.repository.SpringDataCustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter implementing the CustomerRepository port.
 *
 * This adapter bridges the domain model and JPA persistence:
 * - Maps between Customer aggregate and CustomerJpaEntity
 * - Delegates actual persistence to Spring Data repository
 * - Maintains domain integrity during reconstitution
 *
 * Key responsibilities:
 * - Explicit mapping (no framework magic)
 * - Type conversions (UUID, Email, Status)
 * - Transaction management is handled at application service level
 *
 * Design notes:
 * - No @Transactional here (application service concern)
 * - No JPA auditing (domain manages audit fields)
 * - No soft-delete filtering (domain guards handle this)
 */
@Repository
public class CustomerJpaRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository jpaRepository;

    public CustomerJpaRepositoryAdapter(SpringDataCustomerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Customer customer) {
        CustomerJpaEntity entity = toEntity(customer);
        if (entity != null) {
            jpaRepository.save(entity);
        }
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        UUID id = customerId.value();
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value())
                .map(this::toDomain);
    }

    // ==================== Mapping Methods ====================

    /**
     * Maps domain Customer aggregate to JPA entity.
     *
     * This is a one-way mapping for persistence.
     * All domain state is transferred to the entity.
     */
    private CustomerJpaEntity toEntity(Customer customer) {
        return new CustomerJpaEntity(
                customer.getId().value(),
                customer.getEmail().value(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getDeletedAt());
    }

    /**
     * Reconstitutes domain Customer aggregate from JPA entity.
     *
     * Uses Customer.reconstitute() to bypass business validation,
     * assuming persisted data is already valid.
     */
    private Customer toDomain(CustomerJpaEntity entity) {
        return Customer.reconstitute(
                CustomerId.of(entity.getId()),
                Email.of(entity.getEmail()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
