package com.daghanemre.fintech.customer.domain.model;

import com.daghanemre.fintech.common.specification.SpecificationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void create_ShouldInitializeFieldsCorrectly() {
        Email email = Email.of("john.doe@example.com");

        Customer customer = Customer.create(email);

        assertNotNull(customer.getId());
        assertEquals(email, customer.getEmail());
        assertEquals(CustomerStatus.PENDING, customer.getStatus());

        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
        assertNull(customer.getDeletedAt());
        assertFalse(customer.isDeleted());
    }

    @Test
    void activate_ShouldTransitionFromPendingToActive() {
        Customer customer = Customer.create(Email.of("test@example.com"));
        LocalDateTime before = customer.getUpdatedAt();

        customer.activate();

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    @Test
    void suspend_ShouldTransitionFromActiveToSuspended() {
        Customer customer = createActiveCustomer();

        customer.suspend(StateChangeReason.of("Fraud suspicion"));

        assertEquals(CustomerStatus.SUSPENDED, customer.getStatus());
    }

    @Test
    void suspend_ShouldThrowException_WhenReasonIsMissing() {
        Customer customer = createActiveCustomer();

        assertThrows(NullPointerException.class, () -> customer.suspend(null));
    }

    @Test
    void suspend_ShouldBeIdempotent_WhenAlreadySuspended() {
        Customer customer = createActiveCustomer();
        customer.suspend(StateChangeReason.of("Initial reason"));
        LocalDateTime firstUpdate = customer.getUpdatedAt();

        // Should return silently
        customer.suspend(StateChangeReason.of("Different reason"));

        assertEquals(CustomerStatus.SUSPENDED, customer.getStatus());
        assertEquals(firstUpdate, customer.getUpdatedAt());
    }

    @Test
    void suspend_ShouldThrowException_WhenStatusIsNotActive() {
        Customer customer = Customer.create(Email.of("test@example.com"));
        // status = PENDING

        assertThatThrownBy(() -> customer.suspend(StateChangeReason.of("Reason")))
                .isInstanceOf(SpecificationException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void block_ShouldTransitionToBlocked() {
        Customer customer = createActiveCustomer();

        customer.block(StateChangeReason.of("AML list match"));

        assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
    }

    @Test
    void block_ShouldBeIdempotent() {
        Customer customer = createActiveCustomer();

        customer.block(StateChangeReason.of("First reason"));
        LocalDateTime firstUpdate = customer.getUpdatedAt();

        customer.block(StateChangeReason.of("Second reason"));

        assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
        assertEquals(firstUpdate, customer.getUpdatedAt());
    }

    @Test
    void delete_ShouldMarkCustomerAsDeleted() {
        Customer customer = createActiveCustomer();

        customer.delete();

        assertTrue(customer.isDeleted());
        assertNotNull(customer.getDeletedAt());
    }

    @Test
    void delete_ShouldBeIdempotent() {
        Customer customer = createActiveCustomer();

        customer.delete();
        LocalDateTime firstDeletedAt = customer.getDeletedAt();

        customer.delete();

        assertEquals(firstDeletedAt, customer.getDeletedAt());
    }

    @Test
    void deletedCustomer_ShouldRejectAllStateChangingOperations() {
        Customer customer = createActiveCustomer();
        customer.delete();

        assertThatThrownBy(customer::activate)
                .isInstanceOf(SpecificationException.class)
                .extracting("code").isEqualTo("CUSTOMER_DELETED");

        assertThatThrownBy(() -> customer.suspend(StateChangeReason.of("Reason")))
                .isInstanceOf(SpecificationException.class)
                .extracting("code").isEqualTo("CUSTOMER_DELETED");

        assertThatThrownBy(() -> customer.block(StateChangeReason.of("Reason")))
                .isInstanceOf(SpecificationException.class)
                .extracting("code").isEqualTo("CUSTOMER_DELETED");

        assertThatThrownBy(() -> customer.changeEmail(Email.of("new@example.com")))
                .isInstanceOf(SpecificationException.class)
                .extracting("code").isEqualTo("CUSTOMER_DELETED");
    }

    @Test
    void activate_ShouldBeIdempotent_WhenAlreadyActive() {
        Customer customer = createActiveCustomer();
        LocalDateTime firstUpdate = customer.getUpdatedAt();

        // Should return silently without updating timestamps or throwing
        customer.activate();

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertEquals(firstUpdate, customer.getUpdatedAt());
    }

    @Test
    void changeEmail_ShouldUpdateEmailAndAuditFields() {
        Customer customer = createActiveCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        Email newEmail = Email.of("new@example.com");
        customer.changeEmail(newEmail);

        assertEquals(newEmail, customer.getEmail());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    private Customer createActiveCustomer() {
        Customer customer = Customer.create(Email.of("active@example.com"));
        customer.activate();
        return customer;
    }
}
