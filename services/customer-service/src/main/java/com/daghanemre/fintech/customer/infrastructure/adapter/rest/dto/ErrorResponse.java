package com.daghanemre.fintech.customer.infrastructure.adapter.rest.dto;

import java.time.LocalDateTime;

/**
 * Standard error response structure for the REST API.
 *
 * <p>
 * Provides a consistent error format across all endpoints.
 *
 * <p>
 * <b>Fields:</b>
 * <ul>
 * <li>{@code code} - Machine-readable error identifier (e.g.,
 * CUSTOMER_NOT_FOUND)</li>
 * <li>{@code message} - Human-readable error description</li>
 * <li>{@code timestamp} - ISO-8601 timestamp of when the error occurred</li>
 * </ul>
 *
 * <p>
 * <b>Extensibility:</b>
 * Future additions may include {@code traceId}, {@code correlationId}, or
 * {@code path}.
 */
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp) {

    /**
     * Convenience constructor that auto-populates timestamp.
     *
     * @param code    error code
     * @param message error message
     */
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now());
    }
}
