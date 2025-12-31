package com.pharmacy.customer_service.dto;

import java.time.LocalDate;

public record UpdateCustomerRequest(
        String fullName,
        String email,
        String address,
        LocalDate dateOfBirth,
        String gender,
        Integer loyaltyPoints,
        String notes
) {}
