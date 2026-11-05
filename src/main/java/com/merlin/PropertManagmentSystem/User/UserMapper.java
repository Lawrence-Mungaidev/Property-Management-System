package com.merlin.PropertManagmentSystem.User;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User toUser(UserDto dto) {
        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email().trim().toLowerCase());
        user.setActive(true);
        user.setPhoneNumber(dto.phoneNumber());
        user.setCreatedDate(LocalDateTime.now());

        return user;
    }

    public UserResponseDto  toUserResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(),user.getRole());
    }
}
