package com.pe.pivot.customer.infrastructure.adapter.in.web.dto;

import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import lombok.*;

import java.time.Instant;

/**
 * Immutable Response DTO using Java 17 Record, aligned with the OpenAPI 3.0 CustomerResponse schema.
 */
@Builder
public record CustomerResponse(
        String id,
        CustomerType customerType,
        CustomerProfile profile,
        DocumentType documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String businessName,
        String email,
        String phoneNumber,
        String address,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}