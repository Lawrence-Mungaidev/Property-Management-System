package com.merlin.PropertManagmentSystem.TransactionLog;

import org.springframework.stereotype.Component;

@Component
public class TransactionLogMapper {
   

    public TransactionLogResponseDto toTenantResponseDto(TransactionLog transactionLog) {
        return new TransactionLogResponseDto(transactionLog.getId(),transactionLog.getPayerName(), transactionLog.getAccountNumber(), transactionLog.getUnit().getUnitNumber(),transactionLog.getAmount(),transactionLog.getMSISDN(),transactionLog.getPayerName(),
                transactionLog.getTransactionTime(), transactionLog.getReceivedAt(), transactionLog.getProcessedDate() );
    }
}
