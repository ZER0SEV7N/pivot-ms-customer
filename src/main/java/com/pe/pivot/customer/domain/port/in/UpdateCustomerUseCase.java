package com.pe.pivot.customer.domain.port.in;

import com.pe.pivot.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port for updating an existing customer.
 */
public interface UpdateCustomerUseCase {
    Single<Customer> execute(String id, Customer customer);
}
