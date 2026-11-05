package com.merlin.PropertManagmentSystem.NotificationBatch;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Notification.Notification;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class NotificationBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private NBType nbType;
    private String message;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "initiatedById"
    )
    @JsonBackReference
    private User initiatedBy;
    private LocalDateTime sentAt;
    @OneToMany(
            mappedBy = "notificationBatch"
    )
    @JsonManagedReference
    private List<Notification> notifications;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "landlordId"
    )
    @JsonBackReference
    private User belongsToLandlord;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "organisationId"
    )
    @JsonBackReference
    private Organisation belongsToOrganisation;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "adminId"
    )
    @JsonBackReference
    private User belongsToAdmin;


    public NotificationBatch() {
    }

    public NotificationBatch(String message, User initiatedBy) {
        this.message = message;
        this.initiatedBy = initiatedBy;
    }
}
