package org.example.carsharingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 2, max = 20)
        String password
) {
}
