package com.pe.pivot.customer.domain.exception;

/**
 * Exception thrown when attempting to register a customer with an existing document number, indicating a duplicate customer entry.
 */
public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String message) {
        super(message);
    }
}
