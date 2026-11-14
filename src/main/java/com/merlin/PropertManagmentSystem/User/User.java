package com.merlin.PropertManagmentSystem.User;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Notification.Notification;
import com.merlin.PropertManagmentSystem.NotificationBatch.NotificationBatch;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.PaybillService.PayBillStatus;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.RoleChangeRequest.RoleChangeRequest;
import com.merlin.PropertManagmentSystem.Subscription.Subscription;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.sql.results.graph.Fetch;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Setter
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isActive;
    private LocalDateTime createdDate;
    private boolean mustChangePassword;
    private String payBill;
    @Enumerated(EnumType.STRING)
    private PayBillStatus payBillStatus;
    private boolean isLandlordSubscribed;


    @OneToMany(
            mappedBy = "landlord"
    )
    @JsonManagedReference
    private List<Property>  properties;

    @OneToOne(fetch = FetchType.LAZY )
    private Organisation chairManOrganisation;

    @OneToOne(fetch = FetchType.LAZY )
    private Organisation secretaryOrganisation;

    @OneToOne(fetch = FetchType.LAZY )
    private Organisation treasurerOrganisation;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "propertyId"
    )
    @JsonBackReference
    private Property  property;

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
    @JsonBackReference
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(
            mappedBy = "createdBy"
    )
    @JsonManagedReference
    private List<Property> propertiesCreatedBy;

    @OneToMany(
            mappedBy = "updatedBy"
    )
    @JsonManagedReference
    private List<Property> propertiesUpdatedBy;

    @OneToMany(
            mappedBy = "careTaker"
    )
    @JsonManagedReference
    private List<PropertyAsign> propertyAssigns;

    @OneToMany(
            mappedBy = "assignedBy"
    )
    @JsonManagedReference
    private List<PropertyAsign> assignedPropertyBy;

    @OneToMany(
            mappedBy = "unassignedBy"
    )
    @JsonManagedReference
    private List<PropertyAsign> propertyAssignsUnassignedBy;

    @OneToMany(
            mappedBy = "landlord"
    )
    @JsonManagedReference
    private List<PropertyAsign> propertyAssignsLandlord;

    @OneToMany(
            mappedBy = "receiver"
    )
    @JsonManagedReference
    private List<Notification> notification;

    @OneToMany(
            mappedBy = "landlord"
    )
    @JsonManagedReference
    private List<Notification> landlordsNotifications;

    @OneToMany(
            mappedBy = "landlord"
    )
    @JsonManagedReference
    private List<Tenant> landlordsTenants;

    @OneToMany(
            mappedBy = "landlord"
    )
    @JsonManagedReference
    private List<Lease> leaseList;

    @OneToMany(
            mappedBy = "targetUser"
    )
    @JsonManagedReference
    private List<RoleChangeRequest> roleChangeRequestsTargets;

    @OneToMany(
            mappedBy = "rejectedBy"
    )
    @JsonManagedReference
    private List<RoleChangeRequest> roleChangeRequestsRejectedBy;

    @OneToMany(
            mappedBy = "initiatedBy"
    )
    @JsonManagedReference
    private List<RoleChangeRequest> roleChangeRequestsInitiated;

    @OneToMany(
            mappedBy = "approvedBy"
    )
    @JsonManagedReference
    private List<RoleChangeRequest> roleChangeRequestsApprovedBy;

    @OneToMany(
            mappedBy = "landlord"
    )
    @JsonManagedReference
    private List<Subscription> landlordSubscriptions;

    @OneToMany(
            mappedBy = "belongsToAdmin"
    )
    @JsonManagedReference
    private List<NotificationBatch> adminNotificationBatches;

    @OneToMany(
            mappedBy = "Admin"
    )
    @JsonManagedReference
    private List<Notification> adminsNotifications;

    @OneToMany(
            mappedBy = "subscriptionMadeBy"
    )
    @JsonManagedReference
    private List<Subscription> subscriptionsMadeByList;

    @OneToMany(
            mappedBy = "createdBy"
    )
    @JsonManagedReference
    private List<Lease> leaseCreatedBy;

    @OneToMany(
            mappedBy = "landLord"
    )
    @JsonManagedReference
    private List<TransactionLog> landLordTransactionLogs;

    public User() {
    }

    public User(String firstName, String lastName, String email,String phoneNumber ,String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
        this.isActive = true;
        this.createdDate = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
