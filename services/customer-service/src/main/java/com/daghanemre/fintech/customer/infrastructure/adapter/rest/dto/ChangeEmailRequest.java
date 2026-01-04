package com.daghanemre.fintech.customer.infrastructure.adapter.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for changing customer email address.
 *
 * <p>
 * <b>Validation Rules:</b>
 * <ul>
 * <li>newEmail must not be blank</li>
 * <li>newEmail must be valid email format (Jakarta Bean Validation)</li>
 * </ul>
 *
 * <p>
 * <b>Design Decision:</b>
 * <ul>
 * <li>Record for immutability</li>
 * <li>Bean Validation at HTTP layer (fail fast)</li>
 * <li>Domain Email value object performs additional validation</li>
 * <li>No MapStruct needed (single field, direct factory method)</li>
 * </ul>
 *
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * POST /api/v1/customers/{id}/email
 * Content-Type: application/json
 *
 * {
 *   "newEmail": "user@example.com"
 * }
 * </pre>
 */
public record ChangeEmailRequest(

        @NotBlank(message = "newEmail must not be blank") @Email(message = "newEmail must be a valid email address") String newEmail

) {
}
