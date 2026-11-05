package com.merlin.PropertManagmentSystem.Unit;


import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/units")
@RestController
public class UnitController {

    private final UnitService unitService;

    @PostMapping("/createUnit/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<UnitResponseDto> createUnit(
            @PathVariable("propertyId") Long propertyId,
            @Valid @RequestBody UnitDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(unitService.createUnit(propertyId, dto, authenticatedUser));
    }

    @PatchMapping("/updateUnit/{unitId}/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<UnitResponseDto> updateUnit(
            @PathVariable("unitId") Long unitId,
            @PathVariable("propertyId") Long propertyId,
            @Valid @RequestBody UpdateUnit dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(unitService.updateUnit(unitId, propertyId, dto, authenticatedUser));
    }

    @DeleteMapping("/deleteUnit/{unitId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<Void> deleteUnit(
            @PathVariable("unitId") Long unitId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        unitService.deleteUnit(unitId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAllUnits/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<List<UnitResponseDto>> getAllUnits(
            @PathVariable("propertyId") Long propertyId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(unitService.getAllUnits(propertyId, authenticatedUser));
    }

    @GetMapping("/getUnit/{unitId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<UnitResponseDto> getUnit(
            @PathVariable("unitId") Long unitId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(unitService.getUnit(unitId, authenticatedUser));
    }

    @PatchMapping("/activateUnit/{unitId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<Void> activateUnit(
            @PathVariable("unitId") Long unitId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        unitService.activateUnit(unitId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivateUnit/{unitId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<Void> deactivateUnit(
            @PathVariable("unitId") Long unitId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        unitService.deactivateUnit(unitId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}