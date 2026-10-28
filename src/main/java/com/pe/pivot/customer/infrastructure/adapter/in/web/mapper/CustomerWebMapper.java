package com.pe.pivot.customer.infrastructure.adapter.in.web.mapper;

import com.pe.pivot.customer.domain.model.Customer;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerRequest;
import com.pe.pivot.customer.infrastructure.adapter.in.web.dto.CustomerResponse;
import org.springframework.stereotype.Component;

/**
 * Web Mapper converting between inmutable Java 17 Record DTOs and Domain Customer model.
 */
@Component
public class CustomerWebMapper {

    /**
     * Maps an incoming CustomerRequest record to a Customer domain object.
     *
     * @param request immutable request record
     * @return Customer domain instance
     */
    public Customer toDomain(CustomerRequest request) {
        return Customer.builder()
                .customerType(request.customerType())
                .profile(request.profile())
                .documentType(request.documentType())
                .documentNumber(request.documentNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .businessName(request.businessName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .status(request.status())
                .build();
    }

    /**
     * Maps a Customer domain object to an immutable CustomerResponse record.
     *
     * @param domain Customer domain instance
     * @return immutable CustomerResponse record
     */
    public CustomerResponse toResponse(Customer domain) {
        return CustomerResponse.builder()
                .id(domain.getId())
                .customerType(domain.getCustomerType())
                .profile(domain.getProfile())
                .documentType(domain.getDocumentType())
                .documentNumber(domain.getDocumentNumber())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .businessName(domain.getBusinessName())
                .email(domain.getEmail())
                .phoneNumber(domain.getPhoneNumber())
                .address(domain.getAddress())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
