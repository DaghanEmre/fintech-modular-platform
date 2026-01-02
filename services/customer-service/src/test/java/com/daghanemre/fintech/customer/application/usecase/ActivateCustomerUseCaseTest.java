package com.daghanemre.fintech.customer.application.usecase;

import com.daghanemre.fintech.customer.application.exception.CustomerNotFoundException;
import com.daghanemre.fintech.customer.domain.model.Customer;
import com.daghanemre.fintech.customer.domain.model.CustomerId;
import com.daghanemre.fintech.customer.domain.model.CustomerStatus;
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
 * Unit tests for ActivateCustomerUseCase.
 *
 * Tests verify:
 * - Use-case orchestration (retrieve → activate → save)
 * - Repository interaction (mock verification)
 * - Exception handling (not found, domain violations)
 *
 * Domain logic is NOT re-tested here (already covered in CustomerTest).
 */
@DisplayName("ActivateCustomerUseCase")
class ActivateCustomerUseCaseTest {

    private CustomerRepository customerRepository;
    private ActivateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        useCase = new ActivateCustomerUseCase(customerRepository);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("should reject null repository")
        void constructor_ShouldRejectNullRepository() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ActivateCustomerUseCase(null));

            assertTrue(ex.getMessage().contains("CustomerRepository must not be null"));
        }
    }

    @Nested
    @DisplayName("Execute - Happy Path")
    class ExecuteHappyPath {

        @Test
        @DisplayName("should activate pending customer successfully")
        void execute_ShouldActivatePendingCustomer() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Customer customer = Customer.create(Email.of("test@example.com"));

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When
            useCase.execute(customerId);

            // Then
            assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
            verify(customerRepository).findById(customerId);
            verify(customerRepository).save(customer);
            verifyNoMoreInteractions(customerRepository);
        }

        @Test
        @DisplayName("should activate suspended customer successfully")
        void execute_ShouldActivateSuspendedCustomer() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Customer customer = Customer.create(Email.of("test@example.com"));
            customer.activate();
            customer.suspend("Test suspension");

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When
            useCase.execute(customerId);

            // Then
            assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("should call repository methods in correct order")
        void execute_ShouldCallRepositoryMethodsInOrder() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Customer customer = Customer.create(Email.of("test@example.com"));

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When
            useCase.execute(customerId);

            // Then - verify call order
            var inOrder = inOrder(customerRepository);
            inOrder.verify(customerRepository).findById(customerId);
            inOrder.verify(customerRepository).save(customer);
            verifyNoMoreInteractions(customerRepository);
        }
    }

    @Nested
    @DisplayName("Execute - Validation")
    class ExecuteValidation {

        @Test
        @DisplayName("should reject null customerId")
        void execute_ShouldRejectNullCustomerId() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(null));

            assertTrue(ex.getMessage().contains("customerId must not be null"));
            verify(customerRepository, never()).findById(any());
            verifyNoMoreInteractions(customerRepository);
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
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.empty());

            // When/Then
            CustomerNotFoundException ex = assertThrows(
                    CustomerNotFoundException.class,
                    () -> useCase.execute(customerId));

            assertEquals(customerId, ex.getCustomerId());
            verify(customerRepository).findById(customerId);
            verify(customerRepository, never()).save(any());
            verifyNoMoreInteractions(customerRepository);
        }

        @Test
        @DisplayName("should propagate domain exception when activation not allowed")
        void execute_ShouldPropagateDomainException() {
            // Given - Active customer cannot be activated again
            CustomerId customerId = CustomerId.generate();
            Customer customer = Customer.create(Email.of("test@example.com"));
            customer.activate(); // Already active

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(customerId));

            verify(customerRepository).findById(customerId);
            verify(customerRepository, never()).save(any());
            verifyNoMoreInteractions(customerRepository);
        }

        @Test
        @DisplayName("should propagate domain exception when activating deleted customer")
        void execute_ShouldFailWhenCustomerIsDeleted() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Customer customer = Customer.create(Email.of("test@example.com"));
            customer.delete();

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(customerId));

            verify(customerRepository).findById(customerId);
            verify(customerRepository, never()).save(any());
            verifyNoMoreInteractions(customerRepository);
        }
    }
}
