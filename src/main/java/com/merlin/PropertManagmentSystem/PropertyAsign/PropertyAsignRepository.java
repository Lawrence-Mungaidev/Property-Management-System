package com.merlin.PropertManagmentSystem.PropertyAsign;

import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertyAsignRepository extends JpaRepository<PropertyAsign,Long> {


    boolean existsByCareTakerAndPropertyAndUnAssignedAtIsNull(User user, Property property);
    List<PropertyAsign> findByProperty_Landlord_Id(Long landlordId);
    List<PropertyAsign> findByProperty_Organisation_Id(Long organisationId);
    List<PropertyAsign> findByCareTakerAndUnAssignedAtIsNull(User user);

    boolean existsByCareTaker_IdAndProperty_IdAndUnAssignedAtIsNull(Long careTakerId, Long propertyId);
    boolean existsByProperty_IdAndUnAssignedAtIsNull(Long propertyId);

    boolean existsByIdAndProperty_Landlord_Id(Long propertyAssignId, Long landlordId);
    boolean existsByIdAndProperty_Organisation_Id(Long propertyAssignId, Long organisationId);



}
