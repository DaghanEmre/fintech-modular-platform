package com.daghanemre.fintech.customer.application.usecase;

import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerId;
import com.daghanemre.fintech.customer.domain.model.Email;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;

/**
 * Use case for changing a customer's email address.
 *
 * <p>
 * This use case orchestrates the email change workflow:
 * <ol>
 * <li>Retrieve customer from repository</li>
 * <li>Invoke domain behavior (changeEmail)</li>
 * <li>Persist updated aggregate</li>
 * </ol>
 *
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Use-case orchestration</li>
 * <li>Repository interaction</li>
 * <li>Exception propagation</li>
 * </ul>
 *
 * <p>
 * <b>Does NOT:</b>
 * <ul>
 * <li>Contain business rules (those live in domain)</li>
 * <li>Handle transactions (handled by outer layer)</li>
 * <li>Validate email format (handled by Email value object)</li>
 * </ul>
 *
 * @see Customer#changeEmail(Email)
 * @see Email
 * @see CustomerRepository
 */
public class ChangeCustomerEmailUseCase {

    private final CustomerRepository customerRepository;

    /**
     * Constructor injection for repository port.
     *
     * @param customerRepository repository abstraction (port)
     * @throws IllegalArgumentException if repository is null
     */
    public ChangeCustomerEmailUseCase(CustomerRepository customerRepository) {
        if (customerRepository == null) {
            throw new IllegalArgumentException("customerRepository must not be null");
        }
        this.customerRepository = customerRepository;
    }

    /**
     * Executes the email change use case.
     *
     * <p>
     * <b>Workflow:</b>
     * <ol>
     * <li>Fetch customer by ID</li>
     * <li>Call domain behavior: {@link Customer#changeEmail(Email)}</li>
     * <li>Persist updated aggregate</li>
     * </ol>
     *
     * <p>
     * <b>Domain Rules:</b>
     * <ul>
     * <li>Customer must not be deleted</li>
     * <li>New email must be valid (enforced by Email value object)</li>
     * <li>If email is same as current, no-op (idempotent)</li>
     * </ul>
     *
     * <p>
     * <b>Exceptions:</b>
     * <ul>
     * <li>{@link IllegalArgumentException} - if customerId or newEmail is null</li>
     * <li>{@link CustomerNotFoundException} - if customer does not exist</li>
     * <li>{@link com.daghanemre.fintech.customer.domain.exception.CustomerDeletedException}
     * - if customer is deleted</li>
     * </ul>
     *
     * @param customerId unique identifier of the customer
     * @param newEmail   new email address
     * @throws IllegalArgumentException                                                  if
     *                                                                                   inputs
     *                                                                                   are
     *                                                                                   null
     * @throws CustomerNotFoundException                                                 if
     *                                                                                   customer
     *                                                                                   is
     *                                                                                   not
     *                                                                                   found
     * @throws com.daghanemre.fintech.customer.domain.exception.CustomerDeletedException if
     *                                                                                   customer
     *                                                                                   is
     *                                                                                   deleted
     */
    public void execute(CustomerId customerId, Email newEmail) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (newEmail == null) {
            throw new IllegalArgumentException("newEmail must not be null");
        }

        // 1. Retrieve aggregate
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // 2. Execute domain behavior
        customer.changeEmail(newEmail);

        // 3. Persist changes
        customerRepository.save(customer);
    }
}
