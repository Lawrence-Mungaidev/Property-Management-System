package com.merlin.PropertManagmentSystem.RoleChangeRequest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RoleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "organisationId"
    )
    @JsonBackReference
    private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "targetUserId"
    )
    @JsonBackReference
    private User targetUser;
    @Enumerated(EnumType.STRING)
    private Role currentRole;
    @Enumerated(EnumType.STRING)
    private Role proposedRole;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "initiatedById"
    )
    @JsonBackReference
    private User initiatedBy;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "rejectedById"
    )
    @JsonBackReference
    private User rejectedBy;
    private LocalDateTime rejectedOn;
    private String reason;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "completedById"
    )
    @JsonBackReference
    private User approvedBy;
    private LocalDateTime approvedOn;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private RoleChangeStatus status;

    public RoleChangeRequest() {
    }

    public RoleChangeRequest(User targetUser, Role proposedRole, User initiatedBy, String reason, User completedBy) {
        this.targetUser = targetUser;
        this.proposedRole = proposedRole;
        this.initiatedBy = initiatedBy;
        this.reason = reason;
        this.approvedBy = completedBy;
    }
}
