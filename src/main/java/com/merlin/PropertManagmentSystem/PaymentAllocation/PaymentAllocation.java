package com.merlin.PropertManagmentSystem.PaymentAllocation;




import com.fasterxml.jackson.annotation.JsonBackReference;
import com.merlin.PropertManagmentSystem.Credit.Credit;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.RentRecord.RentRecord;
import jakarta.persistence.*;
        import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "creditId"
    )
    @JsonBackReference
    private Credit credit;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "rentRecordId"
    )
    @JsonBackReference
    private RentRecord rentRecord;
    private LocalDateTime allocationDate;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private AllocationType allocationType;
    private boolean reversed;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "leaseId"
    )
    @JsonBackReference
    private Lease lease;

    public PaymentAllocation() {
    }
    public PaymentAllocation(Credit credit, RentRecord rentRecord, BigDecimal amount) {
        this.credit = credit;
        this.rentRecord = rentRecord;
        this.amount = amount;
        this.allocationDate = LocalDateTime.now();
    }
}

