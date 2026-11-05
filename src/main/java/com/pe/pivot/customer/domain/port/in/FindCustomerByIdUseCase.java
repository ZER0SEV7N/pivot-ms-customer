package com.pe.pivot.customer.domain.port.in;

import com.pe.pivot.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port for finding a customer by its unique identifier.
 * This interface is used to centralize the not-found behavior so other use cases can reuse the
 * same lookup without duplicating repository and error-handling logic.
 */
public interface FindCustomerByIdUseCase {
    Single<Customer> findById(String id);
}
