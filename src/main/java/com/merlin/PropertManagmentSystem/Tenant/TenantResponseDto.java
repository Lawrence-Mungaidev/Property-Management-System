package com.merlin.PropertManagmentSystem.Tenant;

import java.math.BigDecimal;

public record TenantResponseDto (
        Long id,
        String firstName,
        String lastName,
        String nationalId,
        String phoneNumber,
        BigDecimal walletBalance
) {
}
