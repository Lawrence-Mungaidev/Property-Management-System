package com.merlin.PropertManagmentSystem.Tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantUpdateDto(


        @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "First name contains invalid characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "Last name contains invalid characters")
        String lastName,


        @Pattern(regexp = "^\\d{7,8}$",
                message = "National ID must be 7 or 8 digits")
        String nationalId,

        @Size(max = 254, message = "Email is too long")
        @Email(message = "Email format is invalid")
        String email,


        @Pattern(regexp = "^(?:\\+254|0)[17]\\d{8}$",
                message = "Enter a valid Kenyan number e.g. 0712345678, 0112345678 or +254712345678")
        String phoneNumber
) {
}
