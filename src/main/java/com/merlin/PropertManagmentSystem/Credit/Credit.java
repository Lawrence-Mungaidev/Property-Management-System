package com.merlin.PropertManagmentSystem.Credit;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.PaymentAllocation.PaymentAllocation;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Credit {

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
            name = "transactionLogId"
    )
    @JsonBackReference
    private TransactionLog transactionLog;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private boolean reversed;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "reversedFromId"
    )
    private Tenant reversedFrom;
    private LocalDate createdAt;
    @Enumerated(EnumType.STRING)
    private CreditStatus creditStatus;
    @Version
    private Long version;
    private BigDecimal totalAmount;

    @OneToMany(
            mappedBy = "credit"
    )
    @JsonManagedReference
    private List<PaymentAllocation> paymentAllocationList;

    public Credit() {
    }

    public Credit(Tenant tenant, TransactionLog transactionLog, BigDecimal amount, BigDecimal remainingAmount, boolean reversed, Tenant reversedFrom) {
        this.tenant = tenant;
        this.transactionLog = transactionLog;
        this.amount = amount;
        this.remainingAmount = remainingAmount;
        this.reversed = reversed;
        this.reversedFrom = reversedFrom;
        this.createdAt = LocalDate.now();
    }
}