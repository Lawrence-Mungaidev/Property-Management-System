package com.merlin.PropertManagmentSystem.PaymentAllocation;

import com.merlin.PropertManagmentSystem.Credit.Credit;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.RentRecord.RentRecord;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PaymentAllocationService {

    private  final PaymentAllocationRepository paymentAllocationRepository;


    public void addAllocation(RentRecord r, Credit credit, AllocationType allocationType, BigDecimal amount, Lease lease ) {
        PaymentAllocation pa = new PaymentAllocation();

        pa.setCredit(credit);
        pa.setLease(lease);
        pa.setRentRecord(r);
        pa.setAmount(amount);
        pa.setAllocationDate(LocalDateTime.now(ZoneId.of("Africa/Nairobi")));
        pa.setAllocationType(allocationType);
        pa.setAllocationDate(LocalDateTime.now());

        paymentAllocationRepository.save(pa);
    }

}
