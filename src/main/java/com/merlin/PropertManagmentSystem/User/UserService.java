package com.merlin.PropertManagmentSystem.User;

import com.merlin.PropertManagmentSystem.Messaging.EmailService;
import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.DuplicateRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private  final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserResponseDto createUser(UserDto dto, User authenticatedUser){
        User user = userMapper.toUser(dto);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(dto.role()  != Role.CARETAKER){
                throw new BusinessRuleException("you can only create caretaker role");
            }

            user.setRole(dto.role());
            user.setLandlord(authenticatedUser);
        } else if (authenticatedUser.getRole().equals(Role.CHAIRMAN)) {

            if (dto.role() == Role.SECRETARY &&
                    userRepository.existsByOrganisationAndRole(authenticatedUser.getOrganisation(), Role.SECRETARY)) {
                throw new BusinessRuleException("The Organisation already has a Secretary");
            }

            if (dto.role() == Role.TREASURER &&
                    userRepository.existsByOrganisationAndRole(authenticatedUser.getOrganisation(), Role.TREASURER)) {
                throw new BusinessRuleException("This Organisation already has a Treasurer");
            }

            user.setRole(dto.role());
            user.setOrganisation(authenticatedUser.getOrganisation());


        } else if (authenticatedUser.getRole().equals(Role.SECRETARY) && authenticatedUser.getOrganisation().isNeedsChairMan()) {

                user.setRole(Role.CHAIRMAN);
                user.setOrganisation(authenticatedUser.getOrganisation());

        } else {
            throw new BusinessRuleException("You are not allowed to create a user");

        }


        if(userRepository.existsByEmail(dto.email()) || userRepository.existsByPhoneNumber(dto.phoneNumber())){
            throw new DuplicateRuleException("Email or Phone Number already exists");
        }

        String password = "123456789PM.";


        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setMustChangePassword(true);

        var savedUser = userRepository.save(user);

        return userMapper.toUserResponseDto(savedUser);
    }



    public UserResponseDto getUserById(Long userId, User authenticatedUser) {

        boolean allowedRole = authenticatedUser.getRole().equals(Role.LANDLORD)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        if (!allowedRole) {
            throw new BusinessRuleException("You are not allowed to get a user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("The user is not found"));

        boolean sameLandlord = user.getLandlord() != null
                && user.getLandlord().getId().equals(authenticatedUser.getId());

        boolean sameOrganisation = user.getOrganisation() != null
                && authenticatedUser.getOrganisation() != null
                && user.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId());

        if (!sameLandlord && !sameOrganisation) {
            throw new BusinessRuleException("You cant get this user");
        }

        return userMapper.toUserResponseDto(user);
    }

    public UserResponseDto updateMyProfile (UserUpdateDto dto, User authenticatedUser){
        if(!(authenticatedUser.getRole().equals(Role.LANDLORD) || authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.CHAIRMAN))){
            throw new BusinessRuleException("You are not allowed to get a user");
        }

        if(dto.firstName() != null){
            authenticatedUser.setFirstName(dto.firstName());
        }
        if(dto.lastName() != null){
            authenticatedUser.setLastName(dto.lastName());
        }

        if(dto.phoneNumber() != null){
            if(userRepository.existsByPhoneNumber(dto.phoneNumber())){
                throw new DuplicateRuleException("The Phone Number already exists");
            }
            authenticatedUser.setPhoneNumber(dto.phoneNumber());
        }

        var savedUser = userRepository.save(authenticatedUser);

        return userMapper.toUserResponseDto(savedUser);

    }

    public void activateUser(Long userId, User authenticatedUser){
        if(!(authenticatedUser.getRole().equals(Role.LANDLORD)|| authenticatedUser.getRole().equals(Role.CHAIRMAN))){
            throw new BusinessRuleException("You are not allowed to get a user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFound("The user is not found"));

        boolean sameLandlord = user.getLandlord() != null
                && user.getLandlord().getId().equals(authenticatedUser.getId());

        boolean sameOrganisation = user.getOrganisation() != null
                && authenticatedUser.getOrganisation() != null
                && user.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId());

        if(!sameLandlord && !sameOrganisation){
            throw new BusinessRuleException("You cant activate this user");
        }

        if(user.isActive()){
            throw new BusinessRuleException("The user is already active");
        }

        user.setActive(true);
        userRepository.save(user);
    }

    public void deactivateUser(Long userId, User authenticatedUser){
        if(!(authenticatedUser.getRole().equals(Role.LANDLORD) || authenticatedUser.getRole().equals(Role.CHAIRMAN))){
            throw new BusinessRuleException("You are not allowed to get a user");
        }


        User user =  userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFound("The user is not found"));

        boolean sameLandlord = user.getLandlord() != null
                && user.getLandlord().getId().equals(authenticatedUser.getId());

        boolean sameOrganisation = user.getOrganisation() != null
                && authenticatedUser.getOrganisation() != null
                && user.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId());

        if(!sameLandlord && !sameOrganisation){
            throw new BusinessRuleException("You cant deactivate this user");
        }

        if(!user.isActive()){
            throw new BusinessRuleException("The user is already deactivated");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    public void changePassword(User authenticatedUser, ChangePasswordDto dto){

        if(!passwordEncoder.matches(dto.oldPassword(),  authenticatedUser.getPassword())){
            throw new BusinessRuleException("Old password does not match");
        }

        if(!dto.newPassword().equals(dto.confirmNewPassword())){
            throw new BusinessRuleException("new password does not match ");
        }

        String newPassword = passwordEncoder.encode(dto.confirmNewPassword());
        authenticatedUser.setPassword(newPassword);
        authenticatedUser.setMustChangePassword(false);

        userRepository.save(authenticatedUser);
    }

    public ForgortResponse forgotPassword(Long userId ){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        String email = user.getEmail();

        String tempPassword = generateTempPassword();

        String password = passwordEncoder.encode(tempPassword);

        user.setPassword(password);

        String subject = "Forgot Password";

       emailService.sendTextEmail(email, user.getFirstName(), subject,tempPassword);

        user.setMustChangePassword(true);

        String message = "A temporary password has been sent to " + user.getEmail();
        userRepository.save(user);

        return new ForgortResponse(message);
    }

    private String generateTempPassword() {
        SecureRandom random = new SecureRandom();

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@#$!";
        String all = upper + lower + digits + special;

        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(upper.charAt(random.nextInt(upper.length())));
        passwordChars.add(upper.charAt(random.nextInt(upper.length())));
        passwordChars.add(lower.charAt(random.nextInt(lower.length())));
        passwordChars.add(lower.charAt(random.nextInt(lower.length())));
        passwordChars.add(digits.charAt(random.nextInt(digits.length())));
        passwordChars.add(digits.charAt(random.nextInt(digits.length())));
        passwordChars.add(special.charAt(random.nextInt(special.length())));
        passwordChars.add(special.charAt(random.nextInt(special.length())));


        Collections.shuffle(passwordChars, random);

        StringBuilder sb = new StringBuilder();
        for (char c : passwordChars) {
            sb.append(c);
        }

        return sb.toString();
    }



}
