package com.merlin.PropertManagmentSystem.Organisation;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;

    public OrganisationResponseDto update(OrganisationDto organisationDto, User authenticatedUser) {

        if(!authenticatedUser.getRole().equals(Role.CHAIRMAN)  && authenticatedUser.isActive()){
            throw new BusinessRuleException("You cannot update the organisation since you are not a Chairman");
        }

        Organisation organisation = authenticatedUser.getOrganisation();

        if(organisationDto.name() != null ){
            organisation.setName(organisationDto.name());
        }

        if(organisationDto.address() != null ){
            organisation.setAddress(organisationDto.address());
        }

        if(organisationDto.town() != null ){
            organisation.setTown(organisationDto.town());
        }
        var savedOrganisation = organisationRepository.save(organisation);

        return organisationMapper.toOrganisationResponseDto(savedOrganisation);
    }

    public OrganisationResponseDto getMyOrganisation(User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        if (!isOfficer || !authenticatedUser.isActive()) {
            throw new BusinessRuleException("Sorry you cannot view the organisation");
        }

       var organisation = organisationRepository.findById(authenticatedUser.getOrganisation().getId())
               .orElseThrow(() -> new ResourceNotFound("Your Organisation is Not Found"));

        return organisationMapper.toOrganisationResponseDto(organisation);
    }

    public List<OrganisationResponseDto> getAllOrganisations(User authenticatedUser){
        if(!authenticatedUser.getRole().equals(Role.ADMIN)){
            throw new BusinessRuleException("You cannot view the organisations");
        }

        return organisationRepository.findAll()
                .stream()
                .map(organisationMapper :: toOrganisationResponseDto)
                .toList();
    }

    public OrganisationResponseDto getOrganisationById(Long organisationId,  User authenticatedUser) {
        if(!authenticatedUser.getRole().equals(Role.ADMIN)){
            throw new BusinessRuleException("You cannot view the organisations");
        }

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFound("Your Organisation is Not Found"));

        return organisationMapper.toOrganisationResponseDto(organisation);
    }

}
