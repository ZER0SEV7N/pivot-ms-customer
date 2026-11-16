package com.pe.pivot.customer.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the customer enumerations.
 * Guarantees that every constant and lookup remains available.
 */
class CustomerEnumsTest {

  @Test
  @DisplayName("should expose all customer types")
  void shouldExposeAllCustomerTypes() {
    // Arrange
    int expectedTypes = 2;

    // Act
    CustomerType[] customerTypes = CustomerType.values();

    // Assert
    assertEquals(expectedTypes, customerTypes.length);
    assertEquals(CustomerType.BUSINESS, CustomerType.valueOf("BUSINESS"));
  }

  @Test
  @DisplayName("should expose all customer statuses")
  void shouldExposeAllCustomerStatuses() {
    // Arrange
    int expectedStatuses = 3;

    // Act
    CustomerStatus[] customerStatuses = CustomerStatus.values();

    // Assert
    assertEquals(expectedStatuses, customerStatuses.length);
    assertEquals(CustomerStatus.INACTIVE, CustomerStatus.valueOf("INACTIVE"));
  }

  @Test
  @DisplayName("should expose all customer profiles")
  void shouldExposeAllCustomerProfiles() {
    // Arrange
    int expectedProfiles = 3;

    // Act
    CustomerProfile[] customerProfiles = CustomerProfile.values();

    // Assert
    assertEquals(expectedProfiles, customerProfiles.length);
    assertEquals(CustomerProfile.VIP, CustomerProfile.valueOf("VIP"));
  }
}
