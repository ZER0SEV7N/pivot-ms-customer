package com.pe.pivot.customer.infrastructure.adapter.in.web.exception;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.exception.DuplicateCustomerException;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global Exception Handler translating domain and validation exceptions into OpenAPI ErrorResponse.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles CustomerNotFoundException and returns a 404 Not Found response with an error message.
     * @param ex - the exception thrown when a customer is not found
     * @param exchange - the current server web exchange
     * @return - Single emitting ResponseEntity with ErrorResponse and 404 status
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public Single<ResponseEntity<ErrorResponse>> handleNotFound(
            CustomerNotFoundException ex, ServerWebExchange exchange) {
        log.warn("Resource not found: {}", ex.getMessage());
        return Single.just(buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), exchange));
    }

    /**
     * Handles DuplicateCustomerException and returns a 409 Conflict response with an error message.
     * @param ex - the exception thrown when a duplicate customer is found
     * @param exchange - the current server web exchange
     * @return - Single emitting ResponseEntity with ErrorResponse and 409 status
     */
    @ExceptionHandler(DuplicateCustomerException.class)
    public Single<ResponseEntity<ErrorResponse>> handleDuplicate(
            DuplicateCustomerException ex, ServerWebExchange exchange) {
        log.warn("Conflict error: {}", ex.getMessage());
        return Single.just(buildResponse(HttpStatus.CONFLICT, ex.getMessage(), exchange));
    }

    /**
     * Handles BusinessRuleException and returns a 400 Bad Request response with an error message.
     * @param ex - the exception thrown when a business rule is violated
     * @param exchange - the current server web exchange
     * @return - Single emitting ResponseEntity with ErrorResponse and 400 status
     */
    @ExceptionHandler(BusinessRuleException.class)
    public Single<ResponseEntity<ErrorResponse>> handleBusinessRule(
            BusinessRuleException ex, ServerWebExchange exchange) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return Single.just(buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange));
    }

    /**
     * Handles validation errors in the web exchange.
     * @param ex - the exception thrown when validation fails
     * @param exchange - the current server web exchange
     * @return - Single emitting ResponseEntity with ErrorResponse and 400 status
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Single<ResponseEntity<ErrorResponse>> handleValidationErrors(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        String errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Payload validation error: {}", errors);
        return Single.just(buildResponse(HttpStatus.BAD_REQUEST, errors, exchange));
    }

    /**
     * Handles generic exceptions and returns a 500 Internal Server Error response.
     * @param ex - the exception thrown when an unexpected error occurs
     * @param exchange - the current server web exchange
     * @return - Single emitting ResponseEntity with ErrorResponse and 500 status
     */
    @ExceptionHandler(Exception.class)
    public Single<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error occurred: ", ex);
        return Single.just(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal server error occurred.", exchange));
    }

    /**
     * Builds a standardized error response for the given HTTP status and message.
     * @param status - The Http status to return
     * @param message - The error message to include in the response
     * @param exchange - The current server web exchange
     * @return - The ResponseEntity with the error response
     */
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, ServerWebExchange exchange) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
