package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.Property.PropertyResponseDto;

import java.util.List;

public record PropertyAsignResponseDto(
        Long id,
        PropertyResponseDto properties,
        Long careTaker
) {
}
