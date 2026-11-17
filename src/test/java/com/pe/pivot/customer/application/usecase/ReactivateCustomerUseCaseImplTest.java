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
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ReactivateCustomerUseCaseImpl}.
 * Covers both allowed origin statuses and the rejected active state.
 */
@ExtendWith(MockitoExtension.class)
class ReactivateCustomerUseCaseImplTest {

  @Mock
  private CustomerRepositoryPort repositoryPort;

  @Mock
  private FindCustomerByIdUseCase findCustomerByIdUseCase;

  private ReactivateCustomerUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new ReactivateCustomerUseCaseImpl(repositoryPort, findCustomerByIdUseCase);
  }

  private Customer customerWithStatus(CustomerStatus status) {
    return Customer.builder()
            .id("customer-1")
            .documentNumber("12345678")
            .status(status)
            .build();
  }

  @Test
  @DisplayName("Should reactivate an inactive customer")
  void shouldReactivateInactiveCustomer() {
    // Arrange
    Customer customer = customerWithStatus(CustomerStatus.INACTIVE);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));
    when(repositoryPort.save(customer)).thenReturn(Single.just(customer));

    // Act
    Single<Customer> result = useCase.reactivate("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(customer)
            .assertComplete();
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertNotNull(customer.getUpdatedAt());
    verify(repositoryPort).save(customer);
  }

  @Test
  @DisplayName("Should reactivate a blocked customer")
  void shouldReactivateBlockedCustomer() {
    // Arrange
    Customer customer = customerWithStatus(CustomerStatus.BLOCKED);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));
    when(repositoryPort.save(customer)).thenReturn(Single.just(customer));

    // Act
    Single<Customer> result = useCase.reactivate("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(customer)
            .assertComplete();
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
  }

  @Test
  @DisplayName("Should reject reactivating an already active customer")
  void shouldRejectAlreadyActiveCustomer() {
    // Arrange
    Customer customer = customerWithStatus(CustomerStatus.ACTIVE);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));

    // Act
    Single<Customer> result = useCase.reactivate("customer-1");

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof BusinessRuleException
                    && "Customer is already ACTIVE."
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
    Single<Customer> result = useCase.reactivate("missing");

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof CustomerNotFoundException
                    && "Customer not found with ID: missing"
                            .equals(error.getMessage()));
    verifyNoInteractions(repositoryPort);
  }

  @Test
  @DisplayName("Should propagate the persistence failure")
  void shouldPropagatePersistenceFailure() {
    // Arrange
    Customer customer = customerWithStatus(CustomerStatus.INACTIVE);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));
    when(repositoryPort.save(customer)).thenReturn(
            Single.error(new IllegalStateException("Database unavailable.")));

    // Act
    Single<Customer> result = useCase.reactivate("customer-1");

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof IllegalStateException
                    && "Database unavailable."
                            .equals(error.getMessage()));
  }
}
