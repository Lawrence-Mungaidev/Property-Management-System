package com.merlin.PropertManagmentSystem.Property;

import jakarta.validation.constraints.*;

public record PropertyDto(

        @NotBlank(message = "Property name is required")
        @Size(min = 2, max = 100, message = "Property name must be 2-100 characters")
        String propertyName,

        @NotBlank(message = "Address is required")
        @Size(min = 3, max = 255, message = "Address must be 3-255 characters")
        String address,

        @NotBlank(message = "Town is required")
        @Size(min = 2, max = 50, message = "Town must be 2-50 characters")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "Town contains invalid characters")
        String town,

        @NotNull(message = "Preferred billing day is required")
        @Min(value = 1, message = "Billing day must be at least 1")
        @Max(value = 30, message = "Billing day must be 28 or less")
        Integer preferredBillingDay
) {}
