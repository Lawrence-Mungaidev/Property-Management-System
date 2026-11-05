package com.merlin.PropertManagmentSystem.NotificationBatch;

public record NotificationBatchResponseDto(
        Long Id,
        String message,
        Long initiatedBy,
        NBType nbType
) {
}
