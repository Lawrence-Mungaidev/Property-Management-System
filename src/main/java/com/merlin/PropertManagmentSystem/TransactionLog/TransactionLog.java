package com.merlin.PropertManagmentSystem.TransactionLog;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Credit.Credit;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
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
@Getter
@Setter
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String transactionCode;
    private String accountNumber;
    private BigDecimal amount;
    private String MSISDN;
    private String payerName;
    private LocalDateTime transactionTime;
    private LocalDateTime  receivedAt;
    private LocalDateTime  processedDate;
    @Enumerated(EnumType.STRING)
    private TransactionLogStatus TransactionStatus;
    private String payBill;
    @Enumerated(EnumType.STRING)
    private Source transactionSource;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "unitId"
    )
    @JsonBackReference
    private Unit unit ;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "leaseId"
    )
    private Lease lease;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "landlordId"
    )
    @JsonBackReference
    private User landLord;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "organisationId"
    )
    @JsonBackReference
    private Organisation organisation;

    @OneToMany(
            mappedBy = "transactionLog"
    )
    @JsonManagedReference
    private List<Credit> credit;



    public TransactionLog() {
    }

    public TransactionLog(String transactionCode, String accountNumber, BigDecimal amount, String MSISDN, String payerName) {
        this.transactionCode = transactionCode;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.MSISDN = MSISDN;
        this.payerName = payerName;

    }
}
