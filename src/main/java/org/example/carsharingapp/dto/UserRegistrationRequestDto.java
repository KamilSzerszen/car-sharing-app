package org.example.carsharingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.carsharingapp.security.annotation.PasswordMatches;

@PasswordMatches
public record UserRegistrationRequestDto(

        @NotBlank @Email(message = "Email should be valid: user@domain.com ")
        String email,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters long")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain uppercase, lowercase, digit and special character"
        )
        String password,

        @NotBlank
        String repeatPassword) {
}
