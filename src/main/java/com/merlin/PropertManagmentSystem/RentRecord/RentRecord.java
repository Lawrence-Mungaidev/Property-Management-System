package com.merlin.PropertManagmentSystem.RentRecord;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.PaymentAllocation.PaymentAllocation;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class RentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "leaseId"
    )
    @JsonBackReference
    private Lease lease;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodMonth;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    @Enumerated(EnumType.STRING)
    private RentStatus rentStatus;
    private boolean declared;

    @OneToMany(
            mappedBy = "rentRecord"
    )
    @JsonManagedReference
    List<PaymentAllocation> paymentAllocations;

    public RentRecord() {
    }

    public RentRecord(BigDecimal amountDue, BigDecimal amountPaid) {
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
    }
}
