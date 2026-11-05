package com.merlin.PropertManagmentSystem.RoleChangeRequest;

import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {

    boolean existsByInitiatedByAndStatus(User initiatedBy, RoleChangeStatus status);

    List<RoleChangeRequest> findByOrganisation(Organisation organisation);
}
