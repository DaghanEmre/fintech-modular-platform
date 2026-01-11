package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

public final class CustomerIsActiveSpec implements Specification<Customer> {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getStatus() == CustomerStatus.ACTIVE;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        return new SpecificationViolation(
                "CUSTOMER_NOT_ACTIVE",
                "Customer is not in ACTIVE status: " + customer.getStatus());
    }
}
