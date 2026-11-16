package com.pe.pivot.customer.infrastructure.adapter.out.persistence;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.infrastructure.adapter.out.persistence.entity.CustomerDocument;
import com.pe.pivot.customer.infrastructure.adapter.out.persistence.mapper.CustomerPersistenceMapper;
import com.pe.pivot.customer.infrastructure.adapter.out.persistence.repository.ReactiveCustomerMongoRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link CustomerPersistenceAdapter}.
 * Exercises every bridge between Reactor and RxJava 3, including empty and error signals.
 */
@ExtendWith(MockitoExtension.class)
class CustomerPersistenceAdapterTest {

  @Mock
  private ReactiveCustomerMongoRepository mongoRepository;

  @Mock
  private CustomerPersistenceMapper mapper;

  private CustomerPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new CustomerPersistenceAdapter(mongoRepository, mapper);
  }

  private Customer customer(String id) {
    return Customer.builder()
            .id(id)
            .documentNumber("12345678")
            .build();
  }

  private CustomerDocument document(String id) {
    return CustomerDocument.builder()
            .id(id)
            .documentNumber("12345678")
            .build();
  }

  @Test
  @DisplayName("should save a customer and return the persisted domain object")
  void shouldSaveCustomer() {
    // Arrange
    Customer domain = customer("customer-1");
    CustomerDocument entity = document("customer-1");
    Customer persisted = customer("customer-1");
    when(mapper.toDocument(domain)).thenReturn(entity);
    when(mongoRepository.save(entity)).thenReturn(Mono.just(entity));
    when(mapper.toDomain(entity)).thenReturn(persisted);

    // Act
    Single<Customer> result = adapter.save(domain);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(persisted)
            .assertComplete();
    verify(mongoRepository).save(entity);
  }

  @Test
  @DisplayName("should propagate the save failure")
  void shouldPropagateSaveFailure() {
    // Arrange
    Customer domain = customer("customer-1");
    CustomerDocument entity = document("customer-1");
    when(mapper.toDocument(domain)).thenReturn(entity);
    when(mongoRepository.save(entity)).thenReturn(
            Mono.error(new IllegalStateException("Database unavailable.")));

    // Act
    Single<Customer> result = adapter.save(domain);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof IllegalStateException && "Database unavailable.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should stream all customers from the database")
  void shouldFindAllCustomers() {
    // Arrange
    CustomerDocument firstDocument = document("customer-1");
    CustomerDocument secondDocument = document("customer-2");
    Customer first = customer("customer-1");
    Customer second = customer("customer-2");
    when(mapper.toDomain(firstDocument)).thenReturn(first);
    when(mapper.toDomain(secondDocument)).thenReturn(second);
    when(mongoRepository.findAll()).thenReturn(Flux.just(firstDocument, secondDocument));

    // Act
    Flowable<Customer> result = adapter.findAll();

    // Assert
    result.test()
            .assertNoErrors()
            .assertValues(first, second)
            .assertValueCount(2)
            .assertComplete();
  }

  @Test
  @DisplayName("should stream an empty result when the database has no customers")
  void shouldFindAllReturnEmpty() {
    // Arrange
    when(mongoRepository.findAll()).thenReturn(Flux.empty());

    // Act
    Flowable<Customer> result = adapter.findAll();

    // Assert
    result.test()
            .assertNoErrors()
            .assertNoValues()
            .assertComplete();
  }

  @Test
  @DisplayName("should propagate the find all failure")
  void shouldPropagateFindAllFailure() {
    // Arrange
    when(mongoRepository.findAll()).thenReturn(
            Flux.error(new IllegalStateException("Database unavailable.")));

    // Act
    Flowable<Customer> result = adapter.findAll();

    // Assert
    result.test()
            .assertNoValues()
            .assertError(IllegalStateException.class)
            .assertNotComplete();
  }

  @Test
  @DisplayName("should find a customer by id")
  void shouldFindById() {
    // Arrange
    CustomerDocument entity = document("customer-1");
    Customer domain = customer("customer-1");
    when(mongoRepository.findById("customer-1")).thenReturn(Mono.just(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    // Act
    Maybe<Customer> result = adapter.findById("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(domain)
            .assertComplete();
  }

  @Test
  @DisplayName("should complete empty when the id does not exist")
  void shouldFindByIdReturnEmpty() {
    // Arrange
    when(mongoRepository.findById("missing")).thenReturn(Mono.empty());

    // Act
    Maybe<Customer> result = adapter.findById("missing");

    // Assert
    result.test()
            .assertNoErrors()
            .assertNoValues()
            .assertComplete();
  }

  @Test
  @DisplayName("should find a customer by document number")
  void shouldFindByDocumentNumber() {
    // Arrange
    CustomerDocument entity = document("customer-1");
    Customer domain = customer("customer-1");
    when(mongoRepository.findByDocumentNumber("12345678")).thenReturn(Mono.just(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    // Act
    Maybe<Customer> result = adapter.findByDocumentNumber("12345678");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(domain)
            .assertComplete();
  }

  @Test
  @DisplayName("should complete empty when the document number does not exist")
  void shouldFindByDocumentNumberReturnEmpty() {
    // Arrange
    when(mongoRepository.findByDocumentNumber("12345678")).thenReturn(Mono.empty());

    // Act
    Maybe<Customer> result = adapter.findByDocumentNumber("12345678");

    // Assert
    result.test()
            .assertNoErrors()
            .assertNoValues()
            .assertComplete();
  }

  @Test
  @DisplayName("should report whether a document number exists")
  void shouldCheckExistenceByDocumentNumber() {
    // Arrange
    when(mongoRepository.existsByDocumentNumber("12345678")).thenReturn(Mono.just(true));

    // Act
    Single<Boolean> result = adapter.existsByDocumentNumber("12345678");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(true)
            .assertComplete();
  }

  @Test
  @DisplayName("should delete a customer by id")
  void shouldDeleteById() {
    // Arrange
    when(mongoRepository.deleteById("customer-1")).thenReturn(Mono.empty());

    // Act
    Completable result = adapter.deleteById("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertComplete();
    verify(mongoRepository).deleteById("customer-1");
  }

  @Test
  @DisplayName("should propagate the delete failure")
  void shouldPropagateDeleteFailure() {
    // Arrange
    when(mongoRepository.deleteById("customer-1")).thenReturn(
            Mono.error(new IllegalStateException("Database unavailable.")));

    // Act
    Completable result = adapter.deleteById("customer-1");

    // Assert
    result.test()
            .assertError(error -> error instanceof IllegalStateException && "Database unavailable.".equals(error.getMessage()))
            .assertNotComplete();
  }
}
