package com.pe.pivot.customer.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.exception.DuplicateCustomerException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import com.pe.pivot.customer.domain.strategy.CustomerValidationStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CreateCustomerUseCaseImpl}.
 * Each test follows the Arrange-Act-Assert pattern and validates the reactive stream
 * through the RxJava 3 TestObserver to avoid false positives.
 */
@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseImplTest {

  @Mock
  private CustomerRepositoryPort repositoryPort;

  @Mock
  private CustomerValidationStrategy strategy;

  private CreateCustomerUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreateCustomerUseCaseImpl(repositoryPort, List.of(strategy));
  }

  private Customer personalCustomer() {
    return Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .documentType(DocumentType.DNI)
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .build();
  }

  @Test
  @DisplayName("should create a customer applying default profile and status")
  void shouldCreateCustomerApplyingDefaults() {
    // Arrange
    Customer customer = personalCustomer();
    Customer saved = personalCustomer();
    saved.setId("customer-1");
    saved.setProfile(CustomerProfile.STANDARD);
    saved.setStatus(CustomerStatus.ACTIVE);

    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(customer)).thenReturn(Completable.complete());
    when(repositoryPort.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));
    when(repositoryPort.save(customer)).thenReturn(Single.just(saved));

    // Act
    Single<Customer> result = useCase.create(customer);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(saved)
            .assertComplete();
    assertEquals(CustomerProfile.STANDARD, customer.getProfile());
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertNotNull(customer.getCreatedAt());
    assertEquals(customer.getCreatedAt(), customer.getUpdatedAt());
    verify(repositoryPort).save(customer);
  }

  @Test
  @DisplayName("should preserve the profile and status supplied by the caller")
  void shouldPreserveProvidedProfileAndStatus() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setProfile(CustomerProfile.VIP);
    customer.setStatus(CustomerStatus.BLOCKED);

    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(customer)).thenReturn(Completable.complete());
    when(repositoryPort.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));
    when(repositoryPort.save(customer)).thenReturn(Single.just(customer));

    // Act
    Single<Customer> result = useCase.create(customer);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(customer)
            .assertComplete();
    assertEquals(CustomerProfile.VIP, customer.getProfile());
    assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
  }

  @Test
  @DisplayName("should reject a customer whose document number already exists")
  void shouldRejectDuplicateDocumentNumber() {
    // Arrange
    Customer customer = personalCustomer();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(customer)).thenReturn(Completable.complete());
    when(repositoryPort.existsByDocumentNumber("12345678")).thenReturn(Single.just(true));

    // Act
    Single<Customer> result = useCase.create(customer);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(DuplicateCustomerException.class)
            .assertNotComplete();
    verify(repositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should reject an unsupported customer type")
  void shouldRejectUnsupportedCustomerType() {
    // Arrange
    Customer customer = personalCustomer();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(false);
    when(repositoryPort.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));

    // Act
    Single<Customer> result = useCase.create(customer);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof BusinessRuleException
                    && "Unsupported customer type: PERSONAL".equals(error.getMessage()));
    verify(strategy, never()).validate(any());
    verify(repositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should propagate the validation strategy failure")
  void shouldPropagateValidationFailure() {
    // Arrange
    Customer customer = personalCustomer();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(customer)).thenReturn(
            Completable.error(new BusinessRuleException("Invalid personal data.")));
    when(repositoryPort.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));

    // Act
    Single<Customer> result = useCase.create(customer);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof BusinessRuleException && "Invalid personal data.".equals(error.getMessage()));
    verify(repositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should propagate the persistence failure")
  void shouldPropagatePersistenceFailure() {
    // Arrange
    Customer customer = personalCustomer();
    when(strategy.supports(CustomerType.PERSONAL)).thenReturn(true);
    when(strategy.validate(customer)).thenReturn(Completable.complete());
    when(repositoryPort.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));
    when(repositoryPort.save(customer)).thenReturn(
            Single.error(new IllegalStateException("Database unavailable.")));

    // Act
    Single<Customer> result = useCase.create(customer);

    // Assert
    result.test()
            .assertValueCount(0)
            .assertError(error -> error instanceof IllegalStateException && "Database unavailable.".equals(error.getMessage()));
  }
}
