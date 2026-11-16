package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.DuplicateCustomerException;
import com.pe.pivot.customer.domain.model.*;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for CreateCustomerUseCaseImpl.
 * Validates customer registration, uniqueness verification, and reactive error handling using AAA pattern.
 */
class CreateCustomerUseCaseTest {

  @Mock
  private CustomerRepositoryPort customerRepository;

  @InjectMocks
  private CreateCustomerUseCaseImpl createCustomerUseCase;

  private Customer sampleCustomer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    sampleCustomer = Customer.builder()
            .id("65f1a2b3c4d5e6f7a8b9c0d1")
            .customerType(CustomerType.PERSONAL)
            .profile(CustomerProfile.VIP)
            .documentType(DocumentType.DNI)
            .documentNumber("44556677")
            .firstName("Daniel")
            .lastName("Singer")
            .email("danielsinger07@hotmail.com")
            .status(CustomerStatus.ACTIVE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }

  @Test
  @DisplayName("Should successfully create a customer when document number does not exist")
  void shouldCreateCustomerSuccessfully() {
    // Arrange
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Single.just(false));
    when(customerRepository.save(any(Customer.class))).thenReturn(Single.just(sampleCustomer));

    // Act & Assert
    createCustomerUseCase.create(sampleCustomer)
            .test()
            .assertNoErrors()
            .assertValue(saved -> saved.getDocumentNumber().equals("44556677"))
            .assertComplete();
  }

  @Test
  @DisplayName("Should throw DuplicateCustomerException when document number already exists")
  void shouldFailWhenCustomerDocumentAlreadyExists() {
    // Arrange
    when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Single.just(true));

    // Act & Assert
    createCustomerUseCase.create(sampleCustomer)
            .test()
            .assertError(DuplicateCustomerException.class)
            .assertNotComplete();
  }
}