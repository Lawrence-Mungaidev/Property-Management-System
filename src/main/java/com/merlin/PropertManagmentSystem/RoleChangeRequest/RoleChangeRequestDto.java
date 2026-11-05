package com.merlin.PropertManagmentSystem.RoleChangeRequest;

import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;

public record RoleChangeRequestDto(
        Long targetUser,
        Role proposedRole,
        String reason
) {
}
