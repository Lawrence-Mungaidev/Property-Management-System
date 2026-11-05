package com.merlin.PropertManagmentSystem.Property;

import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property,Long> {

    boolean existsByPropertyNameAndAddressAndLandlord(String propertyName, String address, User landlord);

    boolean existsByPropertyNameAndAddressAndOrganisation(String name, String address, Organisation organisation);

    List<Property> findByLandlord(User landlord);
    List<Property> findByOrganisation(Organisation organisation);
    boolean existsByIdAndLandlordId(Long id, Long landlordId);
    boolean existsByIdAndOrganisationId(Long id, Long organisationId);

}
