package com.merlin.PropertManagmentSystem.Property;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Unit.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PropertyMapper {

    private final UnitRepository userRepository;

    public Property toProperty(PropertyDto dto) {
        Property property = new Property();

        property.setPropertyName(dto.propertyName());
        property.setAddress(dto.address());
        property.setTown(dto.town());
        property.setPreferredBillingDay(dto.preferredBillingDay());
        property.setActive(true);

        return property;
    }

    public PropertyResponseDto toPropertyResponseDto(Property property) {
        return new PropertyResponseDto(property.getId(),  property.getPropertyName(), property.getAddress(), property.getTown());
    }

    public  PropertyAdminResponseDto toPropertyAdminResponseDto(Property property) {

        String OwnedBy;
        if(property.getLandlord() != null){
            OwnedBy = property.getLandlord().getFirstName() + " " + property.getLandlord().getLastName();

        }else if(property.getOrganisation() != null){
            OwnedBy = property.getOrganisation().getName();
        }else {
            throw new BusinessRuleException("The property isnt linked to anyone");
        }

        int numberOfUnits = userRepository.countByProperty(property);


        return  new PropertyAdminResponseDto(property.getId(),OwnedBy, property.getPropertyName(), property.getAddress(), property.getTown(), property.getCreatedOn(),numberOfUnits );
    }
}
