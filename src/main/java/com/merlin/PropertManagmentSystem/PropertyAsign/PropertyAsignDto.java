package com.merlin.PropertManagmentSystem.PropertyAsign;

import jakarta.validation.constraints.*;

public record PropertyAsignDto(

        @NotNull(message = "Property is required")
        @Positive(message = "Property ID must be a positive number")
        Long propertyId,

        @NotNull(message = "Caretaker is required")
        @Positive(message = "Caretaker ID must be a positive number")
        Long careTaker
) {}
