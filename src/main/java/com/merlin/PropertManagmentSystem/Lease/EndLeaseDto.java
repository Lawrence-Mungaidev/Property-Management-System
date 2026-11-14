package com.merlin.PropertManagmentSystem.Lease;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record EndLeaseDto(
        @NotNull @PositiveOrZero BigDecimal depositRefunded,
        @PositiveOrZero BigDecimal depositDeductions,
        String deductionReason
) {}
