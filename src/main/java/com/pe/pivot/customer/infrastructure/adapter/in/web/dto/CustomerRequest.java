package com.pe.pivot.customer.infrastructure.adapter.in.web.dto;

import com.pe.pivot.customer.domain.model.CustomerProfile;
import com.pe.pivot.customer.domain.model.CustomerStatus;
import com.pe.pivot.customer.domain.model.CustomerType;
import com.pe.pivot.customer.domain.model.DocumentType;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Immutable Request DTO using Java 17 Record, aligned with the OpenAPI 3.0 CustomerRequest schema.
 */
@Builder
public record CustomerRequest(

        @NotNull(message = "customerType is required")
        CustomerType customerType,

        CustomerProfile profile,

        @NotNull(message = "documentType is required")
        DocumentType documentType,

        @NotBlank(message = "documentNumber is required")
        @Size(min = 8, max = 15, message = "documentNumber must be between 8 and 15 characters")
        @Pattern(regexp = "^[0-9A-Za-z]+$", message = "documentNumber must be alphanumeric")
        String documentNumber,

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 150)
        String businessName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "phoneNumber is required")
        @Size(min = 7, max = 20)
        String phoneNumber,

        @Size(max = 250)
        String address,

        CustomerStatus status
) {
}