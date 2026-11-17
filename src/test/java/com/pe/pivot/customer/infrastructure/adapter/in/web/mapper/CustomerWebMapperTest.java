package com.pe.pivot.customer.infrastructure.adapter.in.web.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerRequest;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CustomerWebMapper}.
 * Validates both mapping directions without starting a Spring context.
 */
class CustomerWebMapperTest {

  private CustomerWebMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CustomerWebMapper();
  }

  @Test
  @DisplayName("Should map a request record to a domain customer")
  void shouldMapRequestToDomain() {
    // Arrange
    CustomerRequest request = CustomerRequest.builder()
            .customerType(CustomerType.PERSONAL)
            .profile(CustomerProfile.STANDARD)
            .documentType(DocumentType.DNI)
            .documentNumber("12345678")
            .firstName("Jane")
            .lastName("Doe")
            .businessName("Pivot SAC")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .address("Lima")
            .status(CustomerStatus.ACTIVE)
            .build();

    // Act
    Customer customer = mapper.toDomain(request);

    // Assert
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
    assertNull(customer.getId());
    assertNull(customer.getCreatedAt());
    assertNull(customer.getUpdatedAt());
  }

  @Test
  @DisplayName("Should map a domain customer to a response record")
  void shouldMapDomainToResponse() {
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
    CustomerResponse response = mapper.toResponse(customer);

    // Assert
    assertEquals("customer-1", response.id());
    assertEquals(CustomerType.BUSINESS, response.customerType());
    assertEquals(CustomerProfile.PYME, response.profile());
    assertEquals(DocumentType.RUC, response.documentType());
    assertEquals("20123456789", response.documentNumber());
    assertEquals("John", response.firstName());
    assertEquals("Smith", response.lastName());
    assertEquals("Pivot SAC", response.businessName());
    assertEquals("john.smith@example.com", response.email());
    assertEquals("999888777", response.phoneNumber());
    assertEquals("Lima", response.address());
    assertEquals(CustomerStatus.ACTIVE, response.status());
    assertEquals(createdAt, response.createdAt());
    assertEquals(updatedAt, response.updatedAt());
  }

  @Test
  @DisplayName("Should map a request with only mandatory fields")
  void shouldMapRequestWithOnlyMandatoryFields() {
    // Arrange
    CustomerRequest request = CustomerRequest.builder()
            .customerType(CustomerType.PERSONAL)
            .documentType(DocumentType.DNI)
            .documentNumber("12345678")
            .email("jane.doe@example.com")
            .phoneNumber("999888777")
            .build();

    // Act
    Customer customer = mapper.toDomain(request);

    // Assert
    assertEquals(CustomerType.PERSONAL, customer.getCustomerType());
    assertEquals("12345678", customer.getDocumentNumber());
    assertNull(customer.getProfile());
    assertNull(customer.getFirstName());
    assertNull(customer.getLastName());
    assertNull(customer.getBusinessName());
    assertNull(customer.getAddress());
    assertNull(customer.getStatus());
  }
}
