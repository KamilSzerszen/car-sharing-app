package org.example.carsharingapp.service;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private CarTypeRepository carTypeRepository;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Should added new car")
    public void addNewCar_validRequest_returnCarResponseDto() throws Exception {
        CarRequestDto request = new CarRequestDto(
                "test",
                "test",
                "SUV",
                5,
                BigDecimal.TEN
        );

        CarType carType = new CarType();
        carType.setId(1L);
        carType.setTypeName(TypeName.SUV);

        Car car = new Car();
        car.setId(1L);
        car.setModel("test");
        car.setBrand("test");
        car.setType(carType);
        car.setDailyPrice(BigDecimal.TEN);

        CarResponseDto expected = new CarResponseDto(
                1L,
                "test",
                "test",
                "SUV",
                5,
                BigDecimal.TEN
        );

        Mockito.when(carMapper.toModel(request)).thenReturn(car);
        Mockito.when(carRepository.findByModelAndBrandAndTypeAndDailyPrice(
                car.getModel(),
                car.getBrand(),
                car.getType(),
                car.getDailyPrice()
        )).thenReturn(Optional.empty());
        Mockito.when(carRepository.save(car)).thenReturn(car);
        Mockito.when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto result = carService.addNewCar(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals(expected.model(), result.model());
        Assertions.assertEquals(expected.brand(), result.brand());
        Assertions.assertEquals(expected.type(), result.type());
        Assertions.assertEquals(expected.dailyPrice(), result.dailyPrice());
    }

    @Test
    @DisplayName("Should return all cars with pagination")
    public void getAllCars_validPageable_returnCarsResponseLiteDtoPage() {
        Pageable pageable = Pageable.ofSize(2);

        Car car = new Car();
        car.setId(1L);
        car.setModel("test");
        car.setBrand("test");
        car.setAvailableCars(5);

        CarsResponseLiteDto liteDto = new CarsResponseLiteDto(
                1L,
                "test",
                "test"
        );

        Page<Car> page = new PageImpl<>(List.of(car));

        Mockito.when(carRepository.findAll(pageable)).thenReturn(page);
        Mockito.when(carMapper.toLiteDto(car)).thenReturn(liteDto);

        Page<CarsResponseLiteDto> result = carService.getAllCars(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(liteDto.id(), result.getContent().get(0).id());
        Assertions.assertEquals(liteDto.model(), result.getContent().get(0).model());
        Assertions.assertEquals(liteDto.brand(), result.getContent().get(0).brand());
    }

    @Test
    @DisplayName("Should return single car info by id")
    public void getSingleCarInfo_validId_returnCarResponseDto() {
        Long id = 1L;

        Car car = new Car();
        car.setId(id);
        car.setModel("test");
        car.setBrand("test");

        CarResponseDto expected = new CarResponseDto(
                id,
                "test",
                "test",
                "SUV",
                5,
                BigDecimal.TEN
        );

        Mockito.when(carRepository.findById(id)).thenReturn(Optional.of(car));
        Mockito.when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto result = carService.getSingleCarInfo(id);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals(expected.model(), result.model());
        Assertions.assertEquals(expected.brand(), result.brand());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when car not found by id")
    public void getSingleCarInfo_invalidId_throwEntityNotFoundException() {
        Long id = 99L;

        Mockito.when(carRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                EntityNotFoundException.class, () -> carService.getSingleCarInfo(id)
        );
    }

    @Test
    @DisplayName("Should update car by id with valid request")
    public void updateCarById_validRequest_returnCarResponseDto() {
        Long id = 1L;

        CarRequestDto request = new CarRequestDto(
                "newModel",
                "newBrand",
                "SUV",
                10,
                BigDecimal.ONE
        );

        Car car = new Car();
        car.setId(id);
        car.setModel("old");
        car.setBrand("old");

        CarType carType = new CarType();
        carType.setId(1L);
        carType.setTypeName(TypeName.SUV);

        CarResponseDto expected = new CarResponseDto(
                id,
                "newModel",
                "newBrand",
                "SUV",
                10,
                BigDecimal.ONE
        );

        Mockito.when(carRepository.findById(id)).thenReturn(Optional.of(car));
        Mockito.when(carTypeRepository
                        .findByTypeName(TypeName.SUV)).thenReturn(Optional.of(carType));
        Mockito.when(carRepository.save(Mockito.any(Car.class))).thenReturn(car);
        Mockito.when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto result = carService.updateCarById(id, request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals(expected.model(), result.model());
        Assertions.assertEquals(expected.brand(), result.brand());
        Assertions.assertEquals(expected.type(), result.type());
        Assertions.assertEquals(expected.availableCars(), result.availableCars());
        Assertions.assertEquals(expected.dailyPrice(), result.dailyPrice());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating car with invalid id")
    public void updateCarById_invalidId_throwEntityNotFoundException() {
        Long id = 99L;

        CarRequestDto request = new CarRequestDto(
                "newModel",
                "newBrand",
                "SUV",
                10,
                BigDecimal.ONE
        );

        Mockito.when(carRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                EntityNotFoundException.class, () -> carService.updateCarById(id, request)
        );
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when car type not found during update")
    public void updateCarById_invalidCarType_throwEntityNotFoundException() {
        Long id = 1L;

        CarRequestDto request = new CarRequestDto(
                "newModel",
                "newBrand",
                "SUV",
                10,
                BigDecimal.ONE
        );

        Car car = new Car();
        car.setId(id);

        Mockito.when(carRepository.findById(id)).thenReturn(Optional.of(car));
        Mockito.when(carTypeRepository
                        .findByTypeName(TypeName.SUV)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                EntityNotFoundException.class, () -> carService.updateCarById(id, request)
        );
    }

    @Test
    @DisplayName("Should delete car by id")
    public void deleteCarById_validId_returnConfirmationMessage() {
        Long id = 1L;

        String expected = "Car with id: " + id + " was deleted";

        String result = carService.deleteCarById(id);

        Mockito.verify(carRepository, Mockito.times(1)).deleteById(id);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected, result);
    }

}
