package org.example.carsharingapp.dto;

import java.time.LocalDateTime;

public record RentalRequestDto(
        LocalDateTime rentalDate,
        LocalDateTime returnDate,
        Long carId
) {
}
