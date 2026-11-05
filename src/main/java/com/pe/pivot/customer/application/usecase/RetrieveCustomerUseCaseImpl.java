package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.in.RetrieveCustomerUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import com.pe.pivot.customer.infrastructure.util.DataMaskingUtil;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application Service responsible only for retrieving customers.
 * It provides reactive queries for all customers, IDs, and document numbers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetrieveCustomerUseCaseImpl implements RetrieveCustomerUseCase {
    private final CustomerRepositoryPort repositoryPort;
    private final FindCustomerByIdUseCase findCustomerByIdUseCase;

    /**
     * Retrieves all registered customers reactively.
     *
     * @return Flowable emitting all customers
     */
    @Override
    public Flowable<Customer> findAll() {
        log.debug("Retrieving all customers");
        return repositoryPort.findAll();
    }

    /**
     * Finds a customer by its unique ID.
     *
     * @param id customer identifier
     * @return Single emitting the found customer or CustomerNotFoundException
     */
    @Override
    public Single<Customer> findById(String id) {
        return findCustomerByIdUseCase.findById(id);
    }

    /**
     * Finds a customer by its identification document number.
     *
     * @param documentNumber official document number
     * @return Single emitting the found customer or CustomerNotFoundException
     */
    @Override
    public Single<Customer> findByDocumentNumber(String documentNumber) {
        log.debug("Searching customer by documentNumber: {}",
                DataMaskingUtil.maskDocument(documentNumber));
        return repositoryPort.findByDocumentNumber(documentNumber)
                .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer not found with document number: " + DataMaskingUtil.maskDocument(documentNumber))));
    }
}
