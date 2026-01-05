package com.daghanemre.fintech.customer.domain.exception;

import com.daghanemre.fintech.customer.domain.model.CustomerId;
import java.util.Objects;

/**
 * Exception thrown when attempting to activate an already active customer.
 */
public class CustomerAlreadyActiveException extends CustomerDomainException {

    private final CustomerId customerId;

    public CustomerAlreadyActiveException(CustomerId customerId) {
        super(String.format("Customer is already active: %s", customerId));
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
}
