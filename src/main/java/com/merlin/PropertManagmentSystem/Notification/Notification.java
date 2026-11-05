package com.merlin.PropertManagmentSystem.Notification;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.merlin.PropertManagmentSystem.NotificationBatch.NotificationBatch;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "receiverId"
    )
    @JsonBackReference
    private User receiver;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "receiverTenantId"
    )
    private Tenant  receiverTenant;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "notificationBatchId"
    )
    @JsonBackReference
    private NotificationBatch notificationBatch;
    private String message;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private channel channel;
    private LocalDateTime sentAt;
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    private boolean isRead;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "admin"
    )
    @JsonBackReference
    private User Admin;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "landlordId"
    )
    @JsonBackReference
    private User landlord;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "organisationId"
    )
    private Organisation organisation;



    public Notification() {
    }

    public Notification(String message, Status status, NotificationType notificationType) {
        this.message = message;
        this.status = status;
        this.notificationType = notificationType;
        this.sentAt = LocalDateTime.now();
    }
}
