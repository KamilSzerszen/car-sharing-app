package org.example.carsharingapp.dto;

import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        String status,
        String type,
        String brand,
        String model,
        String sessionUrl,
        String sessionId,
        BigDecimal amountToPay
) {
}
