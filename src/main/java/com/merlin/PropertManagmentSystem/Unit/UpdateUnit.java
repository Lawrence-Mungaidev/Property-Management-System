package com.merlin.PropertManagmentSystem.Unit;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateUnit(

        @Size(max = 20, message = "Unit number must be at most 20 characters")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9\\-/ ]*$",
                message = "Unit number can only contain letters, digits, spaces, - and /")
        String unitNumber,


        @Size(min = 3, max = 30, message = "Account number must be 3-30 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$",
                message = "Account number can only contain letters and digits")
        String accountNumber,


        @Positive(message = "Monthly rent must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Monthly rent: max 10 digits and 2 decimals")
        BigDecimal monthlyRent,


        @PositiveOrZero(message = "Deposit cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Deposit: max 10 digits and 2 decimals")
        BigDecimal deposit,

        UnitType unitType
) {
}
