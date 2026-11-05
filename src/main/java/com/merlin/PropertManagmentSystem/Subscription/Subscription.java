package com.merlin.PropertManagmentSystem.Subscription;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    private BigDecimal subscriptionAmount;
    private String phoneNumberPaid;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private  SubscriptionStatus subscriptionStatus;
    private boolean isSubscribed;
    private String mpesaReference;
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "subscriptionMadeById"
    )
    @JsonBackReference
    private User subscriptionMadeBy;


    public Subscription() {
    }

    public Subscription(BigDecimal subscriptionAmount) {
        this.subscriptionAmount = subscriptionAmount;
    }
}
