package org.example.carsharingapp.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CarRequestDto(

        @NotBlank String model,
        @NotBlank String brand,
        @NotBlank String type,
        @NotNull @Min(value = 1) Integer availableCars,
        @NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 2) BigDecimal dailyPrice
)
{ }
