package com.merlin.PropertManagmentSystem.Credit;

import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit,Long> {



        List<Credit> findByTenantAndCreditStatusInOrderByCreatedAtAsc(
                Tenant tenant, Collection<CreditStatus> statuses);

        boolean existsByTransactionLogAndCreditStatusNot(TransactionLog transactionLog, CreditStatus creditStatus);


        @Query("""
            SELECT COALESCE(SUM(c.remainingAmount), 0)
            FROM Credit c
            WHERE c.tenant = :tenant
              AND c.creditStatus IN (
                  com.merlin.PropertManagmentSystem.Credit.CreditStatus.UNUSED,
                  com.merlin.PropertManagmentSystem.Credit.CreditStatus.PARTIALLY_USED)
            """)
        BigDecimal totalCredit(@Param("tenant") Tenant tenant);

        Optional<Credit> findByTransactionLog(TransactionLog log);

}
