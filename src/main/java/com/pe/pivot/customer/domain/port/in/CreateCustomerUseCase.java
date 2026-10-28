package com.pe.pivot.customer.domain.port.in;

import com.pe.pivot.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port for creating a new bank customer.
 */
public interface CreateCustomerUseCase {
    Single<Customer> execute(Customer customer);
}
