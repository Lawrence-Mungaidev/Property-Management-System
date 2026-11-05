package com.merlin.PropertManagmentSystem.Unit;

import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {


    List<Unit> findByProperty(Property property);
    long countByPropertyLandlordId(Long landlordId);
    long countByPropertyOrganisationId(Long organisationId);
    Optional<Unit> findByAccountNumberAndProperty_Landlord_PayBill(String accountNumber, String payBill);
    boolean existsByAccountNumberAndProperty_Landlord_PayBill(String accountNumber, String payBill);

    int countByProperty(Property property);

    boolean existsByPropertyAndUnitNumber(Property property, String unitNumber);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByAccountNumberAndIdNot(String accountNumber, Long id);
    boolean existsByPropertyIdAndUnitNumberAndIdNot(Long propertyId, String unitNumber, Long id);


}
