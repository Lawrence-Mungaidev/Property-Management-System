package com.merlin.PropertManagmentSystem.PaymentDispute;

import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentDisputeRepository extends JpaRepository<PaymentDispute,Long> {

    List<PaymentDispute> findAllByOrganisation(Organisation  organisation);
    List<PaymentDispute> findAllByLandlord(User landlord);
}
