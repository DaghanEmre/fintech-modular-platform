package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

import java.util.Map;

/**
 * Semantic specification for marking customer inactive eligibility.
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Customer must NOT be deleted (hard constraint)</li>
 *   <li>Customer must NOT be blocked (blocked is a stronger state)</li>
 * </ul>
 * 
 * <p><b>Design Note:</b>
 * INACTIVE represents a customer-initiated closure or voluntary deactivation.
 * It's different from SUSPENDED (temporary restriction) or BLOCKED (compliance action).
 */
public final class CustomerCanBeMarkedInactiveSpec implements Specification<Customer> {

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
                "Cannot mark a deleted customer as inactive",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        if (customer.getStatus() == CustomerStatus.BLOCKED) {
            return new SpecificationViolation(
                "CUSTOMER_BLOCKED",
                "Cannot mark a blocked customer as inactive",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        // Should not reach here if contract is correct
        throw new IllegalStateException("Specification failed but no violation identified");
    }
}
