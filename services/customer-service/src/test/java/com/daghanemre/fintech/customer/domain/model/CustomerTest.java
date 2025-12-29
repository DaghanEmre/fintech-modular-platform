package com.daghanemre.fintech.customer.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

        customer.suspend("Fraud suspicion");

        assertEquals(CustomerStatus.SUSPENDED, customer.getStatus());
    }

    @Test
    void suspend_ShouldThrowException_WhenReasonIsMissing() {
        Customer customer = createActiveCustomer();

        assertThrows(IllegalArgumentException.class, () -> customer.suspend(null));
        assertThrows(IllegalArgumentException.class, () -> customer.suspend("   "));
    }

    @Test
    void suspend_ShouldThrowException_WhenStatusIsNotActive() {
        Customer customer = Customer.create(Email.of("test@example.com"));
        // status = PENDING

        assertThrows(IllegalStateException.class,
                () -> customer.suspend("Reason"));
    }

    @Test
    void block_ShouldTransitionToBlocked() {
        Customer customer = createActiveCustomer();

        customer.block("AML list match");

        assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
    }

    @Test
    void block_ShouldBeIdempotent() {
        Customer customer = createActiveCustomer();

        customer.block("First reason");
        LocalDateTime firstUpdate = customer.getUpdatedAt();

        customer.block("Second reason");

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

        assertThrows(IllegalStateException.class, customer::activate);
        assertThrows(IllegalStateException.class,
                () -> customer.suspend("Reason"));
        assertThrows(IllegalStateException.class,
                () -> customer.block("Reason"));
        assertThrows(IllegalStateException.class,
                () -> customer.changeEmail(Email.of("new@example.com")));
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
