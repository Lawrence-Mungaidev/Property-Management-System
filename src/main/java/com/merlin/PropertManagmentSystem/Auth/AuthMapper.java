package com.merlin.PropertManagmentSystem.Auth;

import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.PaybillService.PayBillStatus;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuthMapper {

    public User toUser(UserRegisterDto dto){
        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setActive(true);
        user.setMustChangePassword(false);
        user.setCreatedDate(LocalDateTime.now());
        user.setPhoneNumber(dto.phoneNumber());
        return user;
    }



    public Organisation toOrganisation(OrganisationRegisterDto dto){
        Organisation organisation = new Organisation();
        organisation.setName(dto.name());
        organisation.setAddress(dto.address());
        organisation.setTown(dto.town());
        organisation.setKraPin(dto.kraPin());

        return organisation;
    }

    public AuthRegisterResponse toAuthRegisterResponse(User user, String message){
        return new AuthRegisterResponse(user.getEmail(), message);
    }

}
