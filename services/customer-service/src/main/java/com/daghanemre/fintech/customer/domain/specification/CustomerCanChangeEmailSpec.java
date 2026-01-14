package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

import java.util.Map;

/**
 * Semantic specification for email change eligibility.
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Customer must NOT be deleted (hard constraint)</li>
 *   <li>Customer must NOT be blocked (compliance requirement)</li>
 * </ul>
 * 
 * <p><b>Design Rationale:</b>
 * Email changes are allowed even for PENDING or SUSPENDED customers,
 * but blocked customers cannot modify their profile.
 */
public final class CustomerCanChangeEmailSpec implements Specification<Customer> {

    private final Specification<Customer> notDeleted = new CustomerNotDeletedSpec();
    private final Specification<Customer> notBlocked = new CustomerNotBlockedSpec();

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return notDeleted.isSatisfiedBy(customer) 
            && notBlocked.isSatisfiedBy(customer);
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        if (isSatisfiedBy(customer)) {
            return SpecificationViolation.none();
        }
        
        // Priority order: deleted > blocked
        
        if (customer.isDeleted()) {
            return new SpecificationViolation(
                "CUSTOMER_DELETED",
                "Cannot change email for a deleted customer",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        if (customer.getStatus() == CustomerStatus.BLOCKED) {
            return new SpecificationViolation(
                "CUSTOMER_BLOCKED",
                "Cannot change email for a blocked customer",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        // Should not reach here if contract is correct
        throw new IllegalStateException("Specification failed but no violation identified");
    }
}
