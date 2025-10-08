package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.CarRequestDto;
import org.example.carsharingapp.dto.CarResponseDto;
import org.example.carsharingapp.dto.CarResponseLiteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto addNewCar(CarRequestDto requestDto);

    Page<CarResponseLiteDto> getAllCars(Pageable pageable);

    CarResponseDto getSingleCarInfo(Long id);

    CarResponseDto updateCarById(Long id, CarRequestDto requestDto);

    void deleteCarById(Long id);

}
