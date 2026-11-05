package com.merlin.PropertManagmentSystem.NotificationBatch;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationBatchMapper {

    public NotificationBatch toNotificationBatchDto(NotificationBatchDto dto) {
        NotificationBatch notificationBatch = new NotificationBatch();
        notificationBatch.setMessage(dto.message());
        notificationBatch.setSentAt(LocalDateTime.now());
        notificationBatch.setNbType(dto.nbType());

        return notificationBatch;
    }

    public NotificationBatchResponseDto toNotificationBatchResponseDto(NotificationBatch notificationBatch) {
        return new NotificationBatchResponseDto(notificationBatch.getId(),notificationBatch.getMessage(),notificationBatch.getInitiatedBy().getId(),notificationBatch.getNbType());
    }
}
