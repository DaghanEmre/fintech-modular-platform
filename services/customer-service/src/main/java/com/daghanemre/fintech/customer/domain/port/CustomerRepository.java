package com.daghanemre.fintech.customer.domain.port;

import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerId;
import com.daghanemre.fintech.customer.domain.model.Email;

import java.util.Optional;

/**
 * Repository port for Customer aggregate.
 *
 * This interface defines the contract between the domain model
 * and persistence mechanisms. It follows the Port-Adapter pattern
 * from Hexagonal Architecture.
 *
 * Key design decisions:
 * - Domain language only (no JPA/SQL terminology)
 * - Returns Optional, not exceptions (not found is not exceptional)
 * - Soft-deleted customers are NOT filtered by default
 * - No rich query methods (use CQRS read models for reporting)
 *
 * Implementations:
 * - Live in infrastructure layer
 * - May use JPA, JDBC, event sourcing, or any persistence mechanism
 * - Must handle both insert and update in save()
 * - Must maintain aggregate atomicity
 *
 * References:
 * - ADR-0001: Hexagonal Architecture
 * - ADR-0002: Customer Domain Model Design
 */
public interface CustomerRepository {

    /**
     * Persists the given Customer aggregate.
     *
     * Implementations must:
     * - Handle both insert (new customer) and update (existing customer)
     * - Ensure atomic persistence of the entire aggregate
     * - Respect optimistic locking if implemented
     *
     * @param customer the customer aggregate to persist
     * @throws IllegalArgumentException if customer is null
     */
    void save(Customer customer);

    /**
     * Finds a Customer by its unique identity.
     *
     * Soft-deleted customers MAY be returned.
     * Domain logic is responsible for checking deletion status
     * and enforcing behavior constraints.
     *
     * @param customerId the customer's unique identifier
     * @return Optional containing the customer if found, empty otherwise
     * @throws IllegalArgumentException if customerId is null
     */
    Optional<Customer> findById(CustomerId customerId);

    /**
     * Finds a Customer by email address.
     *
     * Email is treated as a natural identifier for lookup purposes.
     * This is NOT a unique constraint enforcement method.
     *
     * Use cases:
     * - Login/authentication flows
     * - Duplicate detection during registration
     * - Customer recovery flows
     *
     * Note: Multiple customers with the same email should be prevented
     * at the database level (unique constraint), not by this method.
     *
     * @param email the customer's email address
     * @return Optional containing the customer if found, empty otherwise
     * @throws IllegalArgumentException if email is null
     */
    Optional<Customer> findByEmail(Email email);
}
