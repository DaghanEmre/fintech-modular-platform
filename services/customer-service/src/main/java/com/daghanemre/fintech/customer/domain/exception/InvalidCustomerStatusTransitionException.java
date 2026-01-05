package com.daghanemre.fintech.customer.domain.exception;

import com.daghanemre.fintech.customer.domain.model.CustomerId;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

import java.util.Objects;

/**
 * Exception thrown when attempting an invalid customer status transition.
 */
public class InvalidCustomerStatusTransitionException extends CustomerDomainException {

    private final CustomerId customerId;
    private final CustomerStatus fromStatus;
    private final CustomerStatus toStatus;

    public InvalidCustomerStatusTransitionException(
            CustomerId customerId,
            CustomerStatus fromStatus,
            CustomerStatus toStatus) {
        super(String.format(
                "Invalid status transition for customer %s: %s → %s",
                customerId, fromStatus, toStatus));
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.fromStatus = Objects.requireNonNull(fromStatus, "fromStatus must not be null");
        this.toStatus = Objects.requireNonNull(toStatus, "toStatus must not be null");
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public CustomerStatus getFromStatus() {
        return fromStatus;
    }

    public CustomerStatus getToStatus() {
        return toStatus;
    }
}
