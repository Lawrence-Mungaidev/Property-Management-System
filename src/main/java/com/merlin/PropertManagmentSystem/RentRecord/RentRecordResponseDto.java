package com.merlin.PropertManagmentSystem.RentRecord;

import java.math.BigDecimal;

public record RentRecordResponseDto(
        Long Id,
        BigDecimal amountDue,
        BigDecimal amountPaid,
        RentStatus rentStatus,
        Long unitId
) {
}
