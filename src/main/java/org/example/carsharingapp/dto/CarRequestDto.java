package org.example.carsharingapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CarRequestDto(

        @NotBlank String model,
        @NotBlank String brand,
        @NotBlank String type,
        @NotNull @Min(value = 1) Integer availableCars,
        @NotNull @Min(value = 0) BigDecimal dailyPrice
)
{ }
