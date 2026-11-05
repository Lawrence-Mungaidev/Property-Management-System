package com.merlin.PropertManagmentSystem.RentRecord;

import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentRecordRepository extends JpaRepository<RentRecord, Long> {

    List<RentRecord> findByLease_Unit_Property_Landlord(User landlord);
    List<RentRecord> findByLease_Unit_Property_Organisation(Organisation organisation);
    List<RentRecord> findByLease_Unit_PropertyIn(List<Property> properties);   // Caretaker
    List<RentRecord> findByLeaseAndRentStatusInOrderByStartDateAsc(Lease lease, List<RentStatus> rentStatus);


    boolean existsByLeaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Lease lease, LocalDate periodEnd, LocalDate periodStart);

}
