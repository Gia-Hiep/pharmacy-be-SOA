package com.pharmacy.inventory_service.dto;

public record StockSummaryDto(
        Long medicineId,
        long onHand,
        long reserved,
        long available
) {}
