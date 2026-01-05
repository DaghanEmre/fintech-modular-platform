package com.daghanemre.fintech.customer.application.usecase;

import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerId;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;

import java.util.Objects;

/**
 * Use case for activating a customer account.
 *
 * <p>
 * This use case orchestrates the activation workflow:
 * <ol>
 * <li>Retrieve customer from repository</li>
 * <li>Invoke domain behavior (activate)</li>
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
 * <li>Map to DTOs (handled by adapter layer)</li>
 * </ul>
 *
 * <p>
 * <b>Transaction Management:</b>
 * Transaction management is intentionally delegated to the outer layer
 * (e.g. REST adapter or service wrapper).
 *
 * @see Customer#activate()
 * @see CustomerRepository
 */
public class ActivateCustomerUseCase {

    private final CustomerRepository customerRepository;

    /**
     * Constructor injection for repository port.
     *
     * @param customerRepository repository abstraction (port)
     * @throws IllegalArgumentException if repository is null
     */
    public ActivateCustomerUseCase(CustomerRepository customerRepository) {
        if (customerRepository == null) {
            throw new IllegalArgumentException("customerRepository must not be null");
        }
        this.customerRepository = customerRepository;
    }

    /**
     * Executes the customer activation use case.
     *
     * <p>
     * <b>Workflow:</b>
     * <ol>
     * <li>Fetch customer by ID</li>
     * <li>Call domain behavior: {@link Customer#activate()}</li>
     * <li>Persist updated aggregate</li>
     * </ol>
     *
     * <p>
     * <b>Exceptions:</b>
     * <ul>
     * <li>{@link IllegalArgumentException} - if customerId is null</li>
     * <li>{@link CustomerNotFoundException} - if customer does not exist</li>
     * <li>{@link IllegalStateException} - if activation is not allowed (domain
     * rule)</li>
     * </ul>
     *
     * @param customerId unique identifier of the customer to activate
     * @throws IllegalArgumentException  if customerId is null
     * @throws CustomerNotFoundException if customer is not found
     * @throws IllegalStateException     if customer cannot be activated (domain
     *                                   invariant violation)
     */
    public void execute(CustomerId customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }

        // 1. Retrieve aggregate
        Customer customer = loadCustomer(customerId);

        // 2. Execute domain behavior
        customer.activate();

        // 3. Persist changes
        customerRepository.save(customer);
    }

    private Customer loadCustomer(CustomerId customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}
