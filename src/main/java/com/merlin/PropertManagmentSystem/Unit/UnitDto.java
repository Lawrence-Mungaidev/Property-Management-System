package com.merlin.PropertManagmentSystem.Unit;



import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UnitDto(

        @NotBlank(message = "Unit number is required")
        @Size(max = 20, message = "Unit number must be at most 20 characters")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9\\-/ ]*$",
                message = "Unit number can only contain letters, digits, spaces, - and /")
        String unitNumber,

        @NotBlank(message = "Account number is required")
        @Size(min = 3, max = 30, message = "Account number must be 3-30 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$",
                message = "Account number can only contain letters and digits")
        String accountNumber,

        @NotNull(message = "Monthly rent is required")
        @Positive(message = "Monthly rent must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Monthly rent: max 10 digits and 2 decimals")
        BigDecimal monthlyRent,

        @NotNull(message = "Deposit is required")
        @PositiveOrZero(message = "Deposit cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Deposit: max 10 digits and 2 decimals")
        BigDecimal deposit,
        @NotNull( message = "Unit type required")
        UnitType unitType
) {}
