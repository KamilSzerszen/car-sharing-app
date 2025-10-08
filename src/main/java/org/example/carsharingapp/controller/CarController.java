package org.example.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.CarRequestDto;
import org.example.carsharingapp.dto.CarResponseDto;
import org.example.carsharingapp.dto.CarResponseLiteDto;
import org.example.carsharingapp.security.annotation.IsCustomer;
import org.example.carsharingapp.security.annotation.IsManager;
import org.example.carsharingapp.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cars Controller", description = "Managing car inventory")
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @IsManager
    @PostMapping
    @Operation(summary = "Add a new car", description = "Add a new car")
    public CarResponseDto addNewCar(@RequestBody @Valid CarRequestDto requestDto) {
        return carService.addNewCar(requestDto);
    }

    @GetMapping()
    @Operation(summary = "List of all cars", description = "Get a list off all cars")
    public Page<CarResponseLiteDto> getAllCars(Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @IsCustomer
    @GetMapping("/{id}")
    @Operation(summary = "Get cars info", description = "Get car's detailed information")
    public CarResponseDto getSingleCarInfo(@PathVariable Long id) {
        return carService.getSingleCarInfo(id);
    }

    @IsManager
    @PutMapping("/{id}")
    @Operation(summary = "Update car", description = "Update car details")
    public CarResponseDto updateCarById(@PathVariable Long id,
                                        @RequestBody @Valid CarRequestDto requestDto) {
        return carService.updateCarById(id, requestDto);
    }

    @IsManager
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete car", description = "Delete car with valid id")
    public void deleteCarById(@PathVariable Long id) {
       carService.deleteCarById(id);
    }
}
