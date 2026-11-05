package com.merlin.PropertManagmentSystem.PaymentDispute;

import java.time.LocalDateTime;

public record PDResponseDTO(
        Long id,
        Long transactionLogId,
        Long originalLeaseId,
        Long correctLeaseId,
        PaymentDisputeType paymentDisputeType,
        String resolvedBy,
        LocalDateTime resolvedAt,
        String note
) {
}
