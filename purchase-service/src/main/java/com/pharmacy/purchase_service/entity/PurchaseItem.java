package com.pharmacy.purchase_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="purchase_items")
@Getter @Setter
public class PurchaseItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="purchase_id", nullable=false)
    private Long purchaseId;

    @Column(name="medicine_id", nullable=false)
    private Long medicineId;

    @Column(name="lot_number", nullable=false)
    private String lotNumber;

    @Column(name="expiry_date", nullable=false)
    private LocalDate expiryDate;

    @Column(name="import_price", nullable=false)
    private BigDecimal importPrice;

    @Column(nullable=false)
    private int qty;

    @Column(name="line_total", nullable=false)
    private BigDecimal lineTotal;
}
