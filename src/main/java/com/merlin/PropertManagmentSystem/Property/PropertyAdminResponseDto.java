package com.merlin.PropertManagmentSystem.Property;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PropertyAdminResponseDto(
        Long Id,
        String ownedBy,
        String propertyName,
        String address,
        String town,
        LocalDateTime createdOn,
        int numberOfUnit


) {
}
