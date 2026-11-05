package com.merlin.PropertManagmentSystem.RoleChangeRequest;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RoleChangeRequestMapper {

    public RoleChangeRequest toRoleChangeRequest(RoleChangeRequestDto dto) {

        RoleChangeRequest roleChangeRequest = new RoleChangeRequest();
        roleChangeRequest.setProposedRole(dto.proposedRole());
        roleChangeRequest.setReason(dto.reason());
        roleChangeRequest.setCreatedAt(LocalDateTime.now());

        return roleChangeRequest;
    }

    public RoleCRequestResponseDto toRoleChangeRequestDto(RoleChangeRequest roleChangeRequest) {
        String initiatedBy = roleChangeRequest.getInitiatedBy().getFirstName() + " " + roleChangeRequest.getInitiatedBy().getLastName();

        String targetUser = roleChangeRequest.getTargetUser().getFirstName() + " " + roleChangeRequest.getTargetUser().getLastName();
        String approvedBy = roleChangeRequest.getApprovedBy().getFirstName() + " " + roleChangeRequest.getApprovedBy().getLastName();
        String rejectedBy = roleChangeRequest.getRejectedBy().getFirstName() + " " + roleChangeRequest.getRejectedBy().getLastName();

        return new RoleCRequestResponseDto(roleChangeRequest.getId(), roleChangeRequest.getStatus(),initiatedBy, targetUser, approvedBy, roleChangeRequest.getApprovedOn(),rejectedBy, roleChangeRequest.getRejectedOn(), roleChangeRequest.getReason(),roleChangeRequest.getCurrentRole(),roleChangeRequest.getProposedRole());
    }

}
