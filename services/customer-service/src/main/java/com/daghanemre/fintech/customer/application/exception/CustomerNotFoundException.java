package com.daghanemre.fintech.customer.application.exception;

import com.daghanemre.fintech.customer.domain.model.CustomerId;

/**
 * Exception thrown when a customer cannot be found by their identifier.
 *
 * <p>
 * This is an <b>application-level exception</b>, not a domain exception.
 * It represents a use-case failure due to missing data, not a domain invariant
 * violation.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * <ul>
 * <li>Thrown by use cases when repository returns empty Optional</li>
 * <li>Should be mapped to HTTP 404 by adapter layer</li>
 * <li>NOT thrown by domain model</li>
 * </ul>
 *
 * <p>
 * <b>Design Decision:</b>
 * <ul>
 * <li>Unchecked exception (RuntimeException) for cleaner use-case
 * signatures</li>
 * <li>Carries CustomerId for logging and debugging</li>
 * <li>Immutable and serializable</li>
 * </ul>
 */
public class CustomerNotFoundException extends RuntimeException {

    private final CustomerId customerId;

    /**
     * Constructs exception with customer identifier.
     *
     * @param customerId the identifier of the customer that was not found
     */
    public CustomerNotFoundException(CustomerId customerId) {
        super("Customer not found: " + customerId);
        this.customerId = customerId;
    }

    /**
     * Returns the identifier of the customer that was not found.
     *
     * @return customer identifier
     */
    public CustomerId getCustomerId() {
        return customerId;
    }
}
