package com.pe.pivot.customer.domain.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PersonalCustomerStrategy}.
 * Every reactive assertion subscribes through the TestObserver, so no validation
 * is skipped by a lazy Completable.
 */
class PersonalCustomerStrategyTest {

  private PersonalCustomerStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new PersonalCustomerStrategy();
  }

  private Customer personalCustomer() {
    return Customer.builder()
            .customerType(CustomerType.PERSONAL)
            .documentType(DocumentType.DNI)
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .build();
  }

  @Test
  @DisplayName("should support the PERSONAL customer type")
  void shouldSupportPersonalCustomerType() {
    // Act
    boolean supported = strategy.supports(CustomerType.PERSONAL);

    // Assert
    assertTrue(supported);
  }

  @Test
  @DisplayName("should not support the BUSINESS customer type")
  void shouldNotSupportBusinessCustomerType() {
    // Act
    boolean supported = strategy.supports(CustomerType.BUSINESS);

    // Assert
    assertFalse(supported);
  }

  @Test
  @DisplayName("should validate a personal customer and normalize the document")
  void shouldValidateAndNormalizeDocument() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setDocumentType(DocumentType.PASSPORT);
    customer.setDocumentNumber(" ab1234 ");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertNoErrors()
            .assertComplete();
    assertEquals("AB1234", customer.getDocumentNumber());
  }

  @Test
  @DisplayName("should reject a personal customer registered with a RUC")
  void shouldRejectRucDocumentType() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setDocumentType(DocumentType.RUC);
    customer.setDocumentNumber("20123456789");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "Personal customers cannot be registered with RUC document type.".equals(error.getMessage()))
            .assertNotComplete();
  }

  @Test
  @DisplayName("should reject a personal customer with a blank first name")
  void shouldRejectBlankFirstName() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setFirstName(" ");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "First name and last name are mandatory for PERSONAL customers.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a personal customer with a null last name")
  void shouldRejectNullLastName() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setLastName(null);

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "First name and last name are mandatory for PERSONAL customers.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a personal customer with a null first name")
  void shouldRejectNullFirstName() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setFirstName(null);

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(BusinessRuleException.class)
            .assertError(error -> "First name and last name are mandatory for PERSONAL customers."
                    .equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a personal customer with a blank last name")
  void shouldRejectBlankLastName() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setLastName(" ");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(BusinessRuleException.class)
            .assertError(error -> "First name and last name are mandatory for PERSONAL customers."
                    .equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a personal customer with a PYME profile")
  void shouldRejectPymeProfile() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setProfile(CustomerProfile.PYME);

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "Personal customers cannot have a PYME profile.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a personal customer with an invalid document format")
  void shouldRejectInvalidDocumentFormat() {
    // Arrange
    Customer customer = personalCustomer();
    customer.setDocumentNumber("123");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(BusinessRuleException.class)
            .assertError(error -> error.getMessage().contains("Invalid DNI format"));
  }
}
