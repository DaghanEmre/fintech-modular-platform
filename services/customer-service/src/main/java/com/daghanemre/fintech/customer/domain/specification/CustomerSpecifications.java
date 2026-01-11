package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.customer.domain.model.Customer;

/**
 * Factory and repository for Customer domain specifications.
 */
public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    /**
     * Rules required for a customer to be activated.
     *
     * <p>
     * A customer can be activated if:
     * <ul>
     * <li>They are not deleted</li>
     * <li>They are not blocked</li>
     * <li>They are in PENDING or SUSPENDED status</li>
     * </ul>
     */
    public static Specification<Customer> canBeActivated() {
        return new CustomerNotDeletedSpec()
                .and(new CustomerNotBlockedSpec())
                .and(new CustomerCanBeActivatedStatusSpec());
    }

    /**
     * Rules required for a customer to be suspended.
     */
    public static Specification<Customer> canBeSuspended() {
        return new CustomerNotDeletedSpec()
                .and(new CustomerNotBlockedSpec())
                .and(new CustomerIsActiveSpec());
    }

    /**
     * Rules required for a customer to be blocked.
     */
    public static Specification<Customer> canBeBlocked() {
        return new CustomerNotDeletedSpec();
    }

    /**
     * Rules required for changing a customer's email.
     */
    public static Specification<Customer> canChangeEmail() {
        return new CustomerNotDeletedSpec();
    }
}
