package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyAsignRepository extends JpaRepository<PropertyAsign,Long> {

    List<Property> findByCareTakerAndUnAssignedAtIsNull(User user);
    boolean existsByCareTakerAndPropertyAndUnAssignedAtIsNull(User user, Property property);


}
