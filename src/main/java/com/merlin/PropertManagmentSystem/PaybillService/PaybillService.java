package com.merlin.PropertManagmentSystem.PaybillService;

import com.merlin.PropertManagmentSystem.Exception.DuplicateRuleException;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Organisation.OrganisationRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import com.merlin.PropertManagmentSystem.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaybillService {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;


    public String createPayBill(PayBillDTO payBillDTO, User authenticatedUsr) {

        if(organisationRepository.existsByPayBill(payBillDTO.payBill()) || userRepository.existsByPayBill(payBillDTO.payBill())) {
            throw new DuplicateRuleException("The pay bill already exists");
        }

        if(authenticatedUsr.getRole().equals(Role.LANDLORD)){
            authenticatedUsr.setPayBill(payBillDTO.payBill());
        } else if (authenticatedUsr.getRole().equals(Role.CHAIRMAN)) {

            Organisation organisation = authenticatedUsr.getOrganisation();
            organisation.setPayBill(payBillDTO.payBill());

        }

        return payBillDTO.payBill();

    }


}
