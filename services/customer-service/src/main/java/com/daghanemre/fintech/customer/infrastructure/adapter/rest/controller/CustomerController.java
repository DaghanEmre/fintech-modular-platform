package com.daghanemre.fintech.customer.infrastructure.adapter.rest.controller;

import com.daghanemre.fintech.customer.application.usecase.ActivateCustomerUseCase;
import com.daghanemre.fintech.customer.application.usecase.ChangeCustomerEmailUseCase;
import com.daghanemre.fintech.customer.domain.model.CustomerId;
import com.daghanemre.fintech.customer.domain.model.Email;
import com.daghanemre.fintech.customer.infrastructure.adapter.rest.dto.ChangeEmailRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for customer management operations.
 *
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>HTTP request handling</li>
 * <li>Transaction boundary management (ADR-0003)</li>
 * <li>Use case orchestration</li>
 * <li>HTTP response mapping</li>
 * </ul>
 *
 * <p>
 * <b>Does NOT contain:</b>
 * <ul>
 * <li>Business logic (lives in domain)</li>
 * <li>Direct repository access</li>
 * <li>Input format validation (delegated to domain factories)</li>
 * </ul>
 *
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 * <li>Transaction boundary: Controller level (ADR-0003)</li>
 * <li>Validation: Domain factory methods (single source of truth)</li>
 * <li>No facade: Single use case per endpoint (YAGNI principle)</li>
 * </ul>
 *
 * @see ActivateCustomerUseCase
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ActivateCustomerUseCase activateCustomerUseCase;
    private final ChangeCustomerEmailUseCase changeCustomerEmailUseCase;

    /**
     * Activates a customer account.
     *
     * <p>
     * <b>Business Rules:</b> (Enforced in domain layer)
     * <ul>
     * <li>Customer must exist</li>
     * <li>Customer must be in PENDING or SUSPENDED status</li>
     * <li>Customer must not be deleted</li>
     * </ul>
     *
     * <p>
     * <b>HTTP Status Codes:</b>
     * <ul>
     * <li>204 No Content - Success</li>
     * <li>400 Bad Request - Invalid UUID format</li>
     * <li>404 Not Found - Customer does not exist</li>
     * <li>409 Conflict - Customer already active or invalid state</li>
     * <li>410 Gone - Customer is deleted</li>
     * </ul>
     *
     * @param customerId UUID of the customer to activate (path variable)
     * @return 204 No Content on success
     * @throws IllegalArgumentException if UUID format is invalid (400)
     */
    @PostMapping("/{id}/activate")
    @Transactional
    public ResponseEntity<Void> activateCustomer(
            @PathVariable("id") String customerId) {
        // Convert and validate (domain factory handles format validation)
        CustomerId id = CustomerId.from(customerId);

        // Execute use case within transaction boundary
        activateCustomerUseCase.execute(id);

        // 204 No Content (command succeeded, no response body)
        return ResponseEntity.noContent().build();
    }

    /**
     * Changes a customer's email address.
     *
     * <p>
     * <b>Business Rules:</b> (Enforced in domain layer)
     * <ul>
     * <li>Customer must exist</li>
     * <li>Customer must not be deleted</li>
     * <li>New email must be valid format</li>
     * <li>Idempotent: same email = no-op</li>
     * </ul>
     *
     * <p>
     * <b>HTTP Status Codes:</b>
     * <ul>
     * <li>204 No Content - Success</li>
     * <li>400 Bad Request - Invalid UUID or email format</li>
     * <li>404 Not Found - Customer does not exist</li>
     * <li>410 Gone - Customer is deleted</li>
     * </ul>
     *
     * @param customerId UUID of the customer (path variable)
     * @param request    request body containing new email
     * @return 204 No Content on success
     * @throws IllegalArgumentException  if UUID or email format is invalid (400)
     * @throws CustomerNotFoundException if customer does not exist (404)
     * @throws IllegalStateException     if customer is deleted (410)
     */
    @PostMapping("/{id}/email")
    @Transactional
    public ResponseEntity<Void> changeCustomerEmail(
            @PathVariable("id") String customerId,
            @Valid @RequestBody ChangeEmailRequest request) {
        // Convert path variable and request body to domain types
        CustomerId id = CustomerId.from(customerId);
        Email newEmail = Email.of(request.newEmail());

        // Execute use case within transaction boundary
        changeCustomerEmailUseCase.execute(id, newEmail);

        // 204 No Content (command succeeded, no response body)
        return ResponseEntity.noContent().build();
    }
}
