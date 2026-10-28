package com.pe.pivot.customer.domain.port.in;

import io.reactivex.rxjava3.core.Completable;

/**
 * Input Port for deleting a customer.
 */
public interface DeleteCustomerUseCase {
    Completable execute(String id);
}
