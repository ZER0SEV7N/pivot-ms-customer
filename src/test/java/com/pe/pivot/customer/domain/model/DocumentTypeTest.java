package com.pe.pivot.customer.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the rich enumeration {@link DocumentType}.
 * Covers normalization, valid formats, and every invalid format branch.
 */
class DocumentTypeTest {

  @Test
  @DisplayName("should validate and trim a DNI document number")
  void shouldValidateAndTrimDni() {
    // Arrange
    String documentNumber = " 12345678 ";

    // Act
    String result = DocumentType.DNI.validateAndNormalize(documentNumber);

    // Assert
    assertEquals("12345678", result);
  }

  @Test
  @DisplayName("should validate a CE document number")
  void shouldValidateCe() {
    // Arrange
    String documentNumber = "123456789";

    // Act
    String result = DocumentType.CE.validateAndNormalize(documentNumber);

    // Assert
    assertEquals("123456789", result);
  }

  @Test
  @DisplayName("should validate and uppercase a passport number")
  void shouldValidateAndUppercasePassport() {
    // Arrange
    String documentNumber = "ab1234";

    // Act
    String result = DocumentType.PASSPORT.validateAndNormalize(documentNumber);

    // Assert
    assertEquals("AB1234", result);
  }

  @Test
  @DisplayName("should validate a RUC document number")
  void shouldValidateRuc() {
    // Arrange
    String documentNumber = "20123456789";

    // Act
    String result = DocumentType.RUC.validateAndNormalize(documentNumber);

    // Assert
    assertEquals("20123456789", result);
  }

  @Test
  @DisplayName("should reject an invalid DNI format")
  void shouldRejectInvalidDni() {
    // Arrange
    String documentNumber = "1234";

    // Act
    BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> DocumentType.DNI.validateAndNormalize(documentNumber));

    // Assert
    assertTrue(exception.getMessage().contains("Invalid DNI format"));
  }

  @Test
  @DisplayName("should reject an invalid CE format")
  void shouldRejectInvalidCe() {
    // Arrange
    String documentNumber = "12345678";

    // Act
    BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> DocumentType.CE.validateAndNormalize(documentNumber));

    // Assert
    assertTrue(exception.getMessage().contains("Invalid CE format"));
  }

  @Test
  @DisplayName("should reject an invalid passport format")
  void shouldRejectInvalidPassport() {
    // Arrange
    String documentNumber = "ab12";

    // Act
    BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> DocumentType.PASSPORT.validateAndNormalize(documentNumber));

    // Assert
    assertTrue(exception.getMessage().contains("Invalid PASSPORT format"));
  }

  @Test
  @DisplayName("should reject an invalid RUC format")
  void shouldRejectInvalidRuc() {
    // Arrange
    String documentNumber = "2012345678";

    // Act
    BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> DocumentType.RUC.validateAndNormalize(documentNumber));

    // Assert
    assertTrue(exception.getMessage().contains("Invalid RUC format"));
  }

  @Test
  @DisplayName("should reject a null document number")
  void shouldRejectNullDocumentNumber() {
    // Act
    BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> DocumentType.DNI.validateAndNormalize(null));

    // Assert
    assertTrue(exception.getMessage().contains("Invalid DNI format"));
  }

  @Test
  @DisplayName("should expose all supported document types")
  void shouldExposeAllDocumentTypes() {
    // Arrange
    int expectedTypes = 4;

    // Act
    DocumentType[] documentTypes = DocumentType.values();

    // Assert
    assertEquals(expectedTypes, documentTypes.length);
    assertEquals(DocumentType.RUC, DocumentType.valueOf("RUC"));
  }
}
