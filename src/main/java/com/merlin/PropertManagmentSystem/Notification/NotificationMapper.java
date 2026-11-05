package com.merlin.PropertManagmentSystem.Notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDto toNotificationResponseDto(Notification notification) {
        String receiverName = null;
        String receiverType = null;

        if(notification.getReceiver() != null){
            receiverName = notification.getReceiver().getFirstName() +" "+notification.getReceiver().getLastName();
            receiverType = notification.getReceiver().getRole().toString();
        }

        if(notification.getReceiverTenant() != null){
            receiverName = notification.getReceiverTenant().getFirstName() +" "+notification.getReceiverTenant().getLastName();
            receiverType = "TENANT";
        }

        return new NotificationResponseDto(notification.getId(),
                notification.getMessage(),
                notification.getStatus(),notification.getNotificationType(),notification.getChannel(), notification.getSentAt(),
                notification.getNotificationBatch().getId(), receiverName,receiverType);
    }
}
