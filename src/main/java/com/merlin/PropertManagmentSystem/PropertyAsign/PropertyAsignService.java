package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
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
        PropertyAsign propertyAsign = new PropertyAsign();

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        if (isOfficer & authenticatedUser.isActive()) {
            propertyAsign.setOrganisation(authenticatedUser.getOrganisation());
        } else if (authenticatedUser.getRole().equals(Role.LANDLORD)) {
            propertyAsign.setLandlord(authenticatedUser);
        } else {
            throw new BusinessRuleException("You cannot assign properties");
        }

        Property property = propertyRepository.findById(dto.properties())
                .orElseThrow(() -> new ResourceNotFound("Property"));

        User careTaker = userRepository.findById(dto.careTaker())
                .orElseThrow(() -> new ResourceNotFound("User not found"));



        propertyAsign.setProperty(property);
        propertyAsign.setCareTaker(careTaker);
        propertyAsign.setAssignedBy(authenticatedUser);
        propertyAsign.setAssignedAt(LocalDateTime.now());

        var savedAssigned =propertyAsignRepository.save(propertyAsign);

        return propertyAsignMapper.toPropertyAsignResponseDto(savedAssigned);
    }

    public PropertyAsignResponseDto updateProperty(Long propertyAssignId,PropertyAsignDto  dto, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.LANDLORD);

        if (!isOfficer & !authenticatedUser.isActive()) {
            throw new BusinessRuleException("You cannot assign properties");
        }

        PropertyAsign propertyAsign = propertyAsignRepository.findById(propertyAssignId)
                .orElseThrow(() -> new ResourceNotFound("Property not found"));

        Property property = propertyRepository.findById(dto.properties())
                .orElseThrow(() -> new ResourceNotFound("Property"));

        if(dto.properties() != null) {

            propertyAsign.setProperty(property);
        }

        if(dto.careTaker() !=null){

            User careTaker = userRepository.findById(dto.careTaker())
                            .orElseThrow(() -> new ResourceNotFound("User not found"));

            propertyAsign.setCareTaker(careTaker);

        }

        propertyAsign.setAssignedBy(authenticatedUser);
        propertyAsign.setAssignedAt(LocalDateTime.now());

        var PropertyAssign = propertyAsignRepository.save(propertyAsign);

        return propertyAsignMapper.toPropertyAsignResponseDto(PropertyAssign);
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
            propertyAssigns.addAll(authenticatedUser.getOrganisation().getPropertyAssigns());
        } else if (authenticatedUser.getRole().equals(Role.LANDLORD)) {
            propertyAssigns.addAll(authenticatedUser.getPropertyAssignsLandlord());
        } else if (authenticatedUser.getRole().equals(Role.CARETAKER) && authenticatedUser.isActive()) {
            propertyAssigns.addAll(authenticatedUser.getPropertyAssigns());
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

        PropertyAsign rightPropertyAsign = null;


        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            boolean found = false;
            for(Property property : authenticatedUser.getProperties()){
                if(property.getId().equals(propertyAsign.getProperty().getId())) {
                    found = true;
                }
            }

            if(!found){
                throw new ResourceNotFound("Property not found");
            }

            rightPropertyAsign = propertyAsign;
        } else if (!isOfficer & authenticatedUser.isActive()) {

            List<Property> properties = authenticatedUser.getProperties();

            boolean found = false;
            for(Property property : properties){
                if(property.getId().equals(propertyAsign.getProperty().getId())) {
                    found = true;
                }
            }

            if(!found){
                throw new ResourceNotFound("Property not found");
            }

            rightPropertyAsign = propertyAsign;

        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {

           List<Property> properties = propertyAsignRepository.findByCareTakerAndUnAssignedAtIsNull(authenticatedUser);

           boolean found = false;
           for(Property property : properties){
               if(property.getId().equals(propertyAsign.getProperty().getId())) {
                   found = true;
               }
           }

           if(!found){
               throw new ResourceNotFound("Property not found");
           }

            rightPropertyAsign = propertyAsign;
        }


        return propertyAsignMapper.toPropertyAsignResponseDto(rightPropertyAsign);
    }

}
