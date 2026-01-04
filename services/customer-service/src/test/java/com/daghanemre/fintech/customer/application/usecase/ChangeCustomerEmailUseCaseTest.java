package com.daghanemre.fintech.customer.application.usecase;

import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerId;
import com.daghanemre.fintech.customer.domain.model.Email;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChangeCustomerEmailUseCase.
 *
 * Tests verify:
 * - Use-case orchestration (retrieve → changeEmail → save)
 * - Repository interaction (mock verification)
 * - Exception handling (not found, deleted customer)
 *
 * Domain logic is NOT re-tested here (already covered in CustomerTest).
 */
@DisplayName("ChangeCustomerEmailUseCase")
class ChangeCustomerEmailUseCaseTest {

    private CustomerRepository customerRepository;
    private ChangeCustomerEmailUseCase useCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        useCase = new ChangeCustomerEmailUseCase(customerRepository);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("should reject null repository")
        void constructor_ShouldRejectNullRepository() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ChangeCustomerEmailUseCase(null));

            assertTrue(ex.getMessage().contains("customerRepository must not be null"));
        }
    }

    @Nested
    @DisplayName("Execute - Happy Path")
    class ExecuteHappyPath {

        @Test
        @DisplayName("should change email successfully")
        void execute_ShouldChangeEmail() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Email oldEmail = Email.of("old@example.com");
            Email newEmail = Email.of("new@example.com");
            Customer customer = Customer.create(oldEmail);

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When
            useCase.execute(customerId, newEmail);

            // Then
            assertEquals(newEmail, customer.getEmail());
            verify(customerRepository).findById(customerId);
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("should be idempotent when email is same")
        void execute_ShouldBeIdempotentWhenEmailIsSame() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Email email = Email.of("test@example.com");
            Customer customer = Customer.create(email);

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When
            useCase.execute(customerId, email);

            // Then - Email unchanged
            assertEquals(email, customer.getEmail());
            // But save is still called (aggregate touched)
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("should call repository methods in correct order")
        void execute_ShouldCallRepositoryMethodsInOrder() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Customer customer = Customer.create(Email.of("old@example.com"));
            Email newEmail = Email.of("new@example.com");

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When
            useCase.execute(customerId, newEmail);

            // Then - verify call order
            var inOrder = inOrder(customerRepository);
            inOrder.verify(customerRepository).findById(customerId);
            inOrder.verify(customerRepository).save(customer);
        }
    }

    @Nested
    @DisplayName("Execute - Validation")
    class ExecuteValidation {

        @Test
        @DisplayName("should reject null customerId")
        void execute_ShouldRejectNullCustomerId() {
            Email newEmail = Email.of("test@example.com");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(null, newEmail));

            assertTrue(ex.getMessage().contains("customerId must not be null"));
            verify(customerRepository, never()).findById(any());
        }

        @Test
        @DisplayName("should reject null newEmail")
        void execute_ShouldRejectNullNewEmail() {
            CustomerId customerId = CustomerId.generate();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(customerId, null));

            assertTrue(ex.getMessage().contains("newEmail must not be null"));
            verify(customerRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Execute - Error Cases")
    class ExecuteErrorCases {

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer not found")
        void execute_ShouldThrowExceptionWhenCustomerNotFound() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Email newEmail = Email.of("new@example.com");

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.empty());

            // When/Then
            CustomerNotFoundException ex = assertThrows(
                    CustomerNotFoundException.class,
                    () -> useCase.execute(customerId, newEmail));

            assertEquals(customerId, ex.getCustomerId());
            verify(customerRepository).findById(customerId);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should propagate domain exception when customer is deleted")
        void execute_ShouldFailWhenCustomerIsDeleted() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Customer customer = Customer.create(Email.of("old@example.com"));
            customer.delete();

            Email newEmail = Email.of("new@example.com");

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(customerId, newEmail));

            verify(customerRepository).findById(customerId);
            verify(customerRepository, never()).save(any());
        }
    }
}
