package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application Service responsible only for finding a customer by ID.
 * It centralizes the not-found behavior so other use cases can reuse the
 * same lookup without duplicating repository and error-handling logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FindCustomerByIdUseCaseImpl implements FindCustomerByIdUseCase {
    private final CustomerRepositoryPort repositoryPort;

    /**
     * Finds a customer by its unique ID.
     *
     * @param id customer identifier
     * @return Single emitting the found customer or CustomerNotFoundException
     */
    @Override
    public Single<Customer> findById(String id) {
        log.debug("Searching customer by ID: {}", id);
        return repositoryPort.findById(id)
                .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer not found with ID: " + id)));
    }
}
