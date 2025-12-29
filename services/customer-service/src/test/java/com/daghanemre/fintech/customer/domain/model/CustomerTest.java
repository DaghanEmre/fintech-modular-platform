package com.daghanemre.fintech.customer.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void create_ShouldInitializeFieldsCorrectly() {
        Email email = Email.of("john.doe@example.com");
        Customer customer = Customer.create(email, "John", "Doe");

        assertNotNull(customer.getId());
        assertEquals(email, customer.getEmail());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals(CustomerStatus.PENDING, customer.getStatus());
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
        assertNull(customer.getDeletedAt());
        assertFalse(customer.isDeleted());
        assertEquals(0L, customer.getVersion());
    }

    @Test
    void activate_ShouldTransitionFromPendingToActive() {
        Customer customer = Customer.create(Email.of("test@example.com"), "Test", "User");
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
        assertThrows(IllegalArgumentException.class, () -> customer.suspend("  "));
    }

    @Test
    void suspend_ShouldThrowException_WhenStatusIsNotActive() {
        Customer customer = Customer.create(Email.of("test@example.com"), "Test", "User");
        // Status is PENDING

        assertThrows(IllegalStateException.class, () -> customer.suspend("Reason"));
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
        customer.block("Reason 1");
        LocalDateTime firstUpdate = customer.getUpdatedAt();

        customer.block("Reason 2");

        assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
        // For idempotent call, updatedAt should ideally NOT update if we return early.
        // The implementation does `if (blocked) return;` so updatedAt should match
        // firstUpdate.
        assertEquals(firstUpdate, customer.getUpdatedAt());
    }

    @Test
    void delete_ShouldMarkAsDeletedAndInactive() {
        Customer customer = createActiveCustomer();
        customer.delete();

        assertTrue(customer.isDeleted());
        assertNotNull(customer.getDeletedAt());
        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
    }

    @Test
    void delete_ShouldBeIdempotent() {
        Customer customer = createActiveCustomer();
        customer.delete();
        LocalDateTime firstDelete = customer.getDeletedAt();

        customer.delete();

        assertEquals(firstDelete, customer.getDeletedAt());
    }

    @Test
    void ensureNotDeleted_ShouldPreventOperations_WhenDeleted() {
        Customer customer = createActiveCustomer();
        customer.delete();

        assertThrows(IllegalStateException.class, () -> customer.activate());
        assertThrows(IllegalStateException.class, () -> customer.suspend("Reason"));
        assertThrows(IllegalStateException.class, () -> customer.block("Reason"));
        assertThrows(IllegalStateException.class, () -> customer.changeEmail(Email.of("new@example.com")));
        assertThrows(IllegalStateException.class, () -> customer.changeName("New", "Name"));
    }

    @Test
    void changeName_ShouldUpdateNameAndAudit() {
        Customer customer = createActiveCustomer();
        LocalDateTime before = customer.getUpdatedAt();

        customer.changeName("Jane", "Smith");

        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertTrue(customer.getUpdatedAt().isAfter(before) || customer.getUpdatedAt().equals(before));
    }

    private Customer createActiveCustomer() {
        Customer customer = Customer.create(Email.of("active@example.com"), "Active", "User");
        customer.activate();
        return customer;
    }
}
