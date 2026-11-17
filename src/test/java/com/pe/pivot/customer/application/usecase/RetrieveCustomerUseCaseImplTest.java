package com.pe.pivot.customer.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RetrieveCustomerUseCaseImpl}.
 * Uses TestSubscriber for the Flowable query and TestObserver for the Single queries.
 */
@ExtendWith(MockitoExtension.class)
class RetrieveCustomerUseCaseImplTest {

  @Mock
  private CustomerRepositoryPort repositoryPort;

  @Mock
  private FindCustomerByIdUseCase findCustomerByIdUseCase;

  private RetrieveCustomerUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new RetrieveCustomerUseCaseImpl(repositoryPort, findCustomerByIdUseCase);
  }

  private Customer customer(String id, String documentNumber) {
    return Customer.builder()
            .id(id)
            .documentNumber(documentNumber)
            .build();
  }

  @Test
  @DisplayName("Should stream all customers in order")
  void shouldStreamAllCustomers() {
    // Arrange
    Customer first = customer("customer-1", "12345678");
    Customer second = customer("customer-2", "87654321");
    when(repositoryPort.findAll()).thenReturn(Flowable.just(first, second));

    // Act
    Flowable<Customer> result = useCase.findAll();

    // Assert
    TestSubscriber<Customer> observer = result.test();
    observer.assertNoErrors();
    observer.assertValues(first, second);
    observer.assertValueCount(2);
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should stream an empty collection when there are no customers")
  void shouldStreamEmptyCollection() {
    // Arrange
    when(repositoryPort.findAll()).thenReturn(Flowable.empty());

    // Act
    Flowable<Customer> result = useCase.findAll();

    // Assert
    result.test()
            .assertNoErrors()
            .assertNoValues()
            .assertComplete();
  }

  @Test
  @DisplayName("Should propagate the find all failure")
  void shouldPropagateFindAllFailure() {
    // Arrange
    when(repositoryPort.findAll()).thenReturn(
            Flowable.error(new IllegalStateException("Database unavailable.")));

    // Act
    Flowable<Customer> result = useCase.findAll();

    // Assert
    result.test()
            .assertNoValues()
            .assertError(IllegalStateException.class)
            .assertNotComplete();
  }

  @Test
  @DisplayName("Should delegate the id lookup to the find by id use case")
  void shouldDelegateIdLookup() {
    // Arrange
    Customer customer = customer("customer-1", "12345678");
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(customer));

    // Act
    Single<Customer> result = useCase.findById("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(customer)
            .assertComplete();
    verify(findCustomerByIdUseCase).findById("customer-1");
  }

  @Test
  @DisplayName("Should return the customer found by document number")
  void shouldReturnCustomerByDocumentNumber() {
    // Arrange
    Customer customer = customer("customer-1", "12345678");
    when(repositoryPort.findByDocumentNumber("12345678")).thenReturn(Maybe.just(customer));

    // Act
    Single<Customer> result = useCase.findByDocumentNumber("12345678");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(customer)
            .assertComplete();
  }

  @Test
  @DisplayName("Should emit CustomerNotFoundException when the document number is not found")
  void shouldEmitNotFoundForDocumentNumber() {
    // Arrange
    when(repositoryPort.findByDocumentNumber("12345678")).thenReturn(Maybe.empty());

    // Act
    Single<Customer> result = useCase.findByDocumentNumber("12345678");

    // Assert
    TestObserver<Customer> observer = result.test();
    observer.assertValueCount(0);
    observer.assertError(CustomerNotFoundException.class);
    observer.assertNotComplete();
    observer.assertError(error -> error instanceof CustomerNotFoundException
            && "Customer not found with document number: ****5678"
                    .equals(error.getMessage()));
  }

  @Test
  @DisplayName("Should propagate the document number lookup failure")
  void shouldPropagateDocumentNumberFailure() {
    // Arrange
    when(repositoryPort.findByDocumentNumber("12345678")).thenReturn(
            Maybe.error(new IllegalStateException("Database unavailable.")));

    // Act
    Single<Customer> result = useCase.findByDocumentNumber("12345678");

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(IllegalStateException.class)
            .assertNotComplete();
  }
}
