package com.pharmacy.purchase_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddPurchaseItemRequest(
        Long medicineId,
        String lotNumber,
        LocalDate expiryDate,
        BigDecimal importPrice,
        int qty
) {}
