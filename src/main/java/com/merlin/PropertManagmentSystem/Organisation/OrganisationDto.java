package com.merlin.PropertManagmentSystem.Organisation;

import jakarta.validation.constraints.*;

public record OrganisationDto(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,

        @NotBlank(message = "Address is required")
        @Size(min = 3, max = 255, message = "Address must be 3-255 characters")
        String address,

        @NotBlank(message = "Town is required")
        @Size(min = 2, max = 50, message = "Town must be 2-50 characters")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "Town contains invalid characters")
        String town,

        @NotBlank(message = "KRA PIN is required")
        @Pattern(regexp = "^[AP]\\d{9}[A-Z]$",
                message = "KRA PIN must look like A123456789B or P123456789B")
        String kraPin
) {}
