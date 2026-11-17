package com.pe.pivot.customer.infrastructure.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import com.pe.pivot.customer.domain.port.in.CreateCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.DeleteCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.ReactivateCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.RetrieveCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.UpdateCustomerUseCase;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerRequest;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerResponse;
import com.pe.pivot.customer.infrastructure.adapter.in.web.mapper.CustomerWebMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link CustomerController}.
 * The use cases are mocked and the real mapper is used to verify the complete
 * web-to-domain-to-web transformation.
 */
@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

  @Mock
  private CreateCustomerUseCase createCustomerUseCase;

  @Mock
  private RetrieveCustomerUseCase retrieveCustomerUseCase;

  @Mock
  private UpdateCustomerUseCase updateCustomerUseCase;

  @Mock
  private DeleteCustomerUseCase deleteCustomerUseCase;

  @Mock
  private ReactivateCustomerUseCase reactivateCustomerUseCase;

  private CustomerController controller;

  @BeforeEach
  void setUp() {
    controller = new CustomerController(createCustomerUseCase, retrieveCustomerUseCase,
            updateCustomerUseCase, deleteCustomerUseCase, reactivateCustomerUseCase,
            new CustomerWebMapper());
  }

  private Customer customer() {
    return Customer.builder()
            .id("customer-1")
            .customerType(CustomerType.PERSONAL)
            .profile(CustomerProfile.STANDARD)
            .documentType(DocumentType.DNI)
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .status(CustomerStatus.ACTIVE)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();
  }

  private CustomerRequest request() {
    return CustomerRequest.builder()
            .customerType(CustomerType.PERSONAL)
            .profile(CustomerProfile.STANDARD)
            .documentType(DocumentType.DNI)
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .status(CustomerStatus.ACTIVE)
            .build();
  }

  @Test
  @DisplayName("Should create a customer and return the mapped response")
  void shouldCreateCustomer() {
    // Arrange
    Customer customer = customer();
    when(createCustomerUseCase.create(any(Customer.class))).thenReturn(Single.just(customer));

    // Act
    Single<CustomerResponse> result = controller.createCustomer(request());

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals("customer-1", response.id());
              assertEquals("12345678", response.documentNumber());
              assertEquals("jane.doe@example.com", response.email());
              return true;
            })
            .assertComplete();
    verify(createCustomerUseCase).create(argThat(domain ->
            CustomerType.PERSONAL == domain.getCustomerType()
                    && "12345678".equals(domain.getDocumentNumber())));
  }

  @Test
  @DisplayName("Should propagate the creation error")
  void shouldPropagateCreationError() {
    // Arrange
    when(createCustomerUseCase.create(any(Customer.class))).thenReturn(
            Single.error(new BusinessRuleException("Invalid customer.")));

    // Act
    Single<CustomerResponse> result = controller.createCustomer(request());

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof BusinessRuleException
                    && "Invalid customer."
                            .equals(error.getMessage()));
  }

  @Test
  @DisplayName("Should retrieve all customers mapped to responses")
  void shouldRetrieveAllCustomers() {
    // Arrange
    Customer first = customer();
    Customer second = customer();
    second.setId("customer-2");
    CustomerWebMapper mapper = new CustomerWebMapper();
    when(retrieveCustomerUseCase.findAll()).thenReturn(Flowable.just(first, second));

    // Act
    Flowable<CustomerResponse> result = controller.getAllCustomers();

    // Assert
    result.test()
            .assertNoErrors()
            .assertValues(mapper.toResponse(first), mapper.toResponse(second))
            .assertValueCount(2)
            .assertComplete();
  }

  @Test
  @DisplayName("Should retrieve a customer by id")
  void shouldRetrieveCustomerById() {
    // Arrange
    Customer customer = customer();
    CustomerResponse expected = new CustomerWebMapper().toResponse(customer);
    when(retrieveCustomerUseCase.findById("customer-1")).thenReturn(Single.just(customer));

    // Act
    Single<ResponseEntity<CustomerResponse>> result = controller.getCustomerById("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertEquals(expected, response.getBody());
              return true;
            })
            .assertComplete();
  }

  @Test
  @DisplayName("Should propagate the not found error when retrieving by id")
  void shouldPropagateNotFoundById() {
    // Arrange
    when(retrieveCustomerUseCase.findById("missing")).thenReturn(
            Single.error(new com.pe.pivot.customer.domain.exception.CustomerNotFoundException(
                    "Customer not found with ID: missing")));

    // Act
    Single<ResponseEntity<CustomerResponse>> result = controller.getCustomerById("missing");

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof CustomerNotFoundException
                    && "Customer not found with ID: missing".equals(error.getMessage()));
  }

  @Test
  @DisplayName("Should retrieve a customer by document number")
  void shouldRetrieveCustomerByDocumentNumber() {
    // Arrange
    Customer customer = customer();
    CustomerResponse expected = new CustomerWebMapper().toResponse(customer);
    when(retrieveCustomerUseCase.findByDocumentNumber("12345678"))
            .thenReturn(Single.just(customer));

    // Act
    Single<ResponseEntity<CustomerResponse>> result =
            controller.getCustomerByDocumentNumber("12345678");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertEquals(expected, response.getBody());
              return true;
            })
            .assertComplete();
  }

  @Test
  @DisplayName("Should update a customer and return the mapped response")
  void shouldUpdateCustomer() {
    // Arrange
    Customer updated = customer();
    updated.setFirstName("Updated");
    when(updateCustomerUseCase.update(eq("customer-1"), any(Customer.class)))
            .thenReturn(Single.just(updated));

    // Act
    Single<ResponseEntity<CustomerResponse>> result =
            controller.updateCustomer("customer-1", request());

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertEquals("Updated", response.getBody().firstName());
              return true;
            })
            .assertComplete();
    verify(updateCustomerUseCase).update(eq("customer-1"),
            argThat(domain -> "12345678".equals(domain.getDocumentNumber())));
  }

  @Test
  @DisplayName("Should delete a customer and complete")
  void shouldDeleteCustomer() {
    // Arrange
    when(deleteCustomerUseCase.delete("customer-1")).thenReturn(Completable.complete());

    // Act
    Completable result = controller.deleteCustomer("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertComplete();
    verify(deleteCustomerUseCase).delete("customer-1");
  }

  @Test
  @DisplayName("Should propagate the delete error")
  void shouldPropagateDeleteError() {
    // Arrange
    when(deleteCustomerUseCase.delete("customer-1")).thenReturn(
            Completable.error(new BusinessRuleException("Customer is already INACTIVE.")));

    // Act
    Completable result = controller.deleteCustomer("customer-1");

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException
                    && "Customer is already INACTIVE."
                            .equals(error.getMessage()));
  }

  @Test
  @DisplayName("Should reactivate a customer and return the mapped response")
  void shouldReactivateCustomer() {
    // Arrange
    Customer reactivated = customer();
    CustomerResponse expected = new CustomerWebMapper().toResponse(reactivated);
    when(reactivateCustomerUseCase.reactivate("customer-1")).thenReturn(
            Single.just(reactivated));

    // Act
    Single<ResponseEntity<CustomerResponse>> result =
            controller.reactivateCustomer("customer-1");

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertEquals(expected, response.getBody());
              return true;
            })
            .assertComplete();
  }
}
