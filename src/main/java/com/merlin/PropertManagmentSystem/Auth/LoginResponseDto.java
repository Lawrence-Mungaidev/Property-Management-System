package com.merlin.PropertManagmentSystem.Auth;

import com.merlin.PropertManagmentSystem.User.Role;

public record LoginResponseDto(
        String token,
        String firstName,
        boolean mustChangePassword,
        Role role,
        boolean needsNewChairman,
        boolean isSubscribed
) {
}
