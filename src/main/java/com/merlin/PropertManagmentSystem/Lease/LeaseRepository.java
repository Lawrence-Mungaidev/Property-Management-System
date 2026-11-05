package com.merlin.PropertManagmentSystem.Lease;

import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LeaseRepository extends JpaRepository<Lease,Long> {

    boolean existsByUnit(Unit unit);
    List<Lease> findByUnit_Property(Property property);
    boolean existsByUnitAndLeaseStatusIn(Unit unit, Collection<LeaseStatus> statuses);

    Optional<Lease> findByUnitAndLeaseStatus(Unit unit, LeaseStatus leaseStatus);
    List<Lease> findByUnit(Unit unit);
    Optional<Lease> findByTenantIdAndLeaseStatus(Long tenantId, LeaseStatus status);
}
