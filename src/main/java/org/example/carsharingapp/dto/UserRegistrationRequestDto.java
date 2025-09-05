package org.example.carsharingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequestDto(

        @NotBlank @Email(message = "Email should be valid: user@domain.com ")
        String email,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank @Size(min = 2, max = 20)
        String password,

        @NotBlank @Size(min = 2, max = 20)
        String repeatPassword) {
}
