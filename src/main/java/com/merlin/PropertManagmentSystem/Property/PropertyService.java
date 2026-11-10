package com.merlin.PropertManagmentSystem.Property;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsignRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final PropertyAsignRepository propertyAsignRepository;

    public PropertyResponseDto createProperty(PropertyDto dto, User authenticatedUser) {

        Property property = propertyMapper.toProperty(dto);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){

            if(propertyRepository.existsByPropertyNameAndAddressAndLandlord(dto.propertyName(), dto.address(), authenticatedUser)){
                throw new BusinessRuleException("This property already exists");
            }

            property.setLandlord(authenticatedUser);

        } else if (authenticatedUser.getRole().equals(Role.CHAIRMAN)) {

            Organisation organisation = authenticatedUser.getOrganisation();

            if(propertyRepository.existsByPropertyNameAndAddressAndOrganisation(dto.propertyName(), property.getAddress(), organisation)){
                throw new BusinessRuleException("This property already exists");
            }

            property.setOrganisation(authenticatedUser.getOrganisation());
        }else {
            throw new BusinessRuleException("You are not allowed to create a property");
        }
        property.setCreatedBy(authenticatedUser);
        property.setCreatedOn(LocalDateTime.now());

        var savedProperty = propertyRepository.save(property);

        return propertyMapper.toPropertyResponseDto(savedProperty);
    }

    public PropertyResponseDto updateProperty(Long propertyId,PropertyDto dto, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.LANDLORD);

        if(!isOfficer){
            throw new BusinessRuleException("You are not allowed to update a property");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()-> new ResourceNotFound("The property is not found"));

        if(dto.propertyName() != null){
            property.setPropertyName(dto.propertyName());
        }

        if(dto.address()!= null){
            property.setAddress(dto.address());
        }

        if(dto.town() != null){
            property.setTown(dto.town());
        }

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(propertyRepository.existsByPropertyNameAndAddressAndLandlord(dto.propertyName(), dto.address(), authenticatedUser)){
                throw new BusinessRuleException("This property already exists");
            }
        } else if (authenticatedUser.getRole().equals(Role.CHAIRMAN) || authenticatedUser.getRole().equals(Role.SECRETARY) ){
            Organisation organisation = authenticatedUser.getOrganisation();

            if(propertyRepository.existsByPropertyNameAndAddressAndOrganisation(dto.propertyName(), property.getAddress(), organisation)){
                throw new BusinessRuleException("This property already exists");
            }
        }else {
            throw new BusinessRuleException("You are not allowed to update a property");
        }

        property.setUpdatedBy(authenticatedUser);
        property.setUpdatedOn(LocalDateTime.now());

        var savedProperty = propertyRepository.save(property);

        return propertyMapper.toPropertyResponseDto(savedProperty);
    }

    public void activateProperty(Long propertyId, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.LANDLORD);

        if(!isOfficer){
            throw new BusinessRuleException("You are not allowed to update a property");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()-> new ResourceNotFound("The property is not found"));

        if(property.isActive()){
            throw new BusinessRuleException("This property is already active");
        }
        property.setActive(true);
        property.setUpdatedBy(authenticatedUser);
        property.setUpdatedOn(LocalDateTime.now());

        propertyRepository.save(property);
    }

    public void deactivateProperty(Long propertyId, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.LANDLORD);

        if(!isOfficer){
            throw new BusinessRuleException("You are not allowed to update a property");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()-> new ResourceNotFound("The property is not found"));

        if(!property.isActive()){
            throw new BusinessRuleException("This property is already  deactivated");
        }
        property.setActive(false);
        property.setUpdatedBy(authenticatedUser);
        property.setUpdatedOn(LocalDateTime.now());

        propertyRepository.save(property);
    }

    public List<PropertyResponseDto> getProperties(User authenticatedUser) {
        List<Property> properties = new ArrayList<>();

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
           properties = propertyRepository.findByLandlord(authenticatedUser);

        }else if(isOfficer){
            Organisation organisation = authenticatedUser.getOrganisation();

            properties = propertyRepository.findByOrganisation(organisation);
        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {

          List<Property> assignedProperties = propertyAsignRepository.findByCareTakerAndUnAssignedAtIsNull(authenticatedUser)
                  .stream()
                  .map(PropertyAsign :: getProperty)
                  .toList();

          properties.addAll(assignedProperties);

        }

        return properties
                .stream()
                .map(propertyMapper :: toPropertyResponseDto)
                .toList();
    }

    public PropertyResponseDto getPropertyById(Long propertyId, User authenticatedUser) {
        if(authenticatedUser.getRole().equals(Role.MEMBER)){
            throw new BusinessRuleException("You are not allowed to view a property");
        }

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN) || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()-> new ResourceNotFound("The property is not found"));

        Property rightProperty;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(!propertyRepository.existsByIdAndLandlordId(propertyId,authenticatedUser.getId())){
                throw new BusinessRuleException("Sorry you cant view this property");
            }

            rightProperty = property;
        } else if (isOfficer && authenticatedUser.isActive()) {
            if(authenticatedUser.getOrganisation() == null){
                throw new BusinessRuleException("You are not allowed to view a property");
            }

            if(!propertyRepository.existsByIdAndOrganisationId(propertyId,authenticatedUser.getOrganisation().getId())){
               throw new ResourceNotFound("Sorry the apartment wasn't found");
            }

            rightProperty = property;

        } else if(authenticatedUser.getRole().equals(Role.CARETAKER)){

           if(!propertyAsignRepository.existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser,property)){
               throw new BusinessRuleException("Sorry you cant view this property since you arent assigned to it");
           }

           rightProperty = property;
        }else {
            throw new BusinessRuleException("You are not allowed to view this property");
        }


        return propertyMapper.toPropertyResponseDto(rightProperty);

    }

    public List<PropertyAdminResponseDto> getAllProperties (User authenticatedUser){
        if(!authenticatedUser.getRole().equals(Role.ADMIN)){
            throw new BusinessRuleException("You are not allowed to view all properties");
        }

        return   propertyRepository.findAll()
                .stream()
                .map(propertyMapper :: toPropertyAdminResponseDto)
                .toList();
    }

}
