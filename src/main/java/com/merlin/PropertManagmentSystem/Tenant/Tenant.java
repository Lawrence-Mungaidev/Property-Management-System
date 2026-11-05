package com.merlin.PropertManagmentSystem.Tenant;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Credit.Credit;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Notification.Notification;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String nationalId;
    @Column(unique = true)
    private String phoneNumber;
    private BigDecimal walletBalance;
    @OneToMany(
            mappedBy = "tenant"
    )
    @JsonManagedReference
    private List<Credit> credit;
    @Column(unique = true)
    private String email;

    @OneToMany(
            mappedBy = "receiverTenant"
    )
    @JsonManagedReference
    List<Notification> notifications;

    @OneToMany(
            mappedBy = "tenant"
    )
    @JsonManagedReference
    private List<Lease>  leases;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "landlordId")
    @JsonBackReference
    private User landlord;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "organisationId")
    @JsonBackReference
    private Organisation organisation;



    
    public Tenant() {
    }

    public Tenant(String firstName, String lastName, String nationalId, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.phoneNumber = phoneNumber;
        this.walletBalance = new BigDecimal(0);
    }
}
