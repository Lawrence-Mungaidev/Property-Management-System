package com.merlin.PropertManagmentSystem.Tenant;


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
@RequestMapping("/api/tenants")
@RestController
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/createTenant/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'CARETAKER')")
    public ResponseEntity<TenantResponseDto> createTenant(
            @Valid @RequestBody TenantDto dto,
            @PathVariable("propertyId") Long propertyId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createTenant(dto, authenticatedUser, propertyId));
    }

    @PatchMapping("/updateTenant/{tenantId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'CARETAKER')")
    public ResponseEntity<TenantResponseDto> updateTenant(
            @PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody TenantDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(tenantService.updateTenant(dto, authenticatedUser, tenantId));
    }

    @GetMapping("/getAllTenants")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'CARETAKER')")
    public ResponseEntity<List<TenantResponseDto>> getAllTenants(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(tenantService.getAllTenants(authenticatedUser));
    }

    @GetMapping("/getTenantById/{tenantId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<TenantResponseDto> getTenantById(
            @PathVariable("tenantId") Long tenantId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(tenantService.getTenantById(tenantId, authenticatedUser));
    }
}
