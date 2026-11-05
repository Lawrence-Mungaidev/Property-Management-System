package com.merlin.PropertManagmentSystem.PaymentDispute;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentDispute {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "transactionLogId"
    )
    private TransactionLog transactionLog;

    @ManyToOne(fetch = FetchType.LAZY )
    private Lease originalLease;
    @ManyToOne(fetch = FetchType.LAZY )
    private Lease correctLease;
    private PaymentDisputeType paymentDisputeType;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "resolvedById"
    )
    @JsonBackReference
    private User resolvedBy;
    private LocalDateTime resolvedAt;
    private String note;
    private LocalDateTime createdAt;

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

    public PaymentDispute() {
    }

    public PaymentDispute(TransactionLog transactionLog, Lease originalLease, Lease correctLease, PaymentDisputeType paymentDisputeType, String note) {
        this.transactionLog = transactionLog;
        this.originalLease = originalLease;
        this.correctLease = correctLease;
        this.paymentDisputeType = paymentDisputeType;
        this.note = note;
        this.resolvedAt = LocalDateTime.now();
  }
}
