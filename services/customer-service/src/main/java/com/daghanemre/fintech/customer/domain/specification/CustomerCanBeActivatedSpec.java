package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;

import java.util.Map;

/**
 * Semantic specification for customer activation eligibility.
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Customer must NOT be deleted (hard constraint)</li>
 *   <li>Customer must NOT be blocked (hard constraint)</li>
 *   <li>Customer must be in PENDING, SUSPENDED, or ACTIVE status</li>
 * </ul>
 * 
 * <p><b>Design Rationale:</b>
 * This semantic wrapper provides business-friendly error codes instead of generic
 * composite OR violations. It encapsulates the transition rules:
 * <pre>
 * // It effectively implements:
 * notDeleted.and(notBlocked)
 *     .and(status == PENDING || status == SUSPENDED)
 * </pre>
 * 
 * <p>See ADR-0004 for the full rationale on semantic specifications.
 */
public final class CustomerCanBeActivatedSpec implements Specification<Customer> {

    // Delegate to reusable atomic specifications
    private final Specification<Customer> notDeleted = new CustomerNotDeletedSpec();
    private final Specification<Customer> notBlocked = new CustomerNotBlockedSpec();

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        // Reuse atomic specifications for consistency
        if (!notDeleted.isSatisfiedBy(customer)) {
            return false;
        }
        
        if (!notBlocked.isSatisfiedBy(customer)) {
            return false;
        }
        
        // Status must be PENDING, SUSPENDED, or already ACTIVE (for idempotency)
        CustomerStatus status = customer.getStatus();
        return status == CustomerStatus.PENDING 
            || status == CustomerStatus.SUSPENDED 
            || status == CustomerStatus.ACTIVE;
    }

    @Override
    public SpecificationViolation violation(Customer customer) {
        if (isSatisfiedBy(customer)) {
            return SpecificationViolation.none();
        }
        
        // Priority order: deleted > blocked > status
        
        if (customer.isDeleted()) {
            return new SpecificationViolation(
                "CUSTOMER_DELETED",
                "Cannot activate a deleted customer",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        if (customer.getStatus() == CustomerStatus.BLOCKED) {
            return new SpecificationViolation(
                "CUSTOMER_BLOCKED",
                "Cannot activate a blocked customer",
                Map.of("customerId", customer.getId().value())
            );
        }
        
        // Status must be PENDING or SUSPENDED for a new activation.
        // ACTIVE is also allowed for idempotency.
        // Neither PENDING nor SUSPENDED (nor ACTIVE) - invalid transition (e.g., from INACTIVE)
        return new SpecificationViolation(
            "INVALID_STATUS_TRANSITION",
            String.format(
                "Cannot activate customer in %s status. Customer must be in PENDING or SUSPENDED status to be activated.",
                customer.getStatus()
            ),
            Map.of(
                "customerId", customer.getId().value(),
                "currentStatus", customer.getStatus().name(),
                "allowedStatuses", "PENDING, SUSPENDED"
            )
        );
    }
}
