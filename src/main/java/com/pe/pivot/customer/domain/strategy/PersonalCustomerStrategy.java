package com.pe.pivot.customer.domain.strategy;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import io.reactivex.rxjava3.core.Completable;

/**
 * Validation strategy for PERSONAL bank customers.
 */
public class PersonalCustomerStrategy implements CustomerValidationStrategy {

    /**
     * Checks if this strategy supports the given customer type.
     * @param customerType - the type of customer
     * @return - Only support of PERSONAL customer type
     */
    @Override
    public boolean supports(CustomerType customerType) {
        return CustomerType.PERSONAL == customerType;
    }

    /**
     * Validates the attributes of a PERSONAL customer according to business rules.
     * @param customer - the domain customer to validate
     * @return - Completable that completes if valid, or emits a BusinessRuleException if any validation fails
     */
    @Override
    public Completable validate(Customer customer) {
        return Completable.fromAction(() -> {
            // Perform validation logic for personal customers
            if (customer.getDocumentType() == DocumentType.RUC) {
                throw new BusinessRuleException("Personal customers cannot be registered with RUC document type.");
            }

            // Validates that first name and last name are not null or blank
            if (customer.getFirstName() == null || customer.getFirstName().isBlank()
                    || customer.getLastName() == null || customer.getLastName().isBlank()) {
                throw new BusinessRuleException("First name and last name are mandatory for PERSONAL customers.");
            }

            // Validates that personal customers cannot have a PYME profile
            if (customer.getProfile() == CustomerProfile.PYME) {
                throw new BusinessRuleException("Personal customers cannot have a PYME profile.");
            }

            // Validates that lenght of document number is correct for DNI
            if (customer.getDocumentType() == DocumentType.DNI && customer.getDocumentNumber().length() != 8) {
                throw new BusinessRuleException("DNI document number must have exactly 8 characters.");
            }
        });
    }
}
