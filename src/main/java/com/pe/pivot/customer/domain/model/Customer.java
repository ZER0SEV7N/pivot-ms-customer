package com.pe.pivot.customer.domain.model;

import lombok.*;

import java.time.Instant;

/**
 * Core Domain Model representing a Bank Customer.
 * Free of framework and persistence annotations to respect Hexagonal Architecture
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private String id;
    private CustomerType customerType;
    private CustomerProfile profile;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phoneNumber;
    private String address;
    private CustomerStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
