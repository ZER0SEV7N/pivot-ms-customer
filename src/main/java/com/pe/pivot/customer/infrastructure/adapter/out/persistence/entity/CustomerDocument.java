package com.pe.pivot.customer.infrastructure.adapter.out.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

/**
 * MongoDB Document representing a customer in the 'customers' collection.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customers")
public class CustomerDocument {

    @Id
    private String id;
    private String customerType;
    private String profile;
    private String documentType;

    @Indexed(unique = true)
    private String documentNumber;

    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phoneNumber;
    private String address;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
