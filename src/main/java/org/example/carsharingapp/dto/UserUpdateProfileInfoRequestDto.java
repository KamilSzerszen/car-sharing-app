package org.example.carsharingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateProfileInfoRequestDto(
        @NotBlank @Email(message = "Email should be valid: user@domain.com ")
        String email,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName
) {

}