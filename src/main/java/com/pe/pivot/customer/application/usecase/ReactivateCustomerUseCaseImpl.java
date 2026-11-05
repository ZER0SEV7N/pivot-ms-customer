package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.in.ReactivateCustomerUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Application Service responsible only for reactivating customers.
 * It changes an INACTIVE or BLOCKED customer back to ACTIVE.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactivateCustomerUseCaseImpl implements ReactivateCustomerUseCase {
    private final CustomerRepositoryPort repositoryPort;
    private final FindCustomerByIdUseCase findCustomerByIdUseCase;

    /**
     * Reactivates an INACTIVE or BLOCKED customer back to ACTIVE status.
     *
     * @param id customer unique identifier
     * @return Single emitting the reactivated Customer
     */
    @Override
    public Single<Customer> reactivate(String id) {
        log.debug("Reactivating customer with ID: {}", id);
        return findCustomerByIdUseCase.findById(id)
                .flatMap(existing -> {
                    if (existing.getStatus() == CustomerStatus.ACTIVE) {
                        return Single.error(new BusinessRuleException("Customer is already ACTIVE."));
                    }
                    existing.setStatus(CustomerStatus.ACTIVE);
                    existing.setUpdatedAt(Instant.now());
                    return repositoryPort.save(existing);
                })
                .doOnSuccess(updated -> log.info("Customer reactivated to ACTIVE with ID: {}",
                        updated.getId()));
    }

}
