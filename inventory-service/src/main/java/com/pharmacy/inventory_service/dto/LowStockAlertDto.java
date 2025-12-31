package com.pharmacy.inventory_service.dto;

public record LowStockAlertDto(
        Long medicineId,
        long available,
        int minStockLevel
) {}
