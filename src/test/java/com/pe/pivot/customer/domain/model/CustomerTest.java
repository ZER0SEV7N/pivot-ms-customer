package com.pe.pivot.customer.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Customer} domain model.
 * Covers both constructors, the builder, and every accessor.
 */
class CustomerTest {

  private static final Instant CREATED_AT = Instant.parse("2025-01-01T00:00:00Z");
  private static final Instant UPDATED_AT = Instant.parse("2025-02-01T00:00:00Z");

  @Test
  @DisplayName("should build a customer with all fields")
  void shouldBuildCustomerWithAllFields() {
    // Arrange
    // Act
    Customer customer = Customer.builder()
            .id("customer-1")
            .customerType(CustomerType.PERSONAL)
            .profile(CustomerProfile.VIP)
            .documentType(DocumentType.DNI)
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .businessName("Pivot SAC")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .status(CustomerStatus.ACTIVE)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .build();

    // Assert
    assertEquals("customer-1", customer.getId());
    assertEquals(CustomerType.PERSONAL, customer.getCustomerType());
    assertEquals(CustomerProfile.VIP, customer.getProfile());
    assertEquals(DocumentType.DNI, customer.getDocumentType());
    assertEquals("12345678", customer.getDocumentNumber());
    assertEquals("Jane", customer.getFirstName());
    assertEquals("Doe", customer.getLastName());
    assertEquals("Pivot SAC", customer.getBusinessName());
    assertEquals("jane.doe@example.com", customer.getEmail());
    assertEquals("999888777", customer.getPhoneNumber());
    assertEquals("Lima", customer.getAddress());
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertEquals(CREATED_AT, customer.getCreatedAt());
    assertEquals(UPDATED_AT, customer.getUpdatedAt());
  }

  @Test
  @DisplayName("should set and get every field through the no-args constructor")
  void shouldSetAndGetEveryField() {
    // Arrange
    Customer customer = new Customer();

    // Act
    customer.setId("customer-1");
    customer.setCustomerType(CustomerType.BUSINESS);
    customer.setProfile(CustomerProfile.PYME);
    customer.setDocumentType(DocumentType.RUC);
    customer.setDocumentNumber("20123456789");
    customer.setFirstName("John");
    customer.setLastName("Smith");
    customer.setBusinessName("Pivot SAC");
    customer.setEmail("john.smith@example.com");
    customer.setPhoneNumber("999888777");
    customer.setAddress("Lima");
    customer.setStatus(CustomerStatus.BLOCKED);
    customer.setCreatedAt(CREATED_AT);
    customer.setUpdatedAt(UPDATED_AT);

    // Assert
    assertEquals("customer-1", customer.getId());
    assertEquals(CustomerType.BUSINESS, customer.getCustomerType());
    assertEquals(CustomerProfile.PYME, customer.getProfile());
    assertEquals(DocumentType.RUC, customer.getDocumentType());
    assertEquals("20123456789", customer.getDocumentNumber());
    assertEquals("John", customer.getFirstName());
    assertEquals("Smith", customer.getLastName());
    assertEquals("Pivot SAC", customer.getBusinessName());
    assertEquals("john.smith@example.com", customer.getEmail());
    assertEquals("999888777", customer.getPhoneNumber());
    assertEquals("Lima", customer.getAddress());
    assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
    assertEquals(CREATED_AT, customer.getCreatedAt());
    assertEquals(UPDATED_AT, customer.getUpdatedAt());
  }

  @Test
  @DisplayName("should create a customer through the all-args constructor")
  void shouldCreateCustomerWithAllArgsConstructor() {
    // Arrange
    // Act
    Customer customer = new Customer("customer-1", CustomerType.PERSONAL,
            CustomerProfile.STANDARD, DocumentType.DNI, "12345678", "Jane", "Doe", null,
            "jane.doe@example.com", "999888777", "Lima", CustomerStatus.ACTIVE,
            CREATED_AT, UPDATED_AT);

    // Assert
    assertEquals("customer-1", customer.getId());
    assertEquals(CustomerType.PERSONAL, customer.getCustomerType());
    assertEquals(CustomerProfile.STANDARD, customer.getProfile());
    assertEquals(DocumentType.DNI, customer.getDocumentType());
    assertEquals("12345678", customer.getDocumentNumber());
    assertEquals("Jane", customer.getFirstName());
    assertEquals("Doe", customer.getLastName());
    assertNull(customer.getBusinessName());
    assertEquals("jane.doe@example.com", customer.getEmail());
    assertEquals("999888777", customer.getPhoneNumber());
    assertEquals("Lima", customer.getAddress());
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertEquals(CREATED_AT, customer.getCreatedAt());
    assertEquals(UPDATED_AT, customer.getUpdatedAt());
  }
}
