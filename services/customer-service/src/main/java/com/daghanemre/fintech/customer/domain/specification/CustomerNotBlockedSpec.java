package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

public final class CustomerNotBlockedSpec implements Specification<Customer> {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getStatus() != CustomerStatus.BLOCKED;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        return new SpecificationViolation(
                "CUSTOMER_BLOCKED",
                "Action cannot be performed on a blocked customer: " + customer.getId().value());
    }
}
