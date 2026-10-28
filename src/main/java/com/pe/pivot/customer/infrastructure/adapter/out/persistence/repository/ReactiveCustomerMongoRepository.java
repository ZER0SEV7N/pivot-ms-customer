package com.pe.pivot.customer.infrastructure.adapter.out.persistence.repository;

import com.pe.pivot.customer.infrastructure.adapter.out.persistence.entity.CustomerDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Spring Data Reactive MongoDB repository for CustomerDocument.
 * Uses derived query methods without @Query annotation as required.
 */
@Repository
public interface ReactiveCustomerMongoRepository extends ReactiveMongoRepository<CustomerDocument, String> {

    Mono<CustomerDocument> findByDocumentNumber(String documentNumber);

    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
