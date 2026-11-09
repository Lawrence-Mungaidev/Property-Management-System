package com.merlin.PropertManagmentSystem.Property;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String propertyName;
    private String address;
    private String town;
    private LocalDateTime createdOn;
    private int preferredBillingDay;


    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "LandlordId"
    )
    @JsonBackReference
    private User landlord;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "OrganisationId"
    )
    @JsonBackReference
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "createdById")
    @JsonBackReference
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "updatedById")
    @JsonBackReference
    private User updatedBy;
    private LocalDateTime updatedOn;
    private boolean active;

    @OneToMany(
            mappedBy = "property"
    )
    @JsonManagedReference
    private List<PropertyAsign> propertyAssignments;
    @OneToMany(
            mappedBy = "property"
    )
    @JsonManagedReference
    private List<Unit> units;
    @OneToMany(
            mappedBy = "property"
    )
    @JsonManagedReference
    private List<Tenant>  tenants;

    public Property() {
    }

    public Property(String propertyName, String address, String town) {
        this.propertyName = propertyName;
        this.address = address;
        this.town = town;
    }
}
