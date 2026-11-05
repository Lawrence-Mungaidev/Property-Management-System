package com.merlin.PropertManagmentSystem.Unit;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UnitResponseDto(
        Long id,
        String unitNumber,
        String accountNumber,
        BigDecimal monthlyRent,
        BigDecimal deposit,
        LocalDateTime createdAt,
        Boolean Active,
        Boolean isVacant
) {
}
