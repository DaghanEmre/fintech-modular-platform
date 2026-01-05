package com.daghanemre.fintech.customer.domain.exception;

import com.daghanemre.fintech.customer.domain.model.CustomerId;
import java.util.Objects;

/**
 * Exception thrown when an operation is attempted on a deleted customer.
 */
public class CustomerDeletedException extends CustomerDomainException {

    private final CustomerId customerId;

    public CustomerDeletedException(CustomerId customerId) {
        super(String.format("Operation not allowed on deleted customer: %s", customerId));
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
}
