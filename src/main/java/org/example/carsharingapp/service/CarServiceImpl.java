package org.example.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.CarRequestDto;
import org.example.carsharingapp.dto.CarResponseDto;
import org.example.carsharingapp.dto.CarsResponseLiteDto;
import org.example.carsharingapp.exception.EntityNotFoundException;
import org.example.carsharingapp.mapper.CarMapper;
import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.CarType;
import org.example.carsharingapp.model.TypeName;
import org.example.carsharingapp.repository.CarRepository;
import org.example.carsharingapp.repository.CarTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarTypeRepository carTypeRepository;

    @Override
    public CarResponseDto addNewCar(CarRequestDto requestDto) {
        Car carFromRequest = carMapper.toModel(requestDto);
        Optional<Car> matchingCar = carRepository.findByModelAndBrandAndTypeAndDailyPrice(
                carFromRequest.getModel(),
                carFromRequest.getBrand(),
                carFromRequest.getType(),
                carFromRequest.getDailyPrice());

        if (matchingCar.isPresent()) {
            Car existCar = matchingCar.get();
            existCar.setAvailableCars(existCar.getAvailableCars() + requestDto.availableCars());
            carRepository.save(existCar);
            return carMapper.toDto(existCar);
        }

        carRepository.save(carFromRequest);
        return carMapper.toDto(carFromRequest);
    }

    @Override
    public Page<CarsResponseLiteDto> getAllCars(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toLiteDto);
    }

    @Override
    public CarResponseDto getSingleCarInfo(Long id) {
        Optional<Car> carById = carRepository.findById(id);
        Car car = carById.orElseThrow(
                () -> new EntityNotFoundException(
                        "CarService: Car with id: " + id + " not found"
                ));
        return carMapper.toDto(car);
    }

    @Override
    public CarResponseDto updateCarById(Long id, CarRequestDto requestDto) {
        Optional<Car> carById = carRepository.findById(id);
        Car car = carById.orElseThrow(
                () -> new EntityNotFoundException(
                        "CarService: Car with id: " + id + " not found"
                ));

        Optional<CarType> carTypeByName = carTypeRepository.findByTypeName(
                TypeName.valueOf(requestDto.type())
        );

        CarType carType = carTypeByName.orElseThrow(
                () -> new EntityNotFoundException(
                        "CarService: Car type: " + requestDto.type() + " not found"
                ));

        car.setModel(requestDto.model());
        car.setBrand(requestDto.brand());
        car.setType(carType);
        car.setAvailableCars(requestDto.availableCars());
        car.setDailyPrice(requestDto.dailyPrice());
        Car saved = carRepository.save(car);
        return carMapper.toDto(saved);
    }

    @Override
    public String deleteCarById(Long id) {
        carRepository.deleteById(id);
        return "Car with id: " + id + " was deleted";
    }
}
