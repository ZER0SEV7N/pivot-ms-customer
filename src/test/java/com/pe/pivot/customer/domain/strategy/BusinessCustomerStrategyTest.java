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
 * Unit tests for {@link BusinessCustomerStrategy}.
 * The Completable is always subscribed to guarantee that the validation action runs.
 */
class BusinessCustomerStrategyTest {

  private BusinessCustomerStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new BusinessCustomerStrategy();
  }

  private Customer businessCustomer() {
    return Customer.builder()
            .customerType(CustomerType.BUSINESS)
            .documentType(DocumentType.RUC)
            .documentNumber("20123456789")
            .businessName("Pivot SAC")
            .build();
  }

  @Test
  @DisplayName("should support the BUSINESS customer type")
  void shouldSupportBusinessCustomerType() {
    // Act
    boolean supported = strategy.supports(CustomerType.BUSINESS);

    // Assert
    assertTrue(supported);
  }

  @Test
  @DisplayName("should not support the PERSONAL customer type")
  void shouldNotSupportPersonalCustomerType() {
    // Act
    boolean supported = strategy.supports(CustomerType.PERSONAL);

    // Assert
    assertFalse(supported);
  }

  @Test
  @DisplayName("should validate a business customer and normalize the document")
  void shouldValidateAndNormalizeDocument() {
    // Arrange
    Customer customer = businessCustomer();
    customer.setDocumentNumber(" 20123456789 ");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertNoErrors()
            .assertComplete();
    assertEquals("20123456789", customer.getDocumentNumber());
  }

  @Test
  @DisplayName("should reject a business customer registered without a RUC")
  void shouldRejectNonRucDocumentType() {
    // Arrange
    Customer customer = businessCustomer();
    customer.setDocumentType(DocumentType.DNI);
    customer.setDocumentNumber("12345678");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "Business customers must be registered with a RUC document type.".equals(error.getMessage()))
            .assertNotComplete();
  }

  @Test
  @DisplayName("should reject a business customer with a null business name")
  void shouldRejectNullBusinessName() {
    // Arrange
    Customer customer = businessCustomer();
    customer.setBusinessName(null);

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "Business name (Razon Social) is mandatory for BUSINESS customers.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a business customer with a blank business name")
  void shouldRejectBlankBusinessName() {
    // Arrange
    Customer customer = businessCustomer();
    customer.setBusinessName(" ");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "Business name (Razon Social) is mandatory for BUSINESS customers.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a business customer with a VIP profile")
  void shouldRejectVipProfile() {
    // Arrange
    Customer customer = businessCustomer();
    customer.setProfile(CustomerProfile.VIP);

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(error -> error instanceof BusinessRuleException && "Business customers cannot have a VIP profile.".equals(error.getMessage()));
  }

  @Test
  @DisplayName("should reject a business customer with an invalid RUC format")
  void shouldRejectInvalidRucFormat() {
    // Arrange
    Customer customer = businessCustomer();
    customer.setDocumentNumber("201234");

    // Act
    Completable result = strategy.validate(customer);

    // Assert
    result.test()
            .assertError(BusinessRuleException.class)
            .assertError(error -> error.getMessage().contains("Invalid RUC format"));
  }
}
