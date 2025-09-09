package org.example.carsharingapp.dto;

import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        String type,
        int availableCars,
        BigDecimal dailyPrice
) {
}
