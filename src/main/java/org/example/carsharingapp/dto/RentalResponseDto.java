package org.example.carsharingapp.dto;

import java.time.LocalDateTime;

public record RentalResponseDto(
        Long id,
        LocalDateTime rentalDate,
        LocalDateTime returnDate,
        String brand,
        String model,
        String email,
        String firstName,
        String lastName,
        String isActive
) {
}
