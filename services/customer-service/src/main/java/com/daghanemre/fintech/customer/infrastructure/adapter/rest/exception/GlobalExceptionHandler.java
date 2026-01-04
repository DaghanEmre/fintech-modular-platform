package com.daghanemre.fintech.customer.infrastructure.adapter.rest.exception;

import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import com.daghanemre.fintech.customer.infrastructure.adapter.rest.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the REST API.
 *
 * <p>
 * Maps application and domain exceptions to appropriate HTTP responses.
 *
 * <p>
 * <b>HTTP Status Mapping:</b>
 * <ul>
 * <li>400 Bad Request - Input validation failures</li>
 * <li>404 Not Found - Resource not found</li>
 * <li>409 Conflict - Business rule violation (state conflict)</li>
 * <li>410 Gone - Resource permanently deleted</li>
 * <li>500 Internal Server Error - Unexpected errors</li>
 * </ul>
 *
 * <p>
 * <b>Design Note:</b>
 * Context-aware exception mapping for {@link IllegalStateException} allows
 * distinguishing between "deleted" (410) and "conflict" (409) scenarios.
 * This is a pragmatic MVP approach; domain-specific exceptions are planned
 * for future iterations.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link CustomerNotFoundException} from use-case layer.
     * Maps to 404 Not Found.
     *
     * @param ex the exception
     * @return 404 response with error details
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        log.warn("Customer not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("CUSTOMER_NOT_FOUND", ex.getMessage()));
    }

    /**
     * Handles {@link IllegalStateException} from domain layer.
     *
     * <p>
     * <b>Context-aware mapping:</b>
     * <ul>
     * <li>Deleted customer → 410 Gone</li>
     * <li>Invalid state transition → 409 Conflict</li>
     * </ul>
     *
     * @param ex the exception
     * @return 410 or 409 response based on context
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage();
        String lowerMessage = message != null ? message.toLowerCase() : "";

        // Deleted customers are permanently gone
        if (lowerMessage.contains("deleted")) {
            log.warn("Attempted operation on deleted customer: {}", message);
            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body(new ErrorResponse("CUSTOMER_DELETED", message));
        }

        // Other state violations are conflicts
        log.warn("Invalid state transition: {}", message);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("INVALID_STATE_TRANSITION", message));
    }

    /**
     * Handles Bean Validation errors.
     * Maps to 400 Bad Request.
     *
     * @param ex the constraint violation exception
     * @return 400 response with validation error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("Validation failed");

        log.warn("Validation error: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", errorMessage));
    }

    /**
     * Handles Spring's MethodArgumentNotValidException (thrown for @Valid failures
     * on DTOs).
     * Maps to 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        log.warn("Method argument validation error: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", errorMessage));
    }

    /**
     * Handles {@link IllegalArgumentException} from domain layer input validation.
     * Maps to 400 Bad Request.
     *
     * @param ex the exception
     * @return 400 response with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid input: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_INPUT", ex.getMessage()));
    }

    /**
     * Catches all unexpected exceptions.
     * Maps to 500 Internal Server Error.
     *
     * <p>
     * <b>Security Note:</b>
     * Internal error details are NOT exposed to the client.
     * Exception is logged for investigation.
     *
     * @param ex the unexpected exception
     * @return 500 response with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
