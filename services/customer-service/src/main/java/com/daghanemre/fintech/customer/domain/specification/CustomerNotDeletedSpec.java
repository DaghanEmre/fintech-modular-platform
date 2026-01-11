package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;

public final class CustomerNotDeletedSpec implements Specification<Customer> {

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return !customer.isDeleted();
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        return new SpecificationViolation(
                "CUSTOMER_DELETED",
                "Action cannot be performed on a deleted customer: " + customer.getId().value());
    }
}
