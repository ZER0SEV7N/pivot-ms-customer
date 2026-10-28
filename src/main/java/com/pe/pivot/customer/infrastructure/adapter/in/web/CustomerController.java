package com.pe.pivot.customer.infrastructure.adapter.in.web;

import com.pe.pivot.customer.domain.port.in.CreateCustomerUseCase;
import com.pe.pivot.customer.domain.port.in.DeleteCustomerUseCase;
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
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller implementing the OpenAPI contract for Customer operations using RxJava 3.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final RetrieveCustomerUseCase retrieveCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final CustomerWebMapper mapper;

    /**
     * POST /api/c1/customers : Create a new bank customer.
     * @param request - the customer request payload
     * @return - Single emitting ResponseEntity with the created customer or error
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Single<ResponseEntity<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request){
        return createCustomerUseCase.execute(mapper.toDomain(request))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    /**
     * GET /api/v1/customers : Retrieve all customers.
     * @return - List of all customers
     */
    @GetMapping
    public Flowable<CustomerResponse> getAllCustomers() {
        return retrieveCustomerUseCase.findAll()
                .map(mapper::toResponse);
    }

    /**
     * GET /api/v1/customers/{id} : Get customer by ID.
     * @param id - the ID of the customer to retrieve
     * @return - Single emitting ResponseEntity with the found customer or error
     */
    @GetMapping("/{id}")
    public Single<ResponseEntity<CustomerResponse>> getCustomerById(@PathVariable String id) {
        return retrieveCustomerUseCase.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    /**
     * GET /api/v1/customers/document/{documentNumber} : Get customer by Document Number.
     * @param documentNumber - the document number of the customer to retrieve
     * @return - Single emitting ResponseEntity with the found customer or error
     */
    @GetMapping("/document/{documentNumber}")
    public Single<ResponseEntity<CustomerResponse>> getCustomerByDocumentNumber(@PathVariable String documentNumber) {
        return retrieveCustomerUseCase.findByDocumentNumber(documentNumber)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    /**
     * PUT /api/v1/customers/{id} : Update an existing customer.
     * @param id - identifier of the customer to update
     * @param request - the customer request payload with updated information
     * @return - Single emitting ResponseEntity with the updated customer or error
     */
    @PutMapping("/{id}")
    public Single<ResponseEntity<CustomerResponse>> updateCustomer(@PathVariable String id, @Valid @RequestBody CustomerRequest request) {
        return updateCustomerUseCase.execute(id, mapper.toDomain(request))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    /**
     * DELETE /api/v1/customers/{id} : Delete a customer by ID.
     * @param id - the ID of the customer to delete
     * @return - Completable that completes when the customer is deleted
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Completable deleteCustomer(@PathVariable String id) {
        return deleteCustomerUseCase.execute(id);
    }
}
