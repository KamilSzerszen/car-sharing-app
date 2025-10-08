package org.example.carsharingapp.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserRoleUpdateRequestDto(
        @NotEmpty(message = "Roles must not empty")
        String[] roles) {
}
