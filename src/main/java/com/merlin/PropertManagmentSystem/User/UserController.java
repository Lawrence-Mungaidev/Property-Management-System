package com.merlin.PropertManagmentSystem.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/createUser")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto, authenticatedUser));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(userId, authenticatedUser));
    }

    @PatchMapping("/updateMyProfile")
    public ResponseEntity<UserResponseDto> updateUser(
            @Valid @RequestBody UserUpdateDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateMyProfile(dto, authenticatedUser));
    }

    @PatchMapping("/activateUser/{userId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<Void> activateUser(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        userService.activateUser(userId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivateUser/{userId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY')")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        userService.deactivateUser(userId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/changePassword")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        userService.changePassword(authenticatedUser, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgotPassword/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ForgortResponse> forgotPassword(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.forgotPassword(userId));
    }
}
