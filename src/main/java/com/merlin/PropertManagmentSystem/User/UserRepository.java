package com.merlin.PropertManagmentSystem.User;

import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    boolean existsByOrganisationAndRole(Organisation organisation, Role role);
    Optional<User> findByIdAndRole(Long id, Role role);
    List<User> findAllByRole(Role role);
    boolean existsByPayBill(String payBill);
    List<String> findByPayBillIsNotNull();
}
