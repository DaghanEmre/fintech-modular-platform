package com.daghanemre.fintech.customer.domain.exception;

/**
 * Base exception for all customer domain rule violations.
 *
 * <p>
 * This exception hierarchy represents business rule violations
 * that occur within the Customer bounded context.
 *
 * <p>
 * <b>Design Principles:</b>
 * <ul>
 * <li>Unchecked exception (RuntimeException)</li>
 * <li>Domain-specific and meaningful</li>
 * <li>Type-based exception handling (no string matching)</li>
 * <li>Clear mapping to HTTP status codes at adapter layer</li>
 * </ul>
 *
 * <p>
 * <b>Subclasses:</b>
 * <ul>
 * <li>{@link CustomerDeletedException} - Customer is soft-deleted (410
 * Gone)</li>
 * <li>{@link CustomerAlreadyActiveException} - Already in active state (409
 * Conflict)</li>
 * <li>{@link CustomerBlockedException} - Customer is blocked (403
 * Forbidden)</li>
 * <li>{@link InvalidCustomerStatusTransitionException} - Invalid state change
 * (409 Conflict)</li>
 * </ul>
 */
public abstract class CustomerDomainException extends RuntimeException {

    protected CustomerDomainException(String message) {
        super(message);
    }

    protected CustomerDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
