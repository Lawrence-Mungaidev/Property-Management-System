package com.merlin.PropertManagmentSystem.Organisation;

import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation,Long> {

    boolean existsByKraPin(String kraPin);


    boolean existsByPayBill(String payBill);
    List<String> findByPayBillIsNotNull();
}
