package com.merlin.PropertManagmentSystem.Organisation;

import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/organisations")
@RestController
public class OrganisationController {

    private final OrganisationService organisationService;

    @PutMapping("/updateOrganisation")
    @PreAuthorize("hasRole('CHAIRMAN')")
    public ResponseEntity<OrganisationResponseDto> updateOrganisation(
            @Valid @RequestBody OrganisationDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(organisationService.update(dto, authenticatedUser));
    }

    @GetMapping("/getMyOrganisation")
    @PreAuthorize("hasAnyRole('CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<OrganisationResponseDto> getMyOrganisation(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(organisationService.getMyOrganisation(authenticatedUser));
    }

    @GetMapping("/getAllOrganisations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrganisationResponseDto>> getAllOrganisations(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(organisationService.getAllOrganisations(authenticatedUser));
    }

    @GetMapping("/getOrganisationById/{organisationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganisationResponseDto> getOrganisationById(
            @PathVariable("organisationId") Long organisationId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(organisationService.getOrganisationById(organisationId, authenticatedUser));
    }
}
