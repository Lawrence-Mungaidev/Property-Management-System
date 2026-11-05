package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PropertyAsign {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "propertyId"
    )
    @JsonBackReference
    private Property property;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "caretakerId"
    )
    @JsonBackReference
    private User careTaker;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "assignedBbyId")
    @JsonBackReference
    private User assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime unAssignedAt;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "unassignedById")
    @JsonBackReference
    private User unassignedBy;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "organisationId"
    )
    @JsonBackReference
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "landlordId"
    )
    @JsonBackReference
    private User landlord;

    public PropertyAsign() {
    }

    public PropertyAsign(Property property, User careTaker) {
        this.property = property;
        this.careTaker = careTaker;
        this.assignedAt = LocalDateTime.now();
    }
}
