package com.merlin.PropertManagmentSystem.Property;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/properties")
@RestController
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping("/createProperty")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN')")
    public ResponseEntity<PropertyResponseDto> createProperty(
            @Valid @RequestBody PropertyDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.createProperty(dto, authenticatedUser));
    }

    @PutMapping("/updateProperty/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<PropertyResponseDto> updateProperty(
            @PathVariable("propertyId") Long propertyId,
            @Valid @RequestBody PropertyDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(propertyService.updateProperty(propertyId, dto, authenticatedUser));
    }

    @PatchMapping("/activateProperty/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<Void> activateProperty(
            @PathVariable("propertyId") Long propertyId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        propertyService.activateProperty(propertyId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivateProperty/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<Void> deactivateProperty(
            @PathVariable("propertyId") Long propertyId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        propertyService.deactivateProperty(propertyId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/MyProperties")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<List<PropertyResponseDto>> getProperties(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(propertyService.getProperties(authenticatedUser));
    }

    @GetMapping("/propertyById/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<PropertyResponseDto> getPropertyById(
            @PathVariable("propertyId") Long propertyId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(propertyService.getPropertyById(propertyId, authenticatedUser));
    }

    @GetMapping("/getAllProperties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyAdminResponseDto>> getAllProperties(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(propertyService.getAllProperties(authenticatedUser));
    }
}
