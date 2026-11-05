package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyMapper;
import com.merlin.PropertManagmentSystem.Property.PropertyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PropertyAsignMapper {

    private final PropertyMapper propertyMapper;


   public PropertyAsignResponseDto toPropertyAsignResponseDto(PropertyAsign PA) {

       PropertyResponseDto property = propertyMapper.toPropertyResponseDto(PA.getProperty());

       return new PropertyAsignResponseDto(PA.getId(), property, PA.getCareTaker().getId());
   }

}
