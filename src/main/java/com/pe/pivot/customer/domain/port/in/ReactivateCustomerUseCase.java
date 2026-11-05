package com.pe.pivot.customer.domain.port.in;

import com.pe.pivot.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port for reactivating an INACTIVE (soft-deleted) or BLOCKED customer.
 */
public interface ReactivateCustomerUseCase {
  Single<Customer> reactivate(String id);
}