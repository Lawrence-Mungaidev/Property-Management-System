package com.merlin.PropertManagmentSystem.Subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubscriptionDto(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^(?:\\+254|0)[17]\\d{8}$",
                message = "Enter a valid Kenyan number e.g. 0712345678, 0112345678 or +254712345678")
        String phoneNumber
) {
}
