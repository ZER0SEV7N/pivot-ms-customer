package com.pe.pivot.customer.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DeleteCustomerUseCaseImpl}.
 * Verifies the soft-delete behavior and every reactive termination signal.
 */
@ExtendWith(MockitoExtension.class)
class DeleteCustomerUseCaseImplTest {

  @Mock
  private CustomerRepositoryPort repositoryPort;

  @Mock
  private FindCustomerByIdUseCase findCustomerByIdUseCase;

  private DeleteCustomerUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new DeleteCustomerUseCaseImpl(repositoryPort, findCustomerByIdUseCase);
  }

  private Customer customerWithStatus(CustomerStatus status) {
    return Customer.builder()
            .id("customer-1")
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .status(status)
            .build();
  }

  @Test
  @DisplayName("Should logically deactivate an active customer")
  void shouldDeactivateActiveCustomer() {
    // Arrange
    Customer customer = customerWithStatus(CustomerStatus.ACTIVE);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));
    when(repositoryPort.save(customer)).thenReturn(Single.just(customer));

    // Act
    Completable result = useCase.delete("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertComplete();
    assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
    assertNotNull(customer.getUpdatedAt());
    verify(repositoryPort).save(customer);
  }

  @Test
  @DisplayName("Should reject deleting an already inactive customer")
  void shouldRejectAlreadyInactiveCustomer() {
    // Arrange
    Customer customer = customerWithStatus(CustomerStatus.INACTIVE);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));

    // Act
    Completable result = useCase.delete("customer-1");

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException
                    && "Customer is already INACTIVE."
                            .equals(error.getMessage()))
            .assertNotComplete();
    verify(repositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("Should propagate the not found error")
  void shouldPropagateNotFoundError() {
    // Arrange
    when(findCustomerByIdUseCase.findById("missing")).thenReturn(
            Single.error(new CustomerNotFoundException("Customer not found with ID: missing")));

    // Act
    Completable result = useCase.delete("missing");

    // Assert
    result.test()
            .assertError(error -> error instanceof CustomerNotFoundException
                    && "Customer not found with ID: missing"
                            .equals(error.getMessage()));
    verifyNoInteractions(repositoryPort);
  }

  @Test
  @DisplayName("Should propagate the persistence failure")
  void shouldPropagatePersistenceFailure() {
    // Arrange
    Customer customer = customerWithStatus(CustomerStatus.ACTIVE);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));
    when(repositoryPort.save(customer)).thenReturn(
            Single.error(new IllegalStateException("Database unavailable.")));

    // Act
    Completable result = useCase.delete("customer-1");

    // Assert
    result.test()
            .assertError(error -> error instanceof IllegalStateException
                    && "Database unavailable."
                            .equals(error.getMessage()))
            .assertNotComplete();
  }
}
