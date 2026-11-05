package com.pe.pivot.customer.domain.model;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;

import java.util.regex.Pattern;

/**
 * Rich Enumeration representing official identification document types
 * with pre-compiled regex validation patterns and normalization.
 */
public enum DocumentType {
  DNI(Pattern.compile("^\\d{8}$"), "DNI must contain exactly 8 numeric digits."),  // 8 digits
  CE(Pattern.compile("^\\d{9}$"), "Carnet Extranjeria must contain exactly 9 numeric digits."),  // 9 digits
  PASSPORT(Pattern.compile("^[A-Za-z0-9]{6,12}$"), "Passport must contain 6 to 12 alphanumeric characters."),  // 6-12 alphanumeric
  RUC(Pattern.compile("^\\d{11}$"), "RUC must contain exactly 11 numeric digits.");  // 11 digits

  private final Pattern pattern;
  private final String errorMessage;

  DocumentType(Pattern pattern, String errorMessage) {
      this.pattern = pattern;
      this.errorMessage = errorMessage;
  }

  /**
   * Validates the document Number against the enum's regex pattern and normalizes it to uppercase
   *
   * @param value - raw document number
   * @return - trimmed and uppercase normlized document number
   */
  public String validateAndNormalize(String value) {
      if (value == null || !pattern.matcher(value.trim()).matches()) {
          throw new BusinessRuleException("Invalid " + this.name() + " format: " + this.errorMessage);
      }
      return value.trim().toUpperCase();
  }
}