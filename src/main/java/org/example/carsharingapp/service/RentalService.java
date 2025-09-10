package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.RentalRequestDto;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto addNewRental(RentalRequestDto rentalRequestDto);

    Page<RentalResponseDto> getRentalsByUserIdAndIsActive(
            Long userId, Boolean isActive, Pageable pageable);

    Page<RentalResponseDto> getRentalByCurrentUser(Pageable pageable);

    RentalResponseDto returnRentalByRentalId(Long id);
}
