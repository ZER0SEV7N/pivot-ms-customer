package com.pe.pivot.customer.infrastructure.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DataMaskingUtil}.
 * Covers null, short, exact boundary, and long document numbers.
 */
class DataMaskingUtilTest {

  @Test
  @DisplayName("should mask a standard document number keeping the last four characters")
  void shouldMaskStandardDocumentNumber() {
    // Arrange
    String documentNumber = "12345678";

    // Act
    String result = DataMaskingUtil.maskDocument(documentNumber);

    // Assert
    assertEquals("****5678", result);
  }

  @Test
  @DisplayName("should mask a long document number keeping the last four characters")
  void shouldMaskLongDocumentNumber() {
    // Arrange
    String documentNumber = "20123456789";

    // Act
    String result = DataMaskingUtil.maskDocument(documentNumber);

    // Assert
    assertEquals("*******6789", result);
    assertEquals(documentNumber.length(), result.length());
  }

  @Test
  @DisplayName("should fully mask a document number with exactly four characters")
  void shouldMaskFourCharacterDocumentNumber() {
    // Arrange
    String documentNumber = "1234";

    // Act
    String result = DataMaskingUtil.maskDocument(documentNumber);

    // Assert
    assertEquals("****", result);
  }

  @Test
  @DisplayName("should fully mask a short document number")
  void shouldMaskShortDocumentNumber() {
    // Arrange
    String documentNumber = "123";

    // Act
    String result = DataMaskingUtil.maskDocument(documentNumber);

    // Assert
    assertEquals("****", result);
  }

  @Test
  @DisplayName("should fully mask a null document number")
  void shouldMaskNullDocumentNumber() {
    // Act
    String result = DataMaskingUtil.maskDocument(null);

    // Assert
    assertEquals("****", result);
  }
}
