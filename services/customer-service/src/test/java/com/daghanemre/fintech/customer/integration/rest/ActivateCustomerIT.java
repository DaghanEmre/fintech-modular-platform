package com.daghanemre.fintech.customer.integration.rest;

import com.daghanemre.fintech.customer.domain.model.*;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;
import com.daghanemre.fintech.customer.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Customer Activation endpoint.
 *
 * <p>
 * Tests the complete stack:
 * <ul>
 * <li>REST Controller (HTTP layer)</li>
 * <li>Application Layer (use case)</li>
 * <li>Domain Layer (business rules)</li>
 * <li>Persistence Layer (JPA adapter)</li>
 * <li>Database (PostgreSQL via Testcontainers)</li>
 * </ul>
 *
 * <p>
 * <b>Test Isolation:</b>
 * Each test uses a unique email and the database is cleaned after each test.
 */
@DisplayName("POST /api/v1/customers/{id}/activate - Integration Tests")
class ActivateCustomerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Customer testCustomer;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        // Create unique email per test to avoid constraint violations
        String uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
        testCustomer = Customer.create(Email.of(uniqueEmail));
        customerRepository.save(testCustomer);
    }

    @AfterEach
    void tearDown() {
        // Clean up database after each test (isolation)
        jdbcTemplate.execute("TRUNCATE TABLE customers RESTART IDENTITY CASCADE");
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("should activate PENDING customer and return 204 No Content")
        void shouldActivatePendingCustomer() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When
            ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);

            // Then - HTTP response
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();

            // Then - Database verification
            Customer updatedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow(() -> new AssertionError("Customer should exist"));

            assertThat(updatedCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(updatedCustomer.getUpdatedAt()).isAfter(testCustomer.getUpdatedAt());
        }

        @Test
        @DisplayName("should activate SUSPENDED customer and return 204 No Content")
        void shouldActivateSuspendedCustomer() {
            // Given - First activate, then suspend
            testCustomer.activate();
            testCustomer.suspend(StateChangeReason.of("Test suspension"));
            customerRepository.save(testCustomer);

            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When
            ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            Customer reactivatedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(reactivatedCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
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
            String url = "/api/v1/customers/" + nonExistentId + "/activate";

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).contains("CUSTOMER_NOT_FOUND");
        }

        @Test
        @DisplayName("should return 400 when customerId format is invalid")
        void shouldReturn400WhenInvalidUuidFormat() {
            // Given
            String invalidId = "not-a-uuid";
            String url = "/api/v1/customers/" + invalidId + "/activate";

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("INVALID_INPUT");
        }

        @Test
        @DisplayName("should return 410 when customer is deleted")
        void shouldReturn410WhenCustomerDeleted() {
            // Given
            testCustomer.delete();
            customerRepository.save(testCustomer);

            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
            assertThat(response.getBody()).contains("CUSTOMER_DELETED");
        }

        @Test
        @DisplayName("should return 409 when customer is BLOCKED")
        void shouldReturn409WhenCustomerBlocked() {
            // Given
            testCustomer.block(StateChangeReason.of("Compliance violation"));
            customerRepository.save(testCustomer);

            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).contains("CUSTOMER_BLOCKED");
        }

        @Test
        @DisplayName("should return 409 when customer is already ACTIVE")
        void shouldReturn409WhenCustomerAlreadyActive() {
            // Given - Customer already active
            testCustomer.activate();
            customerRepository.save(testCustomer);

            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When - Activate again
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Then - Domain rejects: already active
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).contains("CUSTOMER_ALREADY_ACTIVE");
        }

        @Test
        @DisplayName("should return 409 when customer is INACTIVE")
        void shouldReturn409WhenCustomerInactive() {
            // Given
            testCustomer.markInactive();
            customerRepository.save(testCustomer);

            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).contains("INVALID_STATUS_TRANSITION");
        }
    }

    @Nested
    @DisplayName("Database Persistence Verification")
    class PersistenceVerification {

        @Test
        @DisplayName("should persist status change in database")
        void shouldPersistStatusChange() {
            // Given
            CustomerStatus initialStatus = testCustomer.getStatus();
            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When
            restTemplate.postForEntity(url, null, Void.class);

            // Then - Re-fetch from database (not cache)
            Customer persistedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(persistedCustomer.getStatus())
                    .isNotEqualTo(initialStatus)
                    .isEqualTo(CustomerStatus.ACTIVE);
        }

        @Test
        @DisplayName("should update audit timestamp in database")
        void shouldUpdateAuditTimestamp() {
            // Given
            String url = "/api/v1/customers/" + testCustomer.getId() + "/activate";

            // When
            restTemplate.postForEntity(url, null, Void.class);

            // Then
            Customer persistedCustomer = customerRepository.findById(testCustomer.getId())
                    .orElseThrow();

            assertThat(persistedCustomer.getUpdatedAt())
                    .isAfter(testCustomer.getCreatedAt());
        }
    }
}
