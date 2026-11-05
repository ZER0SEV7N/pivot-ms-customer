package com.pe.pivot.customer.application.usecase;

import com.pe.pivot.customer.domain.exception.BusinessRuleException;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.port.in.FindCustomerByIdUseCase;
import com.pe.pivot.customer.domain.port.in.DeleteCustomerUseCase;
import com.pe.pivot.customer.domain.port.out.CustomerRepositoryPort;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Application Service responsible only for the customer's soft delete.
 * It changes the customer status to INACTIVE without physically removing
 * the persisted record.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCustomerUseCaseImpl implements DeleteCustomerUseCase {
    private final CustomerRepositoryPort repositoryPort;
    private final FindCustomerByIdUseCase findCustomerByIdUseCase;

    /**
     * Performs a Soft Delete (logical deactivation) of a customer by setting status to INACTIVE.
     *
     * @param id customer unique identifier
     * @return Completable indicating completion
     */
    @Override
    public Completable delete(String id) {
        log.debug("Initiating soft delete (deactivation) for customer ID: {}", id);
        return findCustomerByIdUseCase.findById(id)
                .flatMapCompletable(existing -> {
                    if (existing.getStatus() == CustomerStatus.INACTIVE) {
                        return Completable.error(new BusinessRuleException("Customer is already INACTIVE."));
                    }
                    existing.setStatus(CustomerStatus.INACTIVE);
                    existing.setUpdatedAt(Instant.now());
                    return repositoryPort.save(existing).ignoreElement();
                })
                .doOnComplete(() -> log.info("Customer logically deactivated (INACTIVE) with ID: {}", id));
    }

}
