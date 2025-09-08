package org.example.carsharingapp.dto;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String[] roles) {
}
