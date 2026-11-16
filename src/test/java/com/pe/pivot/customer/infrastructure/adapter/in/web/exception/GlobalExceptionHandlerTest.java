package com.pe.pivot.customer.infrastructure.adapter.in.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.exception.DuplicateCustomerException;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 * Verifies the HTTP status, error code, and standard body for each handled exception.
 */
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private ServerWebExchange exchange;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/customers/customer-1").build());
  }

  private void assertErrorResponse(ResponseEntity<ErrorResponse> response,
                                   HttpStatus expectedStatus, String expectedMessage) {
    assertEquals(expectedStatus, response.getStatusCode());
    assertNotNull(response.getBody());
    ErrorResponse body = response.getBody();
    assertEquals(expectedStatus.value(), body.status());
    assertEquals(expectedStatus.getReasonPhrase(), body.error());
    assertEquals(expectedMessage, body.message());
    assertEquals("/api/v1/customers/customer-1", body.path());
    assertNotNull(body.timestamp());
  }

  @Test
  @DisplayName("should return 404 when the customer is not found")
  void shouldReturnNotFound() {
    // Arrange
    CustomerNotFoundException exception =
            new CustomerNotFoundException("Customer not found with ID: customer-1");

    // Act
    Single<ResponseEntity<ErrorResponse>> result = handler.handleNotFound(exception, exchange);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertErrorResponse(response, HttpStatus.NOT_FOUND,
                      "Customer not found with ID: customer-1");
              return true;
            })
            .assertComplete();
  }

  @Test
  @DisplayName("should return 409 when the customer is duplicated")
  void shouldReturnConflict() {
    // Arrange
    DuplicateCustomerException exception =
            new DuplicateCustomerException("Duplicate document number.");

    // Act
    Single<ResponseEntity<ErrorResponse>> result = handler.handleDuplicate(exception, exchange);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertErrorResponse(response, HttpStatus.CONFLICT,
                      "Duplicate document number.");
              return true;
            })
            .assertComplete();
  }

  @Test
  @DisplayName("should return 400 when a business rule is violated")
  void shouldReturnBadRequestForBusinessRule() {
    // Arrange
    BusinessRuleException exception = new BusinessRuleException("Invalid business rule.");

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleBusinessRule(exception, exchange);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertErrorResponse(response, HttpStatus.BAD_REQUEST,
                      "Invalid business rule.");
              return true;
            })
            .assertComplete();
  }

  @Test
  @DisplayName("should return 400 with joined messages when payload validation fails")
  void shouldReturnBadRequestForValidationErrors() {
    // Arrange
    BeanPropertyBindingResult bindingResult =
            new BeanPropertyBindingResult(new Object(), "customerRequest");
    bindingResult.addError(
            new ObjectError("customerRequest", "email must be a valid email address"));
    bindingResult.addError(
            new ObjectError("customerRequest", "documentNumber is required"));
    WebExchangeBindException exception = new WebExchangeBindException(null, bindingResult);

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleValidationErrors(exception, exchange);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
              assertNotNull(response.getBody());
              assertEquals(400, response.getBody().status());
              assertEquals("Bad Request", response.getBody().error());
              assertTrue(response.getBody().message()
                      .contains("email must be a valid email address"));
              assertTrue(response.getBody().message()
                      .contains("documentNumber is required"));
              assertEquals("/api/v1/customers/customer-1", response.getBody().path());
              return true;
            })
            .assertComplete();
  }

  @Test
  @DisplayName("should return 500 when an unexpected error occurs")
  void shouldReturnInternalServerError() {
    // Arrange
    Exception exception = new RuntimeException("Unexpected failure.");

    // Act
    Single<ResponseEntity<ErrorResponse>> result =
            handler.handleGenericException(exception, exchange);

    // Assert
    result.test()
            .assertNoErrors()
            .assertValue(response -> {
              assertErrorResponse(response,
                      HttpStatus.INTERNAL_SERVER_ERROR,
                      "An unexpected internal server error occurred.");
              return true;
            })
            .assertComplete();
  }
}
