package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.exception.CustomerNotFoundException;
import com.pe.pivot.customer.domain.exception.DuplicateCustomerException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.port.in.*;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import com.pe.pivot.customer.domain.strategy.CustomerValidationStrategy;
import com.pe.pivot.customer.infrastructure.util.DataMaskingUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Application Service implementing all customer
 * use cases using RxJava 3 and Strategy Pattern.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerUseCaseImpl implements
        CreateCustomerUseCase,
        RetrieveCustomerUseCase,
        UpdateCustomerUseCase,
        DeleteCustomerUseCase {

    private final CustomerRepositoryPort repositoryPort;
    private final List<CustomerValidationStrategy> strategies;

    /**
     * Registers a new customer after validating strategy rules and checking document uniqueness.
     *
     * @Param - Customer domain customer to create
     * @Return - Single emitting the persisted customer
     */
    @Override
    public Single<Customer> execute(Customer customer) {
        log.debug("Starting customer registration for document: {}",
            DataMaskingUtil.maskDocument(customer.getDocumentNumber()));

        return applyValidationStrategy(customer)
                .andThen(repositoryPort.existsByDocumentNumber(customer.getDocumentNumber()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)){
                        log.warn("Duplicate registration attempt for document: {}",
                                DataMaskingUtil.maskDocument(customer.getDocumentNumber()));
                        return Single.error(new DuplicateCustomerException(
                                "A customer with the provided document number already exists."));
                    }
                    prepareNewCustomerDefaults(customer);
                    return repositoryPort.save(customer);
                })
                .doOnSuccess(saved -> log.info("Customer created successfully with ID: {} and type: {}",
                        saved.getId(), saved.getCustomerType()));
    }

    /**
     * Retrieves all registered customers reactively.
     *
     * @return Flowable emitting all customers
     */
    @Override
    public Flowable<Customer> findAll() {
        log.debug("Retrieving all customers");
        return repositoryPort.findAll();
    }

    /**
     * Finds a customer by its unique ID.
     *
     * @param id customer identifier
     * @return Single emitting the found customer or CustomerNotFoundException
     */
    @Override
    public Single<Customer> findById(String id) {
        log.debug("Searching customer by ID: {}", id);
        return repositoryPort.findById(id)
                .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer not found with ID: " + id)));
    }

    /**
     * Finds a customer by its identification document number.
     *
     * @param documentNumber official document number
     * @return Single emitting the found customer or CustomerNotFoundException
     */
    @Override
    public Single<Customer> findByDocumentNumber(String documentNumber) {
        log.debug("Searching customer by documentNumber: {}", DataMaskingUtil.maskDocument(documentNumber));
        return repositoryPort.findByDocumentNumber(documentNumber)
                .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer not found with document number: " + DataMaskingUtil.maskDocument(documentNumber))));
    }

    /**
     * Updates an existing customer by ID applying functional Optional mapping.
     *
     * @param id       customer identifier
     * @param customer updated customer data
     * @return Single emitting the updated customer
     */
    @Override
    public Single<Customer> execute(String id, Customer customer) {
        log.debug("Updating customer with ID: {}", id);
       return applyValidationStrategy(customer)
               .andThen(findById(id))
               .flatMap(existingCustomer -> {
                   updateCustomerFields(existingCustomer, customer);
                   return repositoryPort.save(existingCustomer);
               })
               .doOnSuccess(updated -> log.info("Customer updated successfully with ID: {}", updated.getId()));
    }

    /**
     * Deletes a customer by ID after verifying its existence.
     *
     * @param id customer identifier
     * @return Completable that completes when deleted
     */
    @Override
    public Completable execute(String id) {
        log.debug("Deleting customer with ID: {}", id);
        return findById(id)
                .flatMapCompletable(existing -> repositoryPort.deleteById(existing.getId()))
                .doOnComplete(() -> log.info("Customer deleted successfully with ID: {}", id));
    }


    /**
     * Selects and executes the appropriate validation strategy using Java Streams.
     *
     * @param customer domain customer
     * @return Completable result of validation
     */
    private Completable applyValidationStrategy(Customer customer) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(customer.getCustomerType()))
                .findFirst()
                .map(strategy -> strategy.validate(customer))
                .orElseGet(() -> Completable.error(
                        new BusinessRuleException("Unsupported customer type: " + customer.getCustomerType())));
    }

    /**
     * Initializes default values and timestamps for a new customer.
     *
     * @param customer domain customer
     */
    private void prepareNewCustomerDefaults(Customer customer) {
        // Set default profile to STANDARD
        if (customer.getProfile() == null) {
            customer.setProfile(CustomerProfile.STANDARD);
        }

        // Set default status to ACTIVE if not provided
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }

        // Set creation and update timestamps
        Instant now = Instant.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
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
