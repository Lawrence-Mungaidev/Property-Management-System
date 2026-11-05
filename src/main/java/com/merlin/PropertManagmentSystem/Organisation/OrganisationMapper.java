package com.merlin.PropertManagmentSystem.Organisation;

import org.springframework.stereotype.Component;

@Component
public class OrganisationMapper {

    public OrganisationResponseDto toOrganisationResponseDto(Organisation organisation) {
        return new OrganisationResponseDto(organisation.getId(), organisation.getName(), organisation.getAddress(), organisation.getTown());
    }
}
