package com.merlin.PropertManagmentSystem.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordDto(
        @NotBlank(message = "Old password is required")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!.]).{8,}$", message = "Password must be at least 8 characters, one uppercase, one number and one special character")
        String oldPassword,
        @NotBlank(message = "New password is required")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!.]).{8,}$", message = "Password must be at least 8 characters, one uppercase, one number and one special character")
        String newPassword,
        @NotBlank(message = "Please Confirm your new password")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!.]).{8,}$", message = "Password must be at least 8 characters, one uppercase, one number and one special character")
        String confirmNewPassword
) {
}
