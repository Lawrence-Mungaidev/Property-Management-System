package com.merlin.PropertManagmentSystem.Lease;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OnboardLeaseDto(

        @NotNull(message = "Tenant is required")
        @Positive(message = "Tenant ID must be a positive number")
        Long tenantId,

        @NotNull(message = "Unit is required")
        @Positive(message = "Unit ID must be a positive number")
        Long unitId,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "Deposit amount paid is required")
        @PositiveOrZero(message = "Deposit amount paid cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Deposit amount: max 10 digits and 2 decimals")
        BigDecimal depositAmountPaid,

        // Integer instead of int so a missing value fails validation instead of defaulting to 0
        @NotNull(message = "Unpaid cycles is required")
        @PositiveOrZero(message = "Unpaid cycles cannot be negative")
        @Max(value = 24, message = "Unpaid cycles cannot exceed 24")
        Integer unpaidCycles,


        @PositiveOrZero(message = "Partial rent paid cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Partial rent: max 10 digits and 2 decimals")
        BigDecimal rentPartialAmountPaid
) {}
