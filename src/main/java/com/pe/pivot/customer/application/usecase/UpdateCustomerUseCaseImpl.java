package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.in.UpdateCustomerUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import com.pe.pivot.customer.domain.strategy.CustomerValidationStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Application Service responsible only for updating existing customers.
 * It validates incoming data and changes only the non-null fields supplied
 * by the request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCustomerUseCaseImpl implements UpdateCustomerUseCase {
    private final CustomerRepositoryPort repositoryPort;
    private final List<CustomerValidationStrategy> strategies;
    private final FindCustomerByIdUseCase findCustomerByIdUseCase;

    /**
     * Updates an existing customer by ID applying functional Optional mapping.
     *
     * @param id       customer identifier
     * @param customer updated customer data
     * @return Single emitting the updated customer
     */
    @Override
    public Single<Customer> update(String id, Customer customer) {
        log.debug("Updating customer with ID: {}", id);
        return applyValidationStrategy(customer)
                .andThen(findCustomerByIdUseCase.findById(id))
                .flatMap(existingCustomer -> {
                    updateCustomerFields(existingCustomer, customer);
                    return repositoryPort.save(existingCustomer);
                })
                .doOnSuccess(updated -> log.info("Customer updated successfully with ID: {}", updated.getId()));
    }

    /**
     * Applies the appropriate validation strategy based on the customer type.
     *
     * @param customer - Customer domain object to validate
     * @return - Completable indicating validation completion or error
     * @throws BusinessRuleException if no strategy supports the customer type
     */
    private Completable applyValidationStrategy(Customer customer) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(customer.getCustomerType()))
                .findFirst()
                .map(strategy -> strategy.validate(customer))
                .orElseGet(() -> Completable.error(new BusinessRuleException("Unsupported customer type: " + customer.getCustomerType())));
    }

    /**
     * Safely updates only non-null fields from the incoming request into the existing domain entity
     * using Java Optional and functional method references.
     *
     * @param existing target customer from persistence
     * @param incoming source customer with updated attributes
     */
    private void updateCustomerFields(Customer existing, Customer incoming) {
        Optional.ofNullable(incoming.getCustomerType()).ifPresent(existing::setCustomerType);
        Optional.ofNullable(incoming.getProfile()).ifPresent(existing::setProfile);
        Optional.ofNullable(incoming.getDocumentType()).ifPresent(existing::setDocumentType);
        Optional.ofNullable(incoming.getDocumentNumber()).ifPresent(existing::setDocumentNumber);
        Optional.ofNullable(incoming.getFirstName()).ifPresent(existing::setFirstName);
        Optional.ofNullable(incoming.getLastName()).ifPresent(existing::setLastName);
        Optional.ofNullable(incoming.getBusinessName()).ifPresent(existing::setBusinessName);
        Optional.ofNullable(incoming.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(incoming.getPhoneNumber()).ifPresent(existing::setPhoneNumber);
        Optional.ofNullable(incoming.getAddress()).ifPresent(existing::setAddress);
        Optional.ofNullable(incoming.getStatus()).ifPresent(existing::setStatus);
        existing.setUpdatedAt(Instant.now());
    }
}
