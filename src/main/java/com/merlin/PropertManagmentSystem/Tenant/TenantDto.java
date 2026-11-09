package com.merlin.PropertManagmentSystem.Tenant;

import jakarta.validation.constraints.*;

public record TenantDto(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "First name contains invalid characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "Last name contains invalid characters")
        String lastName,

        @NotBlank(message = "National ID is required")
        @Pattern(regexp = "^\\d{7,8}$",
                message = "National ID must be 7 or 8 digits")
        String nationalId,

        @Size(max = 254, message = "Email is too long")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^(?:\\+254|0)[17]\\d{8}$",
                message = "Enter a valid Kenyan number e.g. 0712345678, 0112345678 or +254712345678")
        String phoneNumber,
        @NotNull(message = "property is required")
        Long propertyId
) {}
