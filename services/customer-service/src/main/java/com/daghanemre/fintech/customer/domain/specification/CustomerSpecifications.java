package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.customer.domain.model.Customer;

/**
 * Factory for customer business rule specifications.
 *
 * <p>This class provides semantic specifications for common business rules.
 * Each method returns a specification that encapsulates the complete validation
 * logic and provides domain-appropriate error codes.
 * 
 * <p><b>Design Philosophy:</b>
 * <ul>
 *   <li>Atomic specifications test ONE condition ({@code CustomerNotDeletedSpec})</li>
 *   <li>Semantic specifications combine rules with business intent ({@code CustomerCanBeActivatedSpec})</li>
 *   <li>This factory exposes semantic specifications to aggregate methods</li>
 * </ul>
 * 
 * <p><b>Migration Note:</b>
 * This refactored version replaces raw composite specifications with semantic wrappers
 * to provide better error codes and observability. See ADR-0004 for rationale.
 * 
 * <p><b>Example Usage:</b>
 * <pre>
 * // Inside Customer aggregate
 * public void activate() {
 *     ensure(CustomerSpecifications.canBeActivated());
 *     this.status = CustomerStatus.ACTIVE;
 *     touch();
 * }
 * </pre>
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
     * <p><b>Error Codes:</b>
     * <ul>
     *   <li>{@code CUSTOMER_DELETED} - Customer is soft-deleted</li>
     *   <li>{@code CUSTOMER_BLOCKED} - Customer is blocked</li>
     *   <li>{@code CUSTOMER_ALREADY_ACTIVE} - Already in ACTIVE status</li>
     *   <li>{@code INVALID_STATUS_TRANSITION} - Wrong status for activation</li>
     * </ul>
     *
     * @return semantic specification
     */
    public static Specification<Customer> canBeActivated() {
        return new CustomerCanBeActivatedSpec();
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
     * <p><b>Error Codes:</b>
     * <ul>
     *   <li>{@code CUSTOMER_DELETED} - Customer is soft-deleted</li>
     *   <li>{@code CUSTOMER_BLOCKED} - Customer is blocked</li>
     *   <li>{@code CUSTOMER_ALREADY_SUSPENDED} - Already in SUSPENDED status</li>
     *   <li>{@code INVALID_STATUS_TRANSITION} - Wrong status for suspension</li>
     * </ul>
     *
     * @return semantic specification
     */
    public static Specification<Customer> canBeSuspended() {
        return new CustomerCanBeSuspendedSpec();
    }

    /**
     * Specification for customer blocking eligibility.
     *
     * <p><b>Business Rule:</b>
     * <ul>
     *   <li>Customer must NOT be deleted (blocked customers stay blocked)</li>
     * </ul>
     * 
     * <p><b>Error Code:</b>
     * <ul>
     *   <li>{@code CUSTOMER_DELETED} - Customer is soft-deleted</li>
     * </ul>
     * 
     * <p><b>Design Note:</b>
     * Blocking is intentionally less restrictive than other transitions
     * because it's typically an emergency compliance action.
     *
     * @return semantic specification
     */
    public static Specification<Customer> canBeBlocked() {
        return new CustomerCanBeBlockedSpec();
    }

    /**
     * Specification for email change eligibility.
     *
     * <p><b>Business Rules:</b>
     * <ul>
     *   <li>Customer must NOT be deleted</li>
     *   <li>Customer must NOT be blocked</li>
     * </ul>
     * 
     * <p><b>Error Codes:</b>
     * <ul>
     *   <li>{@code CUSTOMER_DELETED} - Customer is soft-deleted</li>
     *   <li>{@code CUSTOMER_BLOCKED} - Customer is blocked</li>
     * </ul>
     *
     * @return semantic specification
     */
    public static Specification<Customer> canChangeEmail() {
        return new CustomerCanChangeEmailSpec();
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
     * <p><b>Error Codes:</b>
     * <ul>
     *   <li>{@code CUSTOMER_DELETED} - Customer is soft-deleted</li>
     *   <li>{@code CUSTOMER_BLOCKED} - Customer is blocked</li>
     * </ul>
     * 
     * <p><b>Design Note:</b>
     * INACTIVE represents customer-initiated closure, different from
     * SUSPENDED (temporary) or BLOCKED (compliance).
     *
     * @return semantic specification
     */
    public static Specification<Customer> canBeMarkedInactive() {
        return new CustomerCanBeMarkedInactiveSpec();
    }
}
