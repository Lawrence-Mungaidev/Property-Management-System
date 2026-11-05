package com.merlin.PropertManagmentSystem.Credit;

import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
public class CreditService {

    private CreditRepository creditRepository;


    public void saveCredit(Tenant tenant, TransactionLog transactionLog) {

        if(creditRepository.existsByTransactionLogAndCreditStatusNot(transactionLog, CreditStatus.REVERSED)){
            log.info("The credit already exists");
        }

        Credit credit = new Credit();

        credit.setAmount(transactionLog.getAmount());
        credit.setReversed(false);
        credit.setTenant(tenant);
        credit.setCreatedAt(LocalDate.now());
        credit.setTransactionLog(transactionLog);
        credit.setCreditStatus(CreditStatus.UNUSED);

        BigDecimal totalAmount = creditRepository.totalCredit(tenant);

        credit.setTotalAmount(totalAmount);

        creditRepository.save(credit);
    }
}
