package com.merlin.PropertManagmentSystem.Lease;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaseResponseDto(
        Long Id,
        Long tenant,
        Long unit,
        LocalDate startDate,
        LocalDate EndDate,
        BigDecimal rentAtSigning
) {
}
