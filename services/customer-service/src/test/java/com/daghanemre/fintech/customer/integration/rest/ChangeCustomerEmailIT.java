package com.daghanemre.fintech.customer.integration.rest;

import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.Email;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;
import com.daghanemre.fintech.customer.infrastructure.adapter.rest.dto.ChangeEmailRequest;
import com.daghanemre.fintech.customer.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Customer Email Change endpoint.
 *
 * <p>
 * Tests the complete stack:
 * <ul>
 * <li>REST Controller (HTTP layer + DTO validation)</li>
 * <li>Application Layer (use case)</li>
 * <li>Domain Layer (business rules + Email value object)</li>
 * <li>Persistence Layer (JPA adapter)</li>
 * <li>Database (PostgreSQL via Testcontainers)</li>
 * </ul>
 *
 * <p>
 * <b>Test Strategy:</b>
 * <ul>
 * <li>Happy path: Valid email change</li>
 * <li>Validation: Bean Validation + domain validation</li>
 * <li>Error cases: Not found, deleted, invalid format</li>
 * <li>Idempotency: Same email change</li>
 * </ul>
 */
@DisplayName("POST /api/v1/customers/{id}/email - Integration Tests")
class ChangeCustomerEmailIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        String uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
        testCustomer = Customer.create(Email.of(uniqueEmail));
        customerRepository.save(testCustomer);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM customers");
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("should change email successfully and return 204 No Content")
        void shouldChangeEmailSuccessfully() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("new@example.com");

            // When
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    Void.class);

            // Then - HTTP response
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();

            // Then - Database verification
            Customer updatedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(updatedCustomer.getEmail().value()).isEqualTo(request.newEmail());
            assertThat(updatedCustomer.getUpdatedAt()).isAfter(testCustomer.getUpdatedAt());
        }

        @Test
        @DisplayName("should be idempotent when email is same")
        void shouldBeIdempotentWhenEmailIsSame() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest(testCustomer.getEmail().value());

            // When
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            Customer unchangedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(unchangedCustomer.getEmail().value()).isEqualTo(testCustomer.getEmail().value());
        }

        @Test
        @DisplayName("should normalize email (lowercase)")
        void shouldNormalizeEmail() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("NEW-EMAIL@example.com");

            // When
            ResponseEntity<Void> response = restTemplate.postForEntity(url, new HttpEntity<>(request), Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            Customer updatedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(updatedCustomer.getEmail().value()).isEqualTo("new-email@example.com");
        }
    }

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("should return 400 when newEmail is blank")
        void shouldReturn400WhenEmailIsBlank() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("   ");

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("VALIDATION_ERROR");
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void shouldReturn400WhenEmailFormatIsInvalid() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("not-an-email");

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("VALIDATION_ERROR");
        }

        @Test
        @DisplayName("should return 400 when email exceeds max length")
        void shouldReturn400WhenEmailTooLong() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            String longEmail = "a".repeat(250) + "@example.com"; // > 254 chars
            ChangeEmailRequest request = new ChangeEmailRequest(longEmail);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("VALIDATION_ERROR");
        }

        @Test
        @DisplayName("should return 400 when customerId is invalid UUID")
        void shouldReturn400WhenCustomerIdInvalid() {
            // Given
            String url = "/api/v1/customers/not-a-uuid/email";
            ChangeEmailRequest request = new ChangeEmailRequest("new@example.com");

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("INVALID_INPUT");
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("should return 404 when customer does not exist")
        void shouldReturn404WhenCustomerNotFound() {
            // Given
            String nonExistentId = "550e8400-e29b-41d4-a716-446655440000";
            String url = "/api/v1/customers/" + nonExistentId + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("new@example.com");

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).contains("CUSTOMER_NOT_FOUND");
            assertThat(response.getBody()).contains(nonExistentId);
        }

        @Test
        @DisplayName("should return 410 when customer is deleted")
        void shouldReturn410WhenCustomerDeleted() {
            // Given
            testCustomer.delete();
            customerRepository.save(testCustomer);

            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("new@example.com");

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(request),
                    String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
            assertThat(response.getBody()).contains("CUSTOMER_DELETED");
        }
    }

    @Nested
    @DisplayName("Database Persistence Verification")
    class PersistenceVerification {

        @Test
        @DisplayName("should persist email change in database")
        void shouldPersistEmailChange() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("persisted@example.com");

            // When
            restTemplate.postForEntity(url, new HttpEntity<>(request), Void.class);

            // Then - Re-fetch from database
            Customer persistedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(persistedCustomer.getEmail().value())
                    .isEqualTo("persisted@example.com");
        }

        @Test
        @DisplayName("should update audit timestamp in database")
        void shouldUpdateAuditTimestamp() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/email";
            ChangeEmailRequest request = new ChangeEmailRequest("timestamped@example.com");

            // When
            restTemplate.postForEntity(url, new HttpEntity<>(request), Void.class);

            // Then
            Customer persistedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(persistedCustomer.getUpdatedAt())
                    .isAfter(testCustomer.getCreatedAt());
        }
    }
}
