package com.daghanemre.fintech.customer.infrastructure.adapter.rest.exception;

import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import com.daghanemre.fintech.customer.domain.exception.*;
import com.daghanemre.fintech.customer.infrastructure.adapter.rest.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

/**
 * Global exception handler for REST API (Refactored - Type-Based).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /*
         * =========================
         * Application Layer Exceptions
         * =========================
         */

        @ExceptionHandler(CustomerNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
                log.warn("Customer not found: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse("CUSTOMER_NOT_FOUND", ex.getMessage()));
        }

        /*
         * =========================
         * Domain Layer Exceptions
         * =========================
         */

        @ExceptionHandler(CustomerDeletedException.class)
        public ResponseEntity<ErrorResponse> handleCustomerDeleted(CustomerDeletedException ex) {
                log.warn("Attempted operation on deleted customer: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.GONE)
                                .body(new ErrorResponse("CUSTOMER_DELETED", ex.getMessage()));
        }

        @ExceptionHandler(CustomerAlreadyActiveException.class)
        public ResponseEntity<ErrorResponse> handleCustomerAlreadyActive(CustomerAlreadyActiveException ex) {
                log.warn("Customer already active: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(new ErrorResponse("CUSTOMER_ALREADY_ACTIVE", ex.getMessage()));
        }

        @ExceptionHandler(CustomerBlockedException.class)
        public ResponseEntity<ErrorResponse> handleCustomerBlocked(CustomerBlockedException ex) {
                log.warn("Customer blocked: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(new ErrorResponse("CUSTOMER_BLOCKED", ex.getMessage()));
        }

        @ExceptionHandler(InvalidCustomerStatusTransitionException.class)
        public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
                        InvalidCustomerStatusTransitionException ex) {
                log.warn("Invalid status transition: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(new ErrorResponse("INVALID_STATUS_TRANSITION", ex.getMessage()));
        }

        /*
         * =========================
         * Validation Exceptions
         * =========================
         */

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
                String message = ex.getBindingResult().getFieldErrors().stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Validation failed");

                log.warn("Validation error: {}", message);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse("VALIDATION_ERROR", message));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
                String message = ex.getConstraintViolations().stream()
                                .findFirst()
                                .map(v -> v.getMessage())
                                .orElse("Validation failed");

                log.warn("Constraint violation: {}", message);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse("VALIDATION_ERROR", message));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
                log.warn("Invalid input: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse("INVALID_INPUT", ex.getMessage()));
        }

        /*
         * =========================
         * Catch-All Handler
         * =========================
         */

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
                log.error("Unexpected error occurred", ex);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
        }
}
