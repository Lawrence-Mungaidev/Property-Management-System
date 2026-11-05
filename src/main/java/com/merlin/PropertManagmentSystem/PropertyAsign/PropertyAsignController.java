package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/updatePropertyAssign/{propertyAssignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<PropertyAsignResponseDto> updatePropertyAssign(
            @PathVariable("propertyAssignId") Long propertyAssignId,
            @Valid @RequestBody PropertyAsignDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(propertyAsignService.updateProperty(propertyAssignId, dto, authenticatedUser));
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