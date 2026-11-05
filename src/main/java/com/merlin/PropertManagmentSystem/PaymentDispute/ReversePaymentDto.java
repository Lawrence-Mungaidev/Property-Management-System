package com.merlin.PropertManagmentSystem.PaymentDispute;

import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;

public record ReversePaymentDto(
        Long transactionLogId,
        Long tenantId,
        String note
) {
}
