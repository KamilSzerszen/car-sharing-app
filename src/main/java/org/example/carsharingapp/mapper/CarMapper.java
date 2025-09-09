package org.example.carsharingapp.mapper;

import org.example.carsharingapp.dto.CarRequestDto;
import org.example.carsharingapp.dto.CarResponseDto;
import org.example.carsharingapp.dto.CarsResponseLiteDto;
import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.CarType;
import org.example.carsharingapp.model.TypeName;
import org.example.carsharingapp.repository.CarTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CarMapper {

    @Autowired
    private CarTypeRepository carTypeRepository;

    public Car toModel(CarRequestDto requestDto) {
        Car car = new Car();
        car.setModel(requestDto.model());
        car.setBrand(requestDto.brand());

        Optional<TypeName> typeNameByRequest = TypeName.fromString(requestDto.type());

        TypeName typeName = typeNameByRequest.orElseThrow(
                () -> new IllegalArgumentException("Invalid type name: " + requestDto.type()));

        CarType carType = carTypeRepository.findByTypeName(typeName).orElseThrow(
                () -> new IllegalArgumentException("Invalid car type: " + requestDto.type()));

        car.setType(carType);
        car.setAvailableCars(requestDto.availableCars());
        car.setDailyPrice(requestDto.dailyPrice());
        return car;
    }

    public CarResponseDto toDto(Car car) {
        CarResponseDto dto = new CarResponseDto(
                car.getId(),
                car.getModel(),
                car.getBrand(),
                car.getType() != null ? car.getType().getTypeName().toString() : "",
                car.getAvailableCars(),
                car.getDailyPrice()
        );
        return dto;
    }

    public CarsResponseLiteDto toLiteDto(Car car) {
        return new CarsResponseLiteDto(
                car.getId(),
                car.getModel(),
                car.getBrand()
        );
    }
}
