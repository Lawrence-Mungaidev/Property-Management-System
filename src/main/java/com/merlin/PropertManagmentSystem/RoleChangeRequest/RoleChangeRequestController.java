package com.merlin.PropertManagmentSystem.RoleChangeRequest;


import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/roleChangeRequests")
@RestController
public class RoleChangeRequestController {

    private final RoleChangeRequestService roleChangeRequestService;

    @PostMapping("/chairmanChangeRole")
    @PreAuthorize("hasRole('CHAIRMAN')")
    public ResponseEntity<RoleCRequestResponseDto> chairmanChangeRole(
            @Valid @RequestBody RoleChangeRequestDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleChangeRequestService.CMRoleChangeRequest(authenticatedUser, dto));
    }

    @PostMapping("/secretaryRoleChangeRequest")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<RoleCRequestResponseDto> secretaryRoleChangeRequest(
            @Valid @RequestBody RoleChangeRequestDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleChangeRequestService.SCRoleChangeRequest(authenticatedUser, dto));
    }

    @PatchMapping("/approveRoleChange/{roleChangeRequestId}")
    @PreAuthorize("hasRole('TREASURER')")
    public ResponseEntity<Void> approveRoleChange(
            @PathVariable("roleChangeRequestId") Long roleChangeRequestId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        roleChangeRequestService.approveRoleChange(authenticatedUser, roleChangeRequestId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/rejectRoleChange/{roleChangeRequestId}")
    @PreAuthorize("hasRole('TREASURER')")
    public ResponseEntity<Void> rejectRoleChange(
            @PathVariable("roleChangeRequestId") Long roleChangeRequestId,
            @RequestParam("rejectReason") String rejectReason,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        roleChangeRequestService.rejectRoleChange(authenticatedUser, roleChangeRequestId, rejectReason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cancelRoleChange/{roleChangeRequestId}")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<Void> cancelRoleChange(
            @PathVariable("roleChangeRequestId") Long roleChangeRequestId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        roleChangeRequestService.cancelRoleChange(authenticatedUser, roleChangeRequestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getRoleChangeRequests")
    @PreAuthorize("hasAnyRole('CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<List<RoleCRequestResponseDto>> getRoleChangeRequests(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(roleChangeRequestService.RoleChangeRequestList(authenticatedUser));
    }

    @GetMapping("/getRoleChangeRequestById/{roleChangeRequestId}")
    @PreAuthorize("hasAnyRole('CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<RoleCRequestResponseDto> getRoleChangeRequestById(
            @PathVariable("roleChangeRequestId") Long roleChangeRequestId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(roleChangeRequestService.getRoleChangeRequest(authenticatedUser, roleChangeRequestId));
    }
}