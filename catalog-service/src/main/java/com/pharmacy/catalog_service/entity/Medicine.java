package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="medicines")
@Getter @Setter
public class Medicine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String code;

    @Column(nullable=false)
    private String name;

    @Column(name="generic_name")
    private String genericName;

    @Column(nullable=false)
    private String unit;

    @Column(name="is_rx", nullable=false)
    private boolean isRx;

    private String manufacturer;

    @Column(name="category_id")
    private Long categoryId;

    @Column(name="default_supplier_id")
    private Long defaultSupplierId;

    @Column(name="sale_price", nullable=false)
    private BigDecimal salePrice;

    private String barcode;

    @Column(name="image_url")
    private String imageUrl;

    private String description;

    @Column(name="usage_instructions")
    private String usageInstructions;

    @Column(name="side_effects")
    private String sideEffects;

    @Column(nullable=false)
    private String status; // ACTIVE/INACTIVE

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
}
