package com.merlin.PropertManagmentSystem.Unit;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
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
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "propertyId"
    )
    @JsonBackReference
    private Property property;
    private String unitNumber;
    @Column( unique = true)
    private String accountNumber;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;
    private LocalDateTime createdAt;
    private boolean active;
    private boolean isVacant;
    @Enumerated(EnumType.STRING)
    private UnitType unitType;


    @OneToMany(
            mappedBy = "unit"
    )
    @JsonManagedReference
    private List<Lease> leases;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(
            name = "createdById"
    )
    @JsonBackReference
    private User createdBy;

    @OneToMany(
            mappedBy = "unit"
    )
    @JsonManagedReference
    private List<TransactionLog> transactionLog;

    public Unit() {
    }

    public Unit(String unitNumber, String accountNumber, BigDecimal monthlyRent, BigDecimal depositAmount) {
        this.unitNumber = unitNumber;
        this.accountNumber = accountNumber;
        this.monthlyRent = monthlyRent;
        this.depositAmount = depositAmount;
    }
}
