package com.pe.pivot.customer.domain.strategy;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import io.reactivex.rxjava3.core.Completable;

/**
 * Validation strategy for BUSINESS Bank Customers.
 */
public class BusinessCustomerStrategy implements CustomerValidationStrategy {

  /**
   * Checks if this strategy supports the given customer type.
   *
   * @param customerType - the type of customer
   * @return - Only support of BUSINESS customer type
   */
  @Override
  public boolean supports(CustomerType customerType) {
    return CustomerType.BUSINESS == customerType;
  }

  /**
   * Validates the attributes of a BUSINESS customer according to business rules.
   *
   * @param customer - the domain customer to validate
   * @return - Completable that completes if valid, or emits a BusinessRuleException if any
   *     validation fails
   */
  @Override
  public Completable validate(Customer customer) {
    return Completable.fromAction(() -> {
      if (customer.getDocumentType() != DocumentType.RUC) {
        throw new BusinessRuleException(
                "Business customers must be registered with a RUC document type.");
      }

      String normalizedDoc = customer.getDocumentType()
              .validateAndNormalize(customer.getDocumentNumber());
      customer.setDocumentNumber(normalizedDoc);

      if (customer.getBusinessName() == null || customer.getBusinessName().isBlank()) {
        throw new BusinessRuleException(
                "Business name (Razon Social) is mandatory for BUSINESS customers.");
      }

      if (customer.getProfile() == CustomerProfile.VIP) {
        throw new BusinessRuleException("Business customers cannot have a VIP profile.");
      }
    });
  }
}
