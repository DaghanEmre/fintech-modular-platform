package com.daghanemre.fintech.customer.infrastructure.persistence.jpa.adapter;

import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.Email;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;
import com.daghanemre.fintech.customer.infrastructure.persistence.AbstractJpaIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class CustomerJpaRepositoryAdapterIT extends AbstractJpaIntegrationTest {

        @Autowired
        CustomerRepository customerRepository;

        @Test
        void shouldPersistAndReconstituteCustomer() {
                Customer customer = Customer.create(
                                Email.of("john.doe@test.com"));

                customerRepository.save(customer);

                Optional<Customer> reloaded = customerRepository.findById(customer.getId());

                assertThat(reloaded).isPresent();

                Customer loaded = reloaded.get();
                assertThat(loaded.getId()).isEqualTo(customer.getId());
                assertThat(loaded.getEmail()).isEqualTo(customer.getEmail());
                assertThat(loaded.getStatus()).isEqualTo(customer.getStatus());
                assertThat(loaded.getCreatedAt()).isCloseTo(customer.getCreatedAt(), within(1, ChronoUnit.MILLIS));
                assertThat(loaded.getDeletedAt()).isNull();
        }

        @Test
        void shouldEnforceUniqueEmailConstraint() {
                Customer first = Customer.create(
                                Email.of("unique@test.com"));
                customerRepository.save(first);

                Customer second = Customer.create(
                                Email.of("unique@test.com"));

                assertThatThrownBy(() -> customerRepository.save(second))
                                .isInstanceOf(Exception.class);
        }

        @Test
        void shouldLoadSoftDeletedCustomer() {
                Customer customer = Customer.create(
                                Email.of("deleted@test.com"));
                customer.activate();
                customer.delete();

                customerRepository.save(customer);

                Optional<Customer> reloaded = customerRepository.findByEmail(Email.of("deleted@test.com"));

                assertThat(reloaded).isPresent();
                assertThat(reloaded.get().isDeleted()).isTrue();
        }

        @Test
        void shouldPreserveAuditFieldsAcrossPersistence() {
                Customer customer = Customer.create(
                                Email.of("audit@test.com"));
                customer.activate();

                customerRepository.save(customer);

                Customer reloaded = customerRepository
                                .findById(customer.getId())
                                .orElseThrow();

                assertThat(reloaded.getCreatedAt()).isCloseTo(customer.getCreatedAt(), within(1, ChronoUnit.MILLIS));
                assertThat(reloaded.getUpdatedAt()).isCloseTo(customer.getUpdatedAt(), within(1, ChronoUnit.MILLIS));
        }
}
