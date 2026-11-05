package com.merlin.PropertManagmentSystem.Lease;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.PaymentAllocation.PaymentAllocation;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
public class Lease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "tenantId"
    )
    @JsonBackReference
    private Tenant tenant;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "unitID"
    )
    private Unit unit;
    private LocalDate StartDate;
    private LocalDate EndDate;
    private BigDecimal rentAtSigning;
    @Enumerated(EnumType.STRING)
    private LeaseStatus leaseStatus;
    private BigDecimal depositAmount;
    private BigDecimal depositPaid;
    @Enumerated(EnumType.STRING)
    private DepositStatus depositStatus;
    private LocalDateTime depositRefundedAt;
    private int preferredBillingDay;
    private boolean isApproved;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "approvedBy"
    )
    @JsonBackReference
    private User approvedBy;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name="createdById"
    )
    @JsonBackReference
    private User createdBy;

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

    @OneToMany(
            mappedBy = "lease"
    )
    @JsonManagedReference
    private List<TransactionLog> transactionLogs;

    @OneToMany(
            mappedBy = "lease"
    )
    @JsonManagedReference
    private List<PaymentAllocation> paymentAllocations;

    public Lease() {
    }

    public Lease(Tenant tenant, Unit unit, LocalDate startDate, BigDecimal rentAtSigning) {
        this.tenant = tenant;
        this.unit = unit;
        StartDate = startDate;
        this.rentAtSigning = rentAtSigning;
    }
}
