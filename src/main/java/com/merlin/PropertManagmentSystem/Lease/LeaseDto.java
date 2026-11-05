package com.merlin.PropertManagmentSystem.Lease;


import java.time.LocalDate;
import jakarta.validation.constraints.*;


public record LeaseDto(

        @NotNull(message = "Tenant is required")
        @Positive(message = "Tenant ID must be a positive number")
        Long tenant,

        @NotNull(message = "Unit is required")
        @Positive(message = "Unit ID must be a positive number")
        Long unit,

        @NotNull(message = "Start date is required")
        LocalDate startDate
) {}
