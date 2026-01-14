package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;

import java.util.Map;

/**
 * Semantic specification for customer blocking eligibility.
 * 
 * <p><b>Business Rule:</b>
 * <ul>
 *   <li>Customer must NOT be deleted (hard constraint)</li>
 *   <li>Blocked customers remain blocked (idempotent operation)</li>
 * </ul>
 * 
 * <p><b>Design Note:</b>
 * Blocking is less restrictive than other state transitions because it's
 * typically an emergency compliance action. Any non-deleted customer can be blocked.
 */
public final class CustomerCanBeBlockedSpec implements Specification<Customer> {

    private final Specification<Customer> notDeleted = new CustomerNotDeletedSpec();

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return notDeleted.isSatisfiedBy(customer);
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        if (isSatisfiedBy(customer)) {
            return SpecificationViolation.none();
        }
        
        // Only failure mode: customer is deleted
        return new SpecificationViolation(
            "CUSTOMER_DELETED",
            "Cannot block a deleted customer",
            Map.of("customerId", customer.getId().value())
        );
    }
}
