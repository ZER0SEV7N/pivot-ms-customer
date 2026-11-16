package com.pe.pivot.customer.infrastructure.adapter.in.web;

import com.pe.pivot.customer.domain.port.in.CreateCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.DeleteCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.ReactivateCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.RetrieveCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.UpdateCustomerUseCase;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerRequest;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerResponse;
import com.pe.pivot.customer.infrastructure.adapter.in.web.mapper.CustomerWebMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Creates a new bank customer.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CreateCustomerUseCase createCustomerUseCase;
  private final RetrieveCustomerUseCase retrieveCustomerUseCase;
  private final UpdateCustomerUseCase updateCustomerUseCase;
  private final DeleteCustomerUseCase deleteCustomerUseCase;
  private final ReactivateCustomerUseCase reactivateCustomerUseCase;
  private final CustomerWebMapper mapper;

  /**
   * Creates a new bank customer.
   *
   * @param request - the customer request payload
   * @return - Single emitting ResponseEntity with the created customer or error
   * @POST /api/c1/customers : Create a new bank customer.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Single<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
    return createCustomerUseCase.create(mapper.toDomain(request))
            .map(mapper::toResponse);
  }

  /**
   * Retrieves all customers.
   *
   * @return - List of all customers
   * @GET /api/v1/customers : Retrieve all customers.
   */
  @GetMapping
  public Flowable<CustomerResponse> getAllCustomers() {
    return retrieveCustomerUseCase.findAll()
            .map(mapper::toResponse);
  }

  /**
   * Retrieves a customer by ID.
   *
   * @param id - the ID of the customer to retrieve
   * @return - Single emitting ResponseEntity with the found customer or error
   * @GET /api/v1/customers/{id} : Get customer by ID.
   */
  @GetMapping("/{id}")
  public Single<ResponseEntity<CustomerResponse>> getCustomerById(@PathVariable String id) {
    return retrieveCustomerUseCase.findById(id)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok);
  }

  /**
   * Retrieves a customer by document number.
   *
   * @param documentNumber - the document number of the customer to retrieve
   * @return - Single emitting ResponseEntity with the found customer or error
   * @GET /api/v1/customers/document/{documentNumber} : Get customer by Document Number.
   */
  @GetMapping("/document/{documentNumber}")
  public Single<ResponseEntity<CustomerResponse>> getCustomerByDocumentNumber(
          @PathVariable String documentNumber) {
    return retrieveCustomerUseCase.findByDocumentNumber(documentNumber)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok);
  }

  /**
   * Updates an existing bank customer.
   *
   * @param id      - identifier of the customer to update
   * @param request - the customer request payload with updated information
   * @return - Single emitting ResponseEntity with the updated customer or error
   * @PUT /api/v1/customers/{id} : Update an existing customer.
   */
  @PutMapping("/{id}")
  public Single<ResponseEntity<CustomerResponse>> updateCustomer(
          @PathVariable String id, @Valid @RequestBody CustomerRequest request) {
    return updateCustomerUseCase.update(id, mapper.toDomain(request))
            .map(mapper::toResponse)
            .map(ResponseEntity::ok);
  }

  /**
   * Deletes a customer by ID.
   *
   * @param id - the ID of the customer to delete
   * @return - Completable that completes when the customer is deleted
   * @DELETE /api/v1/customers/{id} : Delete a customer by ID.
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Completable deleteCustomer(@PathVariable String id) {
    return deleteCustomerUseCase.delete(id);
  }

  /**
   * Reactivates a previously deactivated customer by ID.
   *
   * @param id - the ID of the customer to reactivate
   * @return - Single emitting ResponseEntity with the reactivated customer or error
   * @PATCH /api/v1/customers/{id}/reactivate
   */
  @PatchMapping("/{id}/reactivate")
  public Single<ResponseEntity<CustomerResponse>> reactivateCustomer(@PathVariable String id) {
    return reactivateCustomerUseCase.reactivate(id)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok);
  }
}
