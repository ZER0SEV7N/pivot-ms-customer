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
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import com.pe.pivot.customer.domain.strategy.CustomerValidationStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link UpdateCustomerUseCaseImpl}.
 * Verifies full and partial updates, strategy selection, and every error path.
 */
@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseImplTest {

  @Mock
  private CustomerRepositoryPort repositoryPort;

  @Mock
  private CustomerValidationStrategy strategy;

  @Mock
  private FindCustomerByIdUseCase findCustomerByIdUseCase;

  private UpdateCustomerUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new UpdateCustomerUseCaseImpl(repositoryPort, List.of(strategy),
            findCustomerByIdUseCase);
  }

  private Customer existingCustomer() {
    return Customer.builder()
            .id("customer-1")
            .customerType(CustomerType.BUSINESS)
            .profile(CustomerProfile.STANDARD)
            .documentType(DocumentType.DNI)
            .documentNumber("11111111")
            .firstName("Old")
            .lastName("Name")
            .email("old@example.com")
            .phoneNumber("1111111")
            .address("Old address")
            .status(CustomerStatus.ACTIVE)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();
  }

  @Test
  @DisplayName("should update every non-null field of an existing customer")
  void shouldUpdateEveryNonNullField() {
    // Arrange
    Customer existing = existingCustomer();
    Customer incoming = Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .profile(CustomerProfile.VIP)
            .documentType(DocumentType.PASSPORT)
            .documentNumber("AB12345")
            .firstName("Jane")
            .lastName("Doe")
            .businessName("Pivot SAC")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .status(CustomerStatus.BLOCKED)
            .build();

    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(incoming)).thenReturn(Completable.complete());
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(existing));
    when(repositoryPort.save(existing)).thenReturn(Single.just(existing));

    // Act
    Single<Customer> result = useCase.update("customer-1", incoming);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(existing)
            .assertComplete();
    assertEquals(CustomerType.PERSONAL, existing.getCustomerType());
    assertEquals(CustomerProfile.VIP, existing.getProfile());
    assertEquals(DocumentType.PASSPORT, existing.getDocumentType());
    assertEquals("AB12345", existing.getDocumentNumber());
    assertEquals("Jane", existing.getFirstName());
    assertEquals("Doe", existing.getLastName());
    assertEquals("Pivot SAC", existing.getBusinessName());
    assertEquals("jane.doe@example.com", existing.getEmail());
    assertEquals("999888777", existing.getPhoneNumber());
    assertEquals("Lima", existing.getAddress());
    assertEquals(CustomerStatus.BLOCKED, existing.getStatus());
    assertNotNull(existing.getUpdatedAt());
    verify(repositoryPort).save(existing);
  }

  @Test
  @DisplayName("should keep the existing fields when the request sends null values")
  void shouldKeepFieldsWhenIncomingValuesAreNull() {
    // Arrange
    Customer existing = existingCustomer();
    Customer incoming = Customer.builder()
            .firstName("Updated")
            .email("updated@example.com")
            .build();

    when(strategy.supports(incoming.getCustomerType())).thenReturn(true);
    when(strategy.validate(incoming)).thenReturn(Completable.complete());
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(existing));
    when(repositoryPort.save(existing)).thenReturn(Single.just(existing));

    // Act
    Single<Customer> result = useCase.update("customer-1", incoming);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(existing)
            .assertComplete();
    assertEquals("Updated", existing.getFirstName());
    assertEquals("updated@example.com", existing.getEmail());
    assertEquals(CustomerType.BUSINESS, existing.getCustomerType());
    assertEquals(CustomerProfile.STANDARD, existing.getProfile());
    assertEquals(DocumentType.DNI, existing.getDocumentType());
    assertEquals("11111111", existing.getDocumentNumber());
    assertEquals("Name", existing.getLastName());
    assertEquals("1111111", existing.getPhoneNumber());
    assertEquals("Old address", existing.getAddress());
    assertEquals(CustomerStatus.ACTIVE, existing.getStatus());
  }

  @Test
  @DisplayName("should reject an unsupported customer type")
  void shouldRejectUnsupportedCustomerType() {
    // Arrange
    Customer incoming = Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .build();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(false);
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.never());

    // Act
    Single<Customer> result = useCase.update("customer-1", incoming);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof BusinessRuleException
                    && "Unsupported customer type: PERSONAL".equals(error.getMessage()));
    verify(repositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should propagate the validation strategy failure")
  void shouldPropagateValidationFailure() {
    // Arrange
    Customer incoming = Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .build();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(incoming)).thenReturn(
            Completable.error(new BusinessRuleException("Invalid personal data.")));
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.never());

    // Act
    Single<Customer> result = useCase.update("customer-1", incoming);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof BusinessRuleException && "Invalid personal data.".equals(error.getMessage()));
    verify(repositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should propagate the not found error")
  void shouldPropagateNotFoundError() {
    // Arrange
    Customer incoming = Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .firstName("Jane")
            .build();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(incoming)).thenReturn(Completable.complete());
    when(findCustomerByIdUseCase.findById("missing")).thenReturn(
            Single.error(new CustomerNotFoundException("Customer not found with ID: missing")));

    // Act
    Single<Customer> result = useCase.update("missing", incoming);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof CustomerNotFoundException && "Customer not found with ID: missing".equals(error.getMessage()));
    verifyNoInteractions(repositoryPort);
  }

  @Test
  @DisplayName("should propagate the persistence failure")
  void shouldPropagatePersistenceFailure() {
    // Arrange
    Customer existing = existingCustomer();
    Customer incoming = Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .firstName("Jane")
            .build();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(incoming)).thenReturn(Completable.complete());
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.just(existing));
    when(repositoryPort.save(existing)).thenReturn(
            Single.error(new IllegalStateException("Database unavailable.")));

    // Act
    Single<Customer> result = useCase.update("customer-1", incoming);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof IllegalStateException && "Database unavailable.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should validate before looking up the customer")
  void shouldValidateBeforeLookingUp() {
    // Arrange
    Customer incoming = Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .build();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(incoming)).thenReturn(Completable.complete());
    when(findCustomerByIdUseCase.findById("customer-1")).thenReturn(Single.never());

    // Act
    Single<Customer> result = useCase.update("customer-1", incoming);

    // Assert
    result.test()
            .assertNoValues()
            .assertNotComplete()
            .assertNoErrors();
    verify(strategy).validate(incoming);
    verify(findCustomerByIdUseCase).findById("customer-1");
    verifyNoInteractions(repositoryPort);
  }
}
