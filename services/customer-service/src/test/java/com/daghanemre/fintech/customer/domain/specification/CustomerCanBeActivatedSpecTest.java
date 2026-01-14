package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.SpecificationViolation;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;
import com.daghanemre.fintech.customer.domain.model.Email;
import com.daghanemre.fintech.customer.domain.model.StateChangeReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomerCanBeActivatedSpec - Semantic Specification Tests")
class CustomerCanBeActivatedSpecTest {

    private final CustomerCanBeActivatedSpec spec = new CustomerCanBeActivatedSpec();

    @Test
    @DisplayName("Should satisfy when customer is PENDING")
    void shouldSatisfy_whenCustomerIsPending() {
        // Given
        Customer customer = Customer.create(Email.of("test@example.com"));
        // Customer starts in PENDING by default

        // When
        boolean satisfied = spec.isSatisfiedBy(customer);

        // Then
        assertThat(satisfied).isTrue();
        assertThat(spec.violation(customer).isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should satisfy when customer is SUSPENDED")
    void shouldSatisfy_whenCustomerIsSuspended() {
        // Given
        Customer customer = Customer.create(Email.of("test@example.com"));
        customer.activate(); // PENDING -> ACTIVE
        customer.suspend(StateChangeReason.of("Test suspension")); // ACTIVE -> SUSPENDED

        // When
        boolean satisfied = spec.isSatisfiedBy(customer);

        // Then
        assertThat(satisfied).isTrue();
        assertThat(spec.violation(customer).isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should fail with CUSTOMER_DELETED when customer is deleted")
    void shouldFail_whenCustomerIsDeleted() {
        // Given
        Customer customer = Customer.create(Email.of("test@example.com"));
        customer.delete();

        // When
        boolean satisfied = spec.isSatisfiedBy(customer);
        SpecificationViolation violation = spec.violation(customer);

        // Then
        assertThat(satisfied).isFalse();
        assertThat(violation.code()).isEqualTo("CUSTOMER_DELETED");
        assertThat(violation.message()).contains("Cannot activate a deleted customer");
    }

    @Test
    @DisplayName("Should fail with CUSTOMER_BLOCKED when customer is blocked")
    void shouldFail_whenCustomerIsBlocked() {
        // Given
        Customer customer = Customer.create(Email.of("test@example.com"));
        customer.activate();
        customer.block(StateChangeReason.of("Test blocking"));

        // When
        boolean satisfied = spec.isSatisfiedBy(customer);
        SpecificationViolation violation = spec.violation(customer);

        // Then
        assertThat(satisfied).isFalse();
        assertThat(violation.code()).isEqualTo("CUSTOMER_BLOCKED");
        assertThat(violation.message()).contains("Cannot activate a blocked customer");
    }

    @Test
    @DisplayName("Should satisfy when customer is already ACTIVE (for idempotency)")
    void shouldSatisfy_whenCustomerIsAlreadyActive() {
        // Given
        Customer customer = Customer.create(Email.of("test@example.com"));
        customer.activate(); // PENDING -> ACTIVE

        // When
        boolean satisfied = spec.isSatisfiedBy(customer);

        // Then
        assertThat(satisfied).isTrue();
        assertThat(spec.violation(customer).isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should fail with INVALID_STATUS_TRANSITION when customer is INACTIVE")
    void shouldFail_whenCustomerIsInactive() {
        // Given
        Customer customer = Customer.create(Email.of("test@example.com"));
        customer.activate();
        customer.markInactive();

        // When
        boolean satisfied = spec.isSatisfiedBy(customer);
        SpecificationViolation violation = spec.violation(customer);

        // Then
        assertThat(satisfied).isFalse();
        assertThat(violation.code()).isEqualTo("INVALID_STATUS_TRANSITION");
        assertThat(violation.message()).contains("PENDING or SUSPENDED");
        assertThat(violation.context()).containsEntry("currentStatus", CustomerStatus.INACTIVE.name());
    }
}
