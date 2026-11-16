package com.pe.pivot.customer.domain.strategy;

import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerType;
import io.reactivex.rxjava3.core.Completable;

/**
 * Strategy interface for validating customer business rules according to CustomerType.
 * Applies the Open/Closed Principle (SOLID)
 */
public interface CustomerValidationStrategy {

  /**
   * Checks if this strategy supports the given customer type.
   *
   * @param customerType - the type of customer
   * @return - true if supported, false otherwise
   */
  boolean supports(CustomerType customerType);

  /**
   * Validates the customer attributes according to business rules.
   *
   * @param customer - the domain customer to validate
   * @return - Completable that completes if valid, or emits a BusinessRuleException
   */
  Completable validate(Customer customer);
}
