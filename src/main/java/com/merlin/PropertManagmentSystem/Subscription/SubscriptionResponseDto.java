package com.merlin.PropertManagmentSystem.Subscription;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponseDto(
        Long subscriptionId,
        String phoneNumberPaid,
        LocalDate startDate,
        LocalDate endDate,
        Long subscribedBy,
        LocalDateTime createdAt,
        SubscriptionStatus status
) {
}
