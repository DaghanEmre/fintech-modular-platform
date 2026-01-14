package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

import java.util.Map;

/**
 * Semantic specification for customer suspension eligibility.
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Customer must NOT be deleted (hard constraint)</li>
 *   <li>Customer must NOT be blocked (hard constraint)</li>
 *   <li>Customer must be ACTIVE or SUSPENDED</li>
 * </ul>
 * 
 * <p><b>Design Rationale:</b>
 * Provides explicit error codes for suspension violations instead of generic
 * composite errors. It encapsulates the transition rules:
 * <pre>
 * // It effectively implements:
 * notDeleted.and(notBlocked).and(status == ACTIVE)
 * </pre>
 */
public final class CustomerCanBeSuspendedSpec implements Specification<Customer> {

    // Delegate to reusable atomic specifications
    private final Specification<Customer> notDeleted = new CustomerNotDeletedSpec();
    private final Specification<Customer> notBlocked = new CustomerNotBlockedSpec();
    private final Specification<Customer> isActive = new CustomerIsActiveSpec();

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        if (!notDeleted.isSatisfiedBy(customer) || !notBlocked.isSatisfiedBy(customer)) {
            return false;
        }

        // Must be ACTIVE or already SUSPENDED (for idempotency)
        CustomerStatus status = customer.getStatus();
        return status == CustomerStatus.ACTIVE || status == CustomerStatus.SUSPENDED;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        if (isSatisfiedBy(customer)) {
            return SpecificationViolation.none();
        }
        
        // Priority order: deleted > blocked > not active
        
        if (customer.isDeleted()) {
            return new SpecificationViolation(
                "CUSTOMER_DELETED",
                "Cannot suspend a deleted customer",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        if (customer.getStatus() == CustomerStatus.BLOCKED) {
            return new SpecificationViolation(
                "CUSTOMER_BLOCKED",
                "Cannot suspend a blocked customer",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        // Customer must be ACTIVE for a new suspension.
        // SUSPENDED is also allowed for idempotency.
        // Neither ACTIVE nor SUSPENDED - invalid transition
        return new SpecificationViolation(
            "INVALID_STATUS_TRANSITION",
            String.format(
                "Cannot suspend customer in %s status. Customer must be ACTIVE to be suspended.",
                customer.getStatus()
            ),
            Map.of(
                "customerId", customer.getId().value(),
                "currentStatus", customer.getStatus().name(),
                "requiredStatus", "ACTIVE"
            )
        );
    }
}
