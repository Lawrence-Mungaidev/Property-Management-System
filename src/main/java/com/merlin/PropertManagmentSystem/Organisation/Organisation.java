package com.merlin.PropertManagmentSystem.Organisation;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Notification.Notification;
import com.merlin.PropertManagmentSystem.PaybillService.PayBillStatus;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.RoleChangeRequest.RoleChangeRequest;
import com.merlin.PropertManagmentSystem.Subscription.Subscription;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String town;
    private String kraPin;
    private boolean needsChairMan;
    private String PayBill;
    @Enumerated(EnumType.STRING)
    private PayBillStatus payBillStatus;
    private boolean isOrganisationSubscribed;
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY )
    private User Chairman;

    @OneToOne(fetch = FetchType.LAZY )
    private User Secretary;

    @OneToOne(fetch = FetchType.LAZY )
    private User Treasurer;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<User> members;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<PropertyAsign> propertyAssigns;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<Notification> notifications;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<Tenant> organisationTenants;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<Property> properties;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<Lease> leasesList;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<RoleChangeRequest> roleChangeRequests;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<Subscription> organisationSubscriptions;

    @OneToMany(
            mappedBy = "organisation"
    )
    @JsonManagedReference
    private List<TransactionLog> organisationTransactionLogs;

    public Organisation() {
    }

    public Organisation(String name, String address, String town, String kraPin,User chairMan, User secretary, User treasurer) {
        this.name = name;
        this.address = address;
        this.town = town;
        this.kraPin = kraPin;
        this.Chairman = chairMan;
        Secretary = secretary;
        Treasurer = treasurer;
    }
}
