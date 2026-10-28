package com.pe.pivot.customer.domain.port.in;

import com.pe.pivot.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port for retrieving bank customer information.
 */
public interface RetrieveCustomerUseCase {

    Flowable<Customer> findAll();
    Single<Customer> findById(String id);
    Single<Customer> findByDocumentNumber(String documentNumber);
}
