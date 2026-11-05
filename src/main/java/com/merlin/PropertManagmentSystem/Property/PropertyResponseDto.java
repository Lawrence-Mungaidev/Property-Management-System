package com.merlin.PropertManagmentSystem.Property;

public record PropertyResponseDto(
        Long propertyId,
        String propertyName,
        String address,
        String town
) {
}
