package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

public final class CustomerIsSuspendedSpec implements Specification<Customer> {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getStatus() == CustomerStatus.SUSPENDED;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        return new SpecificationViolation(
                "CUSTOMER_NOT_SUSPENDED",
                "Customer is not in SUSPENDED status: " + customer.getStatus());
    }
}
