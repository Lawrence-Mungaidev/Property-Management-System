package com.merlin.PropertManagmentSystem.TransactionLog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionLogResponseDto(
        Long id,
        String mpesaTransactionCode,
        String accountNumberUser,
        String unitName,
        BigDecimal amount,
        String MSISDN,
        String payerName,
        LocalDateTime transactionTime,
        LocalDateTime receivedDate,
        LocalDateTime processedDate
) {
}
