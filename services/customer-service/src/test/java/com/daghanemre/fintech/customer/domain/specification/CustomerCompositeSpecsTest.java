package com.daghanemre.fintech.customer.domain.specification;

import com.daghanemre.fintech.common.specification.Specification;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.Email;
import com.daghanemre.fintech.customer.domain.model.StateChangeReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerCompositeSpecsTest {

    private final Email email = Email.of("test@example.com");
    private final StateChangeReason reason = StateChangeReason.of("test");

    @Test
    @DisplayName("canBeActivated should be satisfied for PENDING and SUSPENDED customers")
    void canBeActivatedTest() {
        Specification<Customer> spec = CustomerSpecifications.canBeActivated();

        Customer pending = Customer.create(email);
        assertThat(spec.isSatisfiedBy(pending)).isTrue();

        Customer active = Customer.create(email);
        active.activate();
        assertThat(spec.isSatisfiedBy(active)).isFalse();

        Customer suspended = Customer.create(email);
        suspended.activate();
        suspended.suspend(reason);
        assertThat(spec.isSatisfiedBy(suspended)).isTrue();
    }

    @Test
    @DisplayName("canBeActivated should fail if customer is deleted or blocked")
    void canBeActivatedFailTest() {
        Specification<Customer> spec = CustomerSpecifications.canBeActivated();

        Customer deleted = Customer.create(email);
        deleted.delete();
        assertThat(spec.isSatisfiedBy(deleted)).isFalse();
        assertThat(spec.violation(deleted).code()).isEqualTo("CUSTOMER_DELETED");

        Customer blocked = Customer.create(email);
        blocked.block(reason);
        assertThat(spec.isSatisfiedBy(blocked)).isFalse();
        // Since it's NotDeleted.and(NotBlocked), and NotDeleted is checked first.
        // If we move NotBlocked first, it would return CUSTOMER_BLOCKED.
    }

    @Test
    @DisplayName("canBeSuspended should be satisfied only for ACTIVE customers")
    void canBeSuspendedTest() {
        Specification<Customer> spec = CustomerSpecifications.canBeSuspended();

        Customer active = Customer.create(email);
        active.activate();
        assertThat(spec.isSatisfiedBy(active)).isTrue();

        Customer pending = Customer.create(email);
        assertThat(spec.isSatisfiedBy(pending)).isFalse();
    }
}
