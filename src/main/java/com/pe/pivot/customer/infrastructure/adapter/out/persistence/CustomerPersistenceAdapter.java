package com.pe.pivot.customer.infrastructure.adapter.out.persistence;

import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import com.pe.pivot.customer.infrastructure.adapter.out.persistence.mapper.CustomerPersistenceMapper;
import com.pe.pivot.customer.infrastructure.adapter.out.persistence.repository.ReactiveCustomerMongoRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * Persistence Adapter implementing CustomerRepositoryPort using Reactive MongoDB and RxJava 3.
 */
@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final ReactiveCustomerMongoRepository mongoRepository;
    private final CustomerPersistenceMapper mapper;

    /**
     * Saves a customer to the database.
     *
     * @param customer - the domain customer to save
     * @return - Single emitting the persisted customer
     */
    @Override
    public Single<Customer> save(Customer customer) {
        return RxJava3Adapter.monoToSingle(
                mongoRepository.save(mapper.toDocument(customer))
                        .map(mapper::toDomain)
        );
    }

    /**
     * Retrieves all customers from the database.
     *
     * @return - Flowable emitting all customers
     */
    @Override
    public Flowable<Customer> findAll() {
        return RxJava3Adapter.fluxToFlowable(
                mongoRepository.findAll()
                        .map(mapper::toDomain)
        );
    }

    /**
     * Finds a customer by their ID.
     *
     * @param id - the ID of the customer to find
     * @return - Maybe emitting the found customer, or empty if not found
     */
    @Override
    public Maybe<Customer> findById(String id) {
        return RxJava3Adapter.monoToMaybe(
                mongoRepository.findById(id)
                        .map(mapper::toDomain)
        );
    }

    /**
     * Finds a customer by their document number.
     *
     * @param documentNumber - the document number of the customer to find
     * @return - Maybe emitting the found customer, or empty if not found
     */
    @Override
    public Maybe<Customer> findByDocumentNumber(String documentNumber) {
        return RxJava3Adapter.monoToMaybe(
                mongoRepository.findByDocumentNumber(documentNumber)
                        .map(mapper::toDomain)
        );
    }

    /**
     * Checks if a customer exists by their document number.
     *
     * @param documentNumber - the document number to check for existence
     * @return - Single emitting true if exists, false otherwise
     */
    @Override
    public Single<Boolean> existsByDocumentNumber(String documentNumber) {
        return RxJava3Adapter.monoToSingle(
                mongoRepository.existsByDocumentNumber(documentNumber)
        );
    }

    /**
     * Deletes a customer by their ID.
     *
     * @param id - the ID of the customer to delete
     * @return - Completable that completes when deletion is done
     */
    @Override
    public Completable deleteById(String id) {
        return RxJava3Adapter.monoToCompletable(
                mongoRepository.deleteById(id)
        );
    }
}
