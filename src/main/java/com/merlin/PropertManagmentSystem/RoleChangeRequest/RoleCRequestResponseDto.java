package com.merlin.PropertManagmentSystem.RoleChangeRequest;

import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;

import java.time.LocalDateTime;

public record RoleCRequestResponseDto(
        Long id,
        RoleChangeStatus status,
        String initiatedByName,
        String targetUserName,
        String approvedByName,
        LocalDateTime approvedAt,
        String rejectedByName,
        LocalDateTime rejectedAt,
        String reason,
        Role previousRole,
        Role proposedRole
) {
}
