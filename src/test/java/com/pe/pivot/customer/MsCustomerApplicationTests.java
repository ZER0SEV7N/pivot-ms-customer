package com.pe.pivot.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MsCustomerApplicationTests {

  @Test
  @DisplayName("should load the application context")
  void shouldLoadApplicationContext() {
    // Arrange
    // The Spring context is created by the test framework before this method runs.

    // Act
    // No explicit action is required to validate the context startup.

    // Assert
    // Reaching this point proves that every bean was wired successfully.
  }
}
