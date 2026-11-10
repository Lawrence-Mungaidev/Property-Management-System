package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.DuplicateRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyAsignService {

    private final PropertyAsignRepository propertyAsignRepository;
    private final PropertyAsignMapper propertyAsignMapper;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyAsignResponseDto assignProperty(PropertyAsignDto  dto, User authenticatedUser) {

        if(propertyAsignRepository.existsByCareTaker_IdAndProperty_IdAndUnAssignedAtIsNull(dto.careTaker(),  dto.propertyId())){
            throw new DuplicateRuleException("The Property is already assigned to someone else");
        }

        if(propertyAsignRepository.existsByProperty_IdAndUnAssignedAtIsNull(dto.propertyId())){
            throw new DuplicateRuleException("The Property is already assigned to someone else");
        }

        PropertyAsign propertyAsign = new PropertyAsign();

        Property property = propertyRepository.findById(dto.propertyId())
                .orElseThrow(() -> new ResourceNotFound("Property not found"));

        User careTaker = userRepository.findById(dto.careTaker())
                .orElseThrow(() -> new ResourceNotFound("User not found"));


        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)) {
            if(property.getLandlord() != null && property.getLandlord().getId().equals(authenticatedUser.getId())) {
                if(careTaker.getLandlord() != null && property.getLandlord().getId().equals(careTaker.getLandlord().getId())) {
                    propertyAsign.setLandlord(property.getLandlord());
                }
            }
        } else if (isOfficer && authenticatedUser.isActive()) {
            if(property.getOrganisation() != null && property.getOrganisation().getId().equals(authenticatedUser.getId())) {
                if(careTaker.getOrganisation() != null && property.getOrganisation().getId().equals(careTaker.getOrganisation().getId())) {
                    propertyAsign.setOrganisation(property.getOrganisation());
                }
            }

        }

        propertyAsign.setProperty(property);
        propertyAsign.setCareTaker(careTaker);
        propertyAsign.setAssignedBy(authenticatedUser);
        propertyAsign.setAssignedAt(LocalDateTime.now());

        var savedAssigned =propertyAsignRepository.save(propertyAsign);

        return propertyAsignMapper.toPropertyAsignResponseDto(savedAssigned);
    }

    public void unAssignProperty(Long propertyAssignId, User authenticatedUser) {
        PropertyAsign propertyAsign = propertyAsignRepository.findById(propertyAssignId)
                .orElseThrow(() -> new ResourceNotFound("PropertyAssign not found"));

        if(propertyAsign.getUnAssignedAt() != null) {
            throw new BusinessRuleException("You've already unassigned this property");
        }

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        boolean isAllowed = false;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)) {
            if(propertyAsign.getLandlord() != null && propertyAsign.getLandlord().getId().equals(authenticatedUser.getId())) {
                isAllowed = true;
            }
        }else if(isOfficer && authenticatedUser.isActive()) {
            if(propertyAsign.getOrganisation() != null && propertyAsign.getOrganisation().getId().equals(authenticatedUser.getId())) {
                isAllowed = true;
            }
        }else {
            throw new BusinessRuleException("Sorry you cant unassign this caretaker");
        }

        if(!isAllowed) {
            throw new BusinessRuleException("Sorry you cant unassign this caretaker");
        }


        propertyAsign.setUnassignedBy(authenticatedUser);
        propertyAsign.setUnAssignedAt(LocalDateTime.now());

        var savedPA = propertyAsignRepository.save(propertyAsign);

         propertyAsignMapper.toPropertyAsignResponseDto(savedPA);
    }

    public List<PropertyAsignResponseDto> getPropertyAssign(User authenticatedUser) {
        if(authenticatedUser.getRole().equals(Role.MEMBER)){
            throw new BusinessRuleException("You cannot view assign properties");
        }

        List<PropertyAsign> propertyAssigns = new ArrayList<>();

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        if (isOfficer & authenticatedUser.isActive()) {
            propertyAssigns.addAll(propertyAsignRepository.findByProperty_Organisation_Id(authenticatedUser.getOrganisation().getId()));
        } else if (authenticatedUser.getRole().equals(Role.LANDLORD)) {
            propertyAssigns.addAll(propertyAsignRepository.findByProperty_Landlord_Id(authenticatedUser.getId()));
        } else {
            throw new BusinessRuleException("Sorry you cant view assign properties");
        }

        return propertyAssigns
                .stream()
                .map(propertyAsignMapper ::toPropertyAsignResponseDto)
                .toList();

    }

    public PropertyAsignResponseDto getPropertyAssignById(Long propertyAssignId, User authenticatedUser) {

        PropertyAsign propertyAsign = propertyAsignRepository.findById(propertyAssignId)
                .orElseThrow(() -> new ResourceNotFound("Property not found"));

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                ||  authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);


        boolean ownsIt = false;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
           if( propertyAsign.getLandlord() !=null && !propertyAsign.getLandlord().getId().equals(authenticatedUser.getId())) {
               throw new BusinessRuleException("You cannot view assign properties");
           }
           if(propertyAsignRepository.existsByIdAndProperty_Landlord_Id(propertyAsign.getId(), authenticatedUser.getId())) {
               ownsIt = true;
           }
        } else if (!isOfficer & authenticatedUser.isActive()) {
            if(propertyAsign.getOrganisation() !=null && !propertyAsign.getOrganisation().getId().equals(authenticatedUser.getId())) {
                throw new BusinessRuleException("You cannot view assign properties");
            }

            if (propertyAsignRepository.existsByIdAndProperty_Organisation_Id(propertyAsign.getId(), authenticatedUser.getOrganisation().getId())) {
                ownsIt = true;
            }

        }else {
            throw new BusinessRuleException("Sorry you cant view assign properties");
        }

        if(!ownsIt){
            throw new BusinessRuleException("Sorry you cant view assign properties");
        }

        return propertyAsignMapper.toPropertyAsignResponseDto(propertyAsign);
    }

}
