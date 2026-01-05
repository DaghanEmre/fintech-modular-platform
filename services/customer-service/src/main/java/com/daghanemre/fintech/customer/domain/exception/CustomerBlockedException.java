package com.daghanemre.fintech.customer.domain.exception;

import com.daghanemre.fintech.customer.domain.model.CustomerId;
import java.util.Objects;

/**
 * Exception thrown when an operation is attempted on a blocked customer.
 */
public class CustomerBlockedException extends CustomerDomainException {

    private final CustomerId customerId;

    public CustomerBlockedException(CustomerId customerId) {
        super(String.format("Operation not allowed on blocked customer: %s", customerId));
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
}
