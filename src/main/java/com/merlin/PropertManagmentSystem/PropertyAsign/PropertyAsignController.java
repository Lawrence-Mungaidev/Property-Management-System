package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/propertyAssigns")
@RestController
public class PropertyAsignController {

    private final PropertyAsignService propertyAsignService;

    @PostMapping("/assignProperty")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<PropertyAsignResponseDto> assignProperty(
            @Valid @RequestBody PropertyAsignDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyAsignService.assignProperty(dto, authenticatedUser));
    }

    @PatchMapping("/unAssignPropertyAssign/{propertyAssignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<Void> unAssignProperty(
            @PathVariable("propertyAssignId") Long propertyAssignId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        propertyAsignService.unAssignProperty(propertyAssignId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getPropertyAssigns")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<List<PropertyAsignResponseDto>> getPropertyAssigns(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(propertyAsignService.getPropertyAssign(authenticatedUser));
    }

    @GetMapping("/getPropertyAssignById/{propertyAssignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<PropertyAsignResponseDto> getPropertyAssignById(
            @PathVariable("propertyAssignId") Long propertyAssignId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(propertyAsignService.getPropertyAssignById(propertyAssignId, authenticatedUser));
    }
}