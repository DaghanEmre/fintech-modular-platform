package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;
import com.daghanemre.fintech.customer.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerAtomicSpecsTest {

    private final Email email = Email.of("test@example.com");

    @Test
    @DisplayName("CustomerNotDeletedSpec should be satisfied when deletedAt is null")
    void customerNotDeletedSpecTest() {
        CustomerNotDeletedSpec spec = new CustomerNotDeletedSpec();
        Customer customer = Customer.create(email);

        assertThat(spec.isSatisfiedBy(customer)).isTrue();

        customer.delete();
        assertThat(spec.isSatisfiedBy(customer)).isFalse();
        assertThat(spec.violation(customer).code()).isEqualTo("CUSTOMER_DELETED");
    }

    @Test
    @DisplayName("CustomerNotBlockedSpec should be satisfied when status is not BLOCKED")
    void customerNotBlockedSpecTest() {
        CustomerNotBlockedSpec spec = new CustomerNotBlockedSpec();
        Customer customer = Customer.create(email); // PENDING

        assertThat(spec.isSatisfiedBy(customer)).isTrue();

        customer.block(com.daghanemre.fintech.customer.domain.model.StateChangeReason.of("test"));
        assertThat(spec.isSatisfiedBy(customer)).isFalse();
        assertThat(spec.violation(customer).code()).isEqualTo("CUSTOMER_BLOCKED");
    }

    @Test
    @DisplayName("CustomerIsPendingSpec should be satisfied only when status is PENDING")
    void customerIsPendingSpecTest() {
        CustomerIsPendingSpec spec = new CustomerIsPendingSpec();
        Customer customer = Customer.create(email);

        assertThat(spec.isSatisfiedBy(customer)).isTrue();

        customer.activate();
        assertThat(spec.isSatisfiedBy(customer)).isFalse();
        assertThat(spec.violation(customer).code()).isEqualTo("CUSTOMER_NOT_PENDING");
    }

    @Test
    @DisplayName("CustomerIsSuspendedSpec should be satisfied only when status is SUSPENDED")
    void customerIsSuspendedSpecTest() {
        CustomerIsSuspendedSpec spec = new CustomerIsSuspendedSpec();
        Customer customer = Customer.create(email);
        customer.activate();

        assertThat(spec.isSatisfiedBy(customer)).isFalse();

        customer.suspend(com.daghanemre.fintech.customer.domain.model.StateChangeReason.of("test"));
        assertThat(spec.isSatisfiedBy(customer)).isTrue();
    }

    @Test
    @DisplayName("CustomerIsActiveSpec should be satisfied only when status is ACTIVE")
    void customerIsActiveSpecTest() {
        CustomerIsActiveSpec spec = new CustomerIsActiveSpec();
        Customer customer = Customer.create(email);

        assertThat(spec.isSatisfiedBy(customer)).isFalse();

        customer.activate();
        assertThat(spec.isSatisfiedBy(customer)).isTrue();
    }
}
