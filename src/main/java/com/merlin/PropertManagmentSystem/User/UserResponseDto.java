package com.merlin.PropertManagmentSystem.User;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        Role role
) {
}
