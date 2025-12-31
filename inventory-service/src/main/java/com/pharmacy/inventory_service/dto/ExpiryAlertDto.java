package com.pharmacy.inventory_service.dto;

import java.time.LocalDate;

public record ExpiryAlertDto(
        Long lotId,
        Long medicineId,
        String lotNumber,
        LocalDate expiryDate,
        int available
) {}
