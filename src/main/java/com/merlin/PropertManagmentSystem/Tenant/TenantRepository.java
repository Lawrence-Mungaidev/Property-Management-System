package com.merlin.PropertManagmentSystem.Tenant;

import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository  extends JpaRepository<Tenant,Long> {

    boolean existsByNationalIdAndLeases_Unit_Property_Id(String nationalId, Long propertyId);

    boolean existsByNationalIdAndLandlord(String nationalId, User landlord);
    boolean existsByNationalIdAndOrganisation(String nationalId, Organisation org);
    boolean existsByEmailAndLandlord(String email, User landlord);
    boolean existsByEmailAndOrganisation(String email, Organisation org);
    List<Tenant> findAllByProperty_Id(Long propertyId);


    List<Tenant> findAllByLandlord(User landlord);

    List<Tenant> findAllByOrganisation(Organisation organisation);

    List<Tenant> findDistinctByLandlordAndLeases_LeaseStatus(User landlord, LeaseStatus status);
    List<Tenant> findDistinctByOrganisationAndLeases_LeaseStatus(Organisation org, LeaseStatus status);

}
