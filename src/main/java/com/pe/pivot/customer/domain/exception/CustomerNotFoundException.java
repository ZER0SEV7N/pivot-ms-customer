package com.pe.pivot.customer.domain.exception;

/**
 * Exception thrown when a requested customer does not exist in the system.
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
