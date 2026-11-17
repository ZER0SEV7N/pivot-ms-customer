package com.pe.pivot.customer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Smoke test for MsCustomerApplication class definition.
 */
class MsCustomerApplicationTests {

  @Test
  @DisplayName("Should instantiate MsCustomerApplication successfully")
  void shouldInstantiateApplicationSuccessfully() {
    // Arrange & Act
    MsCustomerApplication application = new MsCustomerApplication();

    // Assert
    assertNotNull(application);
  }
}
