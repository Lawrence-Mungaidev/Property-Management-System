package com.merlin.PropertManagmentSystem.RentRecord;

import java.math.BigDecimal;

public record RentRecordResponseDto(
        Long Id,

        BigDecimal monthlyRent,
        BigDecimal amountPaid,
        BigDecimal amountDue,
        RentStatus rentStatus,
        Long unitId
) {
}
