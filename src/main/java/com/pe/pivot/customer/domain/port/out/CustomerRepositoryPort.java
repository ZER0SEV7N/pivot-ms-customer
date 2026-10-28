package com.pe.pivot.customer.domain.port.out;

import com.pe.pivot.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Output Port defining persistence operations required by the domain.
 */
public interface CustomerRepositoryPort {
    Single<Customer> save(Customer customer);
    Flowable<Customer> findAll();
    Maybe<Customer> findById(String id);
    Maybe<Customer> findByDocumentNumber(String documentNumber);
    Single<Boolean> existsByDocumentNumber(String documentNumber);
    Completable deleteById(String id);
}
