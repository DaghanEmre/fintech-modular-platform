package com.daghanemre.fintech.customer.infrastructure.adapter.rest.exception;

import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import com.daghanemre.fintech.customer.infrastructure.adapter.rest.dto.ErrorResponse;
import com.daghanemre.fintech.common.specification.SpecificationException;
import com.daghanemre.fintech.common.specification.SpecificationViolation;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST API (Refactored - Specification Based).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
        private final MeterRegistry meterRegistry;

        public GlobalExceptionHandler(MeterRegistry meterRegistry) {
                this.meterRegistry = meterRegistry;
        }

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
         * Domain Layer Exceptions (Specification Pattern)
         * =========================
         */

        @ExceptionHandler(SpecificationException.class)
        public ResponseEntity<ErrorResponse> handleSpecificationException(SpecificationException ex,
                        HttpServletRequest request) {
                SpecificationViolation violation = ex.getViolation();
                HttpStatus status = SpecificationHttpStatusMapper.resolve(violation.code());

                log.warn("Domain specification violation [{}]: {}", violation.code(), violation.message());

                // Record metric
                String normalizedPath = normalizePath(request.getRequestURI());
                String operation = request.getMethod() + " " + normalizedPath;

                meterRegistry.counter("domain.violation.total",
                                Tags.of("code", violation.code(),
                                                "operation", operation))
                                .increment();

                // Ensure status is never null for the response entity
                HttpStatusCode responseStatus = (status != null) ? status : HttpStatus.INTERNAL_SERVER_ERROR;

                return ResponseEntity
                                .status(responseStatus)
                                .body(new ErrorResponse(violation.code(), violation.message()));
        }

        private String normalizePath(String path) {
                // Basic normalization: replace UUIDs with {id} placeholder to prevent high
                // cardinality
                return path.replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
                                "{id}");
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
