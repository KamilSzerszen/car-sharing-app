package org.example.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.RentalRequestDto;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.example.carsharingapp.security.annotation.IsCustomer;
import org.example.carsharingapp.security.annotation.IsManager;
import org.example.carsharingapp.service.RentalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Rentals Controller", description = "Managing rentals")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @IsCustomer
    @PostMapping
    @Operation(summary = "Add new rental", description = "Add new rental for current user")
    public RentalResponseDto addNewRental(@RequestBody RentalRequestDto rentalRequestDto) {
        return rentalService.addNewRental(rentalRequestDto);
    }

    @IsManager
    @GetMapping(params = "user_id")
    @Operation(summary = "Get rentals by user id", description = "Get rentals by param user_id")
    public Page<RentalResponseDto> getRentalsByUserId(
            @RequestParam(required = false) Long user_id,
            @RequestParam(required = false) Boolean is_active,
            Pageable pageable
    ) {
        return rentalService.getRentalsByUserIdAndIsActive(user_id, is_active, pageable);
    }

    @IsCustomer
    @GetMapping
    @Operation(summary = "Get rentals", description = "Get rentals by current user")
    public Page<RentalResponseDto> getRentalsByCurrentUser(Pageable pageable) {
        return rentalService.getRentalByCurrentUser(pageable);
    }

    @IsCustomer
    @PostMapping("/{id}/return")
    @Operation(summary = "End car rental", description = "Return car, end active rental")
    public RentalResponseDto returnRental(@PathVariable Long id) {
        return rentalService.returnRentalByRentalId(id);
    }

}
