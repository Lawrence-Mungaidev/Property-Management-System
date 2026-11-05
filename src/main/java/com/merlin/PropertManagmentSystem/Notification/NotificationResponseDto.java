package com.merlin.PropertManagmentSystem.Notification;

import java.time.LocalDateTime;

public record NotificationResponseDto (
        Long id,
        String message,
        Status status,
        NotificationType notificationType,
        channel channel,
        LocalDateTime sentAt,
        Long batchId,
        String receiverName,
        String receiverType

){
}
