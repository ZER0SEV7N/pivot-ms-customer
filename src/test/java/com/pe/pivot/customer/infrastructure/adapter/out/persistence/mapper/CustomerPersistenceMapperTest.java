package com.pe.pivot.customer.infrastructure.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import com.pe.pivot.customer.infrastructure.adapter.out.persistence.entity.CustomerDocument;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CustomerPersistenceMapper}.
 * Covers the non-null and null conversion branches in both directions.
 */
class CustomerPersistenceMapperTest {

  private CustomerPersistenceMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CustomerPersistenceMapper();
  }

  @Test
  @DisplayName("should map a domain customer to a document")
  void shouldMapDomainToDocument() {
    // Arrange
    Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2025-02-01T00:00:00Z");
    Customer customer = Customer.builder()
            .id("customer-1")
            .customerType(CustomerType.BUSINESS)
            .profile(CustomerProfile.PYME)
            .documentType(DocumentType.RUC)
            .documentNumber("20123456789")
            .firstName("John")
            .lastName("Smith")
            .businessName("Pivot SAC")
            .email("john.smith@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .status(CustomerStatus.ACTIVE)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

    // Act
    CustomerDocument document = mapper.toDocument(customer);

    // Assert
    assertEquals("customer-1", document.getId());
    assertEquals("BUSINESS", document.getCustomerType());
    assertEquals("PYME", document.getProfile());
    assertEquals("RUC", document.getDocumentType());
    assertEquals("20123456789", document.getDocumentNumber());
    assertEquals("John", document.getFirstName());
    assertEquals("Smith", document.getLastName());
    assertEquals("Pivot SAC", document.getBusinessName());
    assertEquals("john.smith@example.com", document.getEmail());
    assertEquals("999888777", document.getPhoneNumber());
    assertEquals("Lima", document.getAddress());
    assertEquals("ACTIVE", document.getStatus());
    assertEquals(createdAt, document.getCreatedAt());
    assertEquals(updatedAt, document.getUpdatedAt());
  }

  @Test
  @DisplayName("should map a domain customer with null enums to a document")
  void shouldMapDomainWithNullEnumsToDocument() {
    // Arrange
    Customer customer = Customer.builder()
            .documentNumber("12345678")
            .build();

    // Act
    CustomerDocument document = mapper.toDocument(customer);

    // Assert
    assertNull(document.getCustomerType());
    assertNull(document.getProfile());
    assertNull(document.getDocumentType());
    assertNull(document.getStatus());
    assertEquals("12345678", document.getDocumentNumber());
  }

  @Test
  @DisplayName("should map a document to a domain customer")
  void shouldMapDocumentToDomain() {
    // Arrange
    Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2025-02-01T00:00:00Z");
    CustomerDocument document = CustomerDocument.builder()
            .id("customer-1")
            .customerType("PERSONAL")
            .profile("STANDARD")
            .documentType("DNI")
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .businessName("Pivot SAC")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .status("ACTIVE")
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

    // Act
    Customer customer = mapper.toDomain(document);

    // Assert
    assertEquals("customer-1", customer.getId());
    assertEquals(CustomerType.PERSONAL, customer.getCustomerType());
    assertEquals(CustomerProfile.STANDARD, customer.getProfile());
    assertEquals(DocumentType.DNI, customer.getDocumentType());
    assertEquals("12345678", customer.getDocumentNumber());
    assertEquals("Jane", customer.getFirstName());
    assertEquals("Doe", customer.getLastName());
    assertEquals("Pivot SAC", customer.getBusinessName());
    assertEquals("jane.doe@example.com", customer.getEmail());
    assertEquals("999888777", customer.getPhoneNumber());
    assertEquals("Lima", customer.getAddress());
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    assertEquals(createdAt, customer.getCreatedAt());
    assertEquals(updatedAt, customer.getUpdatedAt());
  }

  @Test
  @DisplayName("should map a document with null enums to a domain customer")
  void shouldMapDocumentWithNullEnumsToDomain() {
    // Arrange
    CustomerDocument document = CustomerDocument.builder()
            .id("customer-1")
            .documentNumber("12345678")
            .build();

    // Act
    Customer customer = mapper.toDomain(document);

    // Assert
    assertNull(customer.getCustomerType());
    assertNull(customer.getProfile());
    assertNull(customer.getDocumentType());
    assertNull(customer.getStatus());
    assertEquals("customer-1", customer.getId());
    assertEquals("12345678", customer.getDocumentNumber());
  }
}
