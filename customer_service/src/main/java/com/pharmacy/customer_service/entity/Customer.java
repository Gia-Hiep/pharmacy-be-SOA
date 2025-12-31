package com.pharmacy.customer_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="customers",
        indexes = {
                @Index(name="idx_customer_phone", columnList="phone", unique = true)
        }
)
@Getter @Setter
public class Customer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    private String email;
    private String address;

    private LocalDate dateOfBirth;
    private String gender; // MALE/FEMALE/OTHER

    @Column(nullable = false)
    private int loyaltyPoints;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
