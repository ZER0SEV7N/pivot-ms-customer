package com.pe.pivot.customer.infrastructure.adapter.out.persistence.mapper;

import com.pe.pivot.customer.domain.model.*;
import com.pe.pivot.customer.infrastructure.adapter.out.persistence.entity.CustomerDocument;
import org.springframework.stereotype.Component;

/**
 * Mapper component converting between Domain Customer and MongoDB CustomerDocument.
 */
@Component
public class CustomerPersistenceMapper {

    /**
     * Transform Customer Domain to CustomerDocument for persistence
     *
     * @param domain - Customer domain object
     * @return - CustomerDocument for persistence
     */
    public CustomerDocument toDocument(Customer domain) {
        return CustomerDocument.builder()
                .id(domain.getId())
                .customerType(domain.getCustomerType() != null ? domain.getCustomerType().name() : null)
                .profile(domain.getProfile() != null ? domain.getProfile().name() : null)
                .documentType(domain.getDocumentType() != null ? domain.getDocumentType().name() : null)
                .documentNumber(domain.getDocumentNumber())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .businessName(domain.getBusinessName())
                .email(domain.getEmail())
                .phoneNumber(domain.getPhoneNumber())
                .address(domain.getAddress())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Transform CustomerDocument to Customer Domain
     *
     * @param doc - CustomerDocument from persistence layer
     * @return - Customer domain object
     */
    public Customer toDomain(CustomerDocument doc) {
        return Customer.builder()
                .id(doc.getId())
                .customerType(doc.getCustomerType() != null ? CustomerType.valueOf(doc.getCustomerType()) : null)
                .profile(doc.getProfile() != null ? CustomerProfile.valueOf(doc.getProfile()) : null)
                .documentType(doc.getDocumentType() != null ? DocumentType.valueOf(doc.getDocumentType()) : null)
                .documentNumber(doc.getDocumentNumber())
                .firstName(doc.getFirstName())
                .lastName(doc.getLastName())
                .businessName(doc.getBusinessName())
                .email(doc.getEmail())
                .phoneNumber(doc.getPhoneNumber())
                .address(doc.getAddress())
                .status(doc.getStatus() != null ? CustomerStatus.valueOf(doc.getStatus()) : null)
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
