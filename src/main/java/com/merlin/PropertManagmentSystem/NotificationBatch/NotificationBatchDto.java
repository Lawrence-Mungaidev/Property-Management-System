package com.merlin.PropertManagmentSystem.NotificationBatch;

import com.merlin.PropertManagmentSystem.Notification.NotificationType;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NotificationBatchDto(
        @NotNull(message = "Property is required")
        @Positive(message = "Property ID must be a positive number")
        Long propertyId,

        @NotBlank(message = "Message is required")
        @Size(max = 480, message = "Message must be at most 480 characters")
        String message,

        @NotNull(message = "Notification type is required")
        NotificationType notificationType,

        @NotNull(message = "Batch type is required")
        NBType nbType
) {
}
