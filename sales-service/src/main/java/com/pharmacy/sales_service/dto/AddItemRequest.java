package com.pharmacy.sales_service.dto;

public record AddItemRequest(
        Long medicineId,
        int qty
) {}

