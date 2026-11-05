package com.merlin.PropertManagmentSystem.Auth;

import com.merlin.PropertManagmentSystem.Config.JWTService;
import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.DuplicateRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Organisation.OrganisationRepository;
import com.merlin.PropertManagmentSystem.PaybillService.PayBillStatus;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import com.merlin.PropertManagmentSystem.User.UserMapper;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrganisationRepository organisationRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthRegisterResponse registerLandLord(UserRegisterDto dto){
        if(userRepository.existsByEmail(dto.email()) ||  userRepository.existsByPhoneNumber(dto.phoneNumber())){
            throw new DuplicateRuleException("Email or phone number already exists");
        }

        User user =authMapper.toUser(dto);

        user.setRole(Role.LANDLORD);
        user.setPayBillStatus(PayBillStatus.NOT_REGISTERED);
        String encodedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(encodedPassword);
        user.setLandlordSubscribed(true);

        var savedUser = userRepository.save(user);


        String registerMessage = "You have successfully Registered!";

        return authMapper.toAuthRegisterResponse(savedUser,registerMessage);
    }

    public AuthRegisterResponse registerOrganisation(OrganisationRegisterDto dto){

        if(organisationRepository.existsByKraPin(dto.kraPin())){
            throw new DuplicateRuleException("This organisation already exists");
        }

        Organisation organisation = authMapper.toOrganisation(dto);

        User user = authMapper.toUser(dto.chairman());

        if(userRepository.existsByEmail(dto.chairman().email()) ||  userRepository.existsByPhoneNumber(dto.chairman().phoneNumber())){
            throw new DuplicateRuleException("Email or phone number already exists");
        }


        user.setOrganisation(organisation);
        user.setRole(Role.CHAIRMAN);



        String encodedPassword = passwordEncoder.encode(dto.chairman().password());

        user.setPassword(encodedPassword);
        organisation.setPayBillStatus(PayBillStatus.NOT_REGISTERED);
        organisation.setOrganisationSubscribed(true);


        Organisation org =  organisationRepository.save(organisation);
        User savedUser = userRepository.save(user);

        org.setChairman(savedUser);

        String registerMessage = "You have successfully Registered!";

        return authMapper.toAuthRegisterResponse(savedUser,registerMessage);

    }


    public LoginResponseDto  login(LoginDto dto){

            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                dto.email(),
                                dto.password()
                        )
                );
            }catch (BadCredentialsException e){
                throw new BusinessRuleException(
                        "Invalid email or password"
                );
            }


            var user = userRepository.findByEmail(dto.email())
                    .orElseThrow(()-> new ResourceNotFound("User not found"));

            var jwt = jwtService.generateToken(user);

            boolean mustNeedChairMan = false;

            boolean isOfficer = user.getRole().equals(Role.CHAIRMAN)
                    || user.getRole().equals(Role.SECRETARY)
                    || user.getRole().equals(Role.TREASURER);

            boolean isSubscribed;

            if(user.getRole().equals(Role.LANDLORD)){
                 isSubscribed = user.isLandlordSubscribed();
            } else if (user.getRole().equals(Role.ADMIN)) {
                isSubscribed = true;

            } else if (isOfficer && user.isActive()) {

                mustNeedChairMan = user.getOrganisation().isNeedsChairMan();

                isSubscribed = user.getOrganisation().isOrganisationSubscribed();


            } else if(user.getRole().equals(Role.CARETAKER)){
                isSubscribed = user.getLandlord().isLandlordSubscribed();
            }else {
                throw new BusinessRuleException("Sorry, failed to Login");
            }


        return new LoginResponseDto(jwt, user.getFirstName(),user.isMustChangePassword(),user.getRole(), mustNeedChairMan,isSubscribed);

    }

}
