package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.CarRequestDto;
import org.example.carsharingapp.dto.CarResponseDto;
import org.example.carsharingapp.dto.CarsResponseLiteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto addNewCar(CarRequestDto requestDto);

    Page<CarsResponseLiteDto> getAllCars(Pageable pageable);

    CarResponseDto getSingleCarInfo(Long id);

    CarResponseDto updateCarById(Long id, CarRequestDto requestDto);

    String deleteCarById(Long id);

}
