package com.merlin.PropertManagmentSystem.Lease;


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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/leases")
@RestController
public class LeaseController {

    private final LeaseService leaseService;

    @PostMapping("/createLease")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'CARETAKER')")
    public ResponseEntity<LeaseResponseDto> createLease(
            @Valid @RequestBody LeaseDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaseService.createLease(dto, authenticatedUser));
    }

    @PostMapping("/createOnBoardingLease")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'CARETAKER')")
    public ResponseEntity<LeaseResponseDto> createOnBoardingLease(
            @Valid @RequestBody OnboardLeaseDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaseService.createOnBoardingLease(dto, authenticatedUser));
    }

    @PostMapping("/endLease/{leaseId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'CARETAKER')")
    public ResponseEntity<Void> endLease(
            @PathVariable("leaseId") Long leaseId,
            @Valid @RequestBody EndLeaseDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        leaseService.endLease(leaseId,dto, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getLeasesByProperty/{propertyId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<List<LeaseResponseDto>> getLeasesByProperty(
            @PathVariable("propertyId") Long propertyId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(leaseService.getAllLeaseByProperty(propertyId, authenticatedUser));
    }

    @GetMapping("/getLeaseByUnit/{unitId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<LeaseResponseDto> getLeaseByUnit(
            @PathVariable("unitId") Long unitId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(leaseService.getLeaseByUnit(unitId, authenticatedUser));
    }

    @GetMapping("/getLeaseHistoryByUnit/{unitId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<List<LeaseResponseDto>> getLeaseHistoryByUnit(
            @PathVariable("unitId") Long unitId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(leaseService.getAllLeaseUnitHistory(unitId, authenticatedUser));
    }
}
