package com.pe.pivot.customer.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link FindCustomerByIdUseCaseImpl}.
 * Ensures the centralized not-found behavior is emitted through the reactive stream.
 */
@ExtendWith(MockitoExtension.class)
class FindCustomerByIdUseCaseImplTest {

  @Mock
  private CustomerRepositoryPort repositoryPort;

  private FindCustomerByIdUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new FindCustomerByIdUseCaseImpl(repositoryPort);
  }

  @Test
  @DisplayName("Should return the customer when it exists")
  void shouldReturnExistingCustomer() {
    // Arrange
    Customer customer = Customer.builder()
            .id("customer-1")
            .documentNumber("12345678")
            .build();
    when(repositoryPort.findById("customer-1")).thenReturn(Maybe.just(customer));

    // Act
    Single<Customer> result = useCase.findById("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(customer)
            .assertComplete();
    verify(repositoryPort).findById("customer-1");
  }

  @Test
  @DisplayName("Should emit CustomerNotFoundException when the customer does not exist")
  void shouldEmitNotFoundWhenCustomerDoesNotExist() {
    // Arrange
    when(repositoryPort.findById("missing")).thenReturn(Maybe.empty());

    // Act
    Single<Customer> result = useCase.findById("missing");

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof CustomerNotFoundException
                    && "Customer not found with ID: missing".equals(error.getMessage()))
            .assertNotComplete();
  }

  @Test
  @DisplayName("Should propagate the persistence failure")
  void shouldPropagatePersistenceFailure() {
    // Arrange
    when(repositoryPort.findById("customer-1")).thenReturn(
            Maybe.error(new IllegalStateException("Database unavailable.")));

    // Act
    Single<Customer> result = useCase.findById("customer-1");

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(IllegalStateException.class);
  }

  @Test
  @DisplayName("Should expose the not found message as a plain business error")
  void shouldExposeNotFoundMessage() {
    // Arrange
    when(repositoryPort.findById("missing")).thenReturn(Maybe.empty());

    // Act
    Single<Customer> result = useCase.findById("missing");

    // Assert
    io.reactivex.rxjava3.observers.TestObserver<Customer> observer = result.test();
    observer.assertError(error -> error instanceof CustomerNotFoundException
            && "Customer not found with ID: missing".equals(error.getMessage()));
  }
}
