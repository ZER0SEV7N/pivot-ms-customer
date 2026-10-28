package com.pe.pivot.customer.domain.exception;

/**
 * Exception thrown when a domain business rule or validation strategy is violated.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
