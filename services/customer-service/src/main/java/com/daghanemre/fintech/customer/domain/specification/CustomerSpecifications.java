package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.customer.domain.model.Customer;

/**
 * Factory for composite customer specifications.
 *
 * <p>This class provides factory methods for common business rule combinations.
 * Individual specifications are atomic and reusable. Composite rules are built
 * using AND, OR, NOT operations.
 *
 * <p><b>Design Principle:</b> Each atomic specification tests ONE condition.
 * Complex rules are composed from simple ones.
 */
public final class CustomerSpecifications {

    private CustomerSpecifications() {
        // Utility class
    }

    /**
     * Specification for customer activation eligibility.
     *
     * <p><b>Business Rules:</b>
     * <ul>
     *   <li>Customer must NOT be deleted</li>
     *   <li>Customer must NOT be blocked</li>
     *   <li>Customer must be in PENDING OR SUSPENDED status</li>
     * </ul>
     *
     * @return composite specification
     */
    public static Specification<Customer> canBeActivated() {
        return new CustomerNotDeletedSpec()
                .and(new CustomerNotBlockedSpec())
                .and(
                        new CustomerIsPendingSpec()
                                .or(new CustomerIsSuspendedSpec())
                );
    }

    /**
     * Specification for customer suspension eligibility.
     *
     * <p><b>Business Rules:</b>
     * <ul>
     *   <li>Customer must NOT be deleted</li>
     *   <li>Customer must NOT be blocked</li>
     *   <li>Customer must be ACTIVE</li>
     * </ul>
     *
     * @return composite specification
     */
    public static Specification<Customer> canBeSuspended() {
        return new CustomerNotDeletedSpec()
                .and(new CustomerNotBlockedSpec())
                .and(new CustomerIsActiveSpec());
    }

    /**
     * Specification for customer blocking eligibility.
     *
     * <p><b>Business Rule:</b>
     * <ul>
     *   <li>Customer must NOT be deleted (blocked customers stay blocked)</li>
     * </ul>
     *
     * @return composite specification
     */
    public static Specification<Customer> canBeBlocked() {
        return new CustomerNotDeletedSpec();
    }

    /**
     * Specification for email change eligibility.
     *
     * <p><b>Business Rule:</b>
     * <ul>
     *   <li>Customer must NOT be deleted</li>
     * </ul>
     *
     * @return composite specification
     */
    public static Specification<Customer> canChangeEmail() {
        return new CustomerNotDeletedSpec();
    }

    /**
     * Specification for marking customer inactive eligibility.
     *
     * <p><b>Business Rules:</b>
     * <ul>
     *   <li>Customer must NOT be deleted</li>
     *   <li>Customer must NOT be blocked</li>
     * </ul>
     *
     * @return composite specification
     */
    public static Specification<Customer> canBeMarkedInactive() {
        return new CustomerNotDeletedSpec()
                .and(new CustomerNotBlockedSpec());
    }
}
