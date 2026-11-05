package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.exception.DuplicateCustomerException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.port.in.CreateCustomerUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import com.pe.pivot.customer.domain.strategy.CustomerValidationStrategy;
import com.pe.pivot.customer.infrastructure.util.DataMaskingUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Application Service responsible only for registering new customers.
 * It validates the customer, applies defaults, checks document uniqueness,
 * and persists the new customer using RxJava 3 and Strategy Pattern.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {

    private final CustomerRepositoryPort repositoryPort;
    private final List<CustomerValidationStrategy> strategies;

    /**
     * Registers a new customer after validating strategy rules and checking document uniqueness.
     *
     * @param customer - Customer domain customer to create
     * @return - Single emitting the persisted customer
     */
    @Override
    public Single<Customer> create(Customer customer) {
        log.debug("Starting customer registration for document: {}",
                DataMaskingUtil.maskDocument(customer.getDocumentNumber()));

        return applyValidationStrategy(customer)
                .andThen(repositoryPort.existsByDocumentNumber(customer.getDocumentNumber()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("Duplicate registration attempt for document: {}",
                                DataMaskingUtil.maskDocument(customer.getDocumentNumber()));
                        return Single.error(new DuplicateCustomerException("A customer with the provided document number already exists."));
                    }
                    prepareNewCustomerDefaults(customer);
                    return repositoryPort.save(customer);
                })
                .doOnSuccess(saved -> log.info("Customer created successfully with ID: {} and type: {}",
                        saved.getId(), saved.getCustomerType()));
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
                .orElseGet(() -> Completable.error(new BusinessRuleException("Unsupported customer type: " + customer.getCustomerType())));
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
}
