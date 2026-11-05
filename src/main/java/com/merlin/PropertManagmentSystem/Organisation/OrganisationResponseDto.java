package com.merlin.PropertManagmentSystem.Organisation;

public record OrganisationResponseDto (
        Long organisationId,
        String name,
        String address,
        String town
) {
}
