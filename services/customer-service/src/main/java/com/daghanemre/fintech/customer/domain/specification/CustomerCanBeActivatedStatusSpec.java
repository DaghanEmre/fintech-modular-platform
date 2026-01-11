package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

/**
 * Specification for validating if a customer's status allows activation.
 * Provides specific violation codes for different invalid states.
 */
public final class CustomerCanBeActivatedStatusSpec implements Specification<Customer> {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getStatus() == CustomerStatus.PENDING || customer.getStatus() == CustomerStatus.SUSPENDED;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        if (customer.getStatus() == CustomerStatus.ACTIVE) {
            return new SpecificationViolation("CUSTOMER_ALREADY_ACTIVE", "Customer is already active");
        }
        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            return new SpecificationViolation("INVALID_STATUS_TRANSITION",
                    "Inactive customer cannot be activated. Current status: " + customer.getStatus());
        }
        return new SpecificationViolation("INVALID_STATUS",
                "Customer status is invalid for activation: " + customer.getStatus());
    }
}
