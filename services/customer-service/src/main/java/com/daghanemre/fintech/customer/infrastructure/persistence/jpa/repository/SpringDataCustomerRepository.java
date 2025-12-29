package com.daghanemre.fintech.customer.infrastructure.persistence.jpa.repository;

import com.daghanemre.fintech.customer.infrastructure.persistence.jpa.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for CustomerJpaEntity.
 *
 * This interface is internal to the infrastructure layer
 * and should NEVER be referenced by domain or application layers.
 *
 * The domain port (CustomerRepository) is implemented by
 * CustomerJpaRepositoryAdapter, which uses this repository internally.
 */
interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    /**
     * Finds a customer by email address.
     *
     * @param email normalized email string (lowercase, trimmed)
     * @return Optional containing the entity if found
     */
    Optional<CustomerJpaEntity> findByEmail(String email);
}
