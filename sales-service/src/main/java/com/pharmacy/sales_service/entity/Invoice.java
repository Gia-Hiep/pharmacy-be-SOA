package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="invoices")
@Getter @Setter
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name="customer_id")
    private Long customerId;

    @Column(name="prescription_id")
    private Long prescriptionId;

    @Column(name="cashier_id", nullable = false)
    private Long cashierId;

    @Column(nullable = false)
    private String status; // DRAFT/WAIT_PAYMENT/PAID/CANCELLED

    @Column(name="payment_status", nullable = false)
    private String paymentStatus; // UNPAID/PAID/REFUNDED

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal discount;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private String notes;
}
