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
     * @return - Completable that completes if valid,
     * or emits a BusinessRuleException if any validation fails
     */
    @Override
    public Completable validate(Customer customer) {
        return Completable.fromAction(() -> {
            // Perform validation logic for Business customers

            // Validates that business customers must be registered with a RUC document type
            if (customer.getDocumentType() != DocumentType.RUC) {
                throw new BusinessRuleException("Business customers must be registered with a RUC document type.");
            }

            // Validates that the RUC document number has exactly 11 digits
            if (customer.getDocumentNumber() == null || customer.getDocumentNumber().length() != 11) {
                throw new BusinessRuleException("RUC document number must have exactly 11 digits.");
            }

            // Validates that the business name is not null or blank
            if (customer.getBusinessName() == null || customer.getBusinessName().isBlank()) {
                throw new BusinessRuleException("Business name (Razon Social) is mandatory for BUSINESS customers.");
            }

            // Validates of business customers cannot have a VIP profile
            if (customer.getProfile() == CustomerProfile.VIP) {
                throw new BusinessRuleException("Business customers cannot have a VIP profile.");
            }
        });
    }
}
