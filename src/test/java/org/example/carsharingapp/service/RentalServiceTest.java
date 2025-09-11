package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.RentalRequestDto;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.example.carsharingapp.exception.NoAvailableCarsException;
import org.example.carsharingapp.exception.ReturnRentalException;
import org.example.carsharingapp.mapper.RentalMapper;
import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.Rental;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.example.carsharingapp.model.User;
import org.example.carsharingapp.repository.CarRepository;
import org.example.carsharingapp.repository.RentalRepository;
import org.example.carsharingapp.repository.RoleRepository;
import org.example.carsharingapp.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserService userService;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Should add new rental when car is available")
    public void addNewRental_validRequest_returnRentalResponseDto() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        RentalRequestDto request = new RentalRequestDto(start, end, 1L);

        Car car = new Car();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setAvailableCars(2);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(start);
        rental.setReturnDate(end);

        RentalResponseDto expected = new RentalResponseDto(
                1L, start, end, "Toyota", "Corolla",
                "test@test.com", null, null, "true"
        );

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        Mockito.when(rentalRepository.findActiveRentalsForCar(car, end, start))
                .thenReturn(Collections.emptyList());
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(rentalRepository.save(Mockito.any(Rental.class))).thenReturn(rental);
        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        Mockito.when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponseDto result = rentalService.addNewRental(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals(expected.brand(), result.brand());
        Assertions.assertEquals(expected.model(), result.model());
    }

    @Test
    @DisplayName("Should throw NoAvailableCarsException when no cars left for period")
    public void addNewRental_noCars_throwNoAvailableCarsException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        RentalRequestDto request = new RentalRequestDto(start, end, 1L);

        Car car = new Car();
        car.setId(1L);
        car.setAvailableCars(1);

        Mockito.when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        Mockito.when(rentalRepository.findActiveRentalsForCar(car, end, start))
                .thenReturn(List.of(new Rental()));

        Assertions.assertThrows(NoAvailableCarsException.class,
                () -> rentalService.addNewRental(request));
    }

    @Test
    @DisplayName("Should return rentals by user id and active flag")
    public void getRentalsByUserIdAndIsActive_validRequest_returnPage() {
        User user = new User();
        user.setId(1L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(user);
        rental.setRentalDate(LocalDateTime.now());
        rental.setReturnDate(LocalDateTime.now().plusDays(1));
        rental.setActualReturnDate(null);

        RentalResponseDto dto = new RentalResponseDto(
                1L, rental.getRentalDate(), rental.getReturnDate(),
                "BMW",
                "X5",
                "user@test.com",
                "Test",
                "User",
                "true"
        );

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(rentalRepository.findAllByUserId(1L)).thenReturn(List.of(rental));
        Mockito.when(rentalMapper.toDto(rental)).thenReturn(dto);

        Page<RentalResponseDto> result = rentalService.getRentalsByUserIdAndIsActive(
                1L, true, Pageable.ofSize(5)
        );

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("BMW", result.getContent().get(0).brand());
    }

    @Test
    @DisplayName("Should return rentals for current user")
    public void getRentalByCurrentUser_validUser_returnPage() {
        User user = new User();
        user.setId(1L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(user);
        rental.setRentalDate(LocalDateTime.now());
        rental.setReturnDate(LocalDateTime.now().plusDays(1));

        RentalResponseDto dto = new RentalResponseDto(
                1L, rental.getRentalDate(), rental.getReturnDate(),
                "Audi",
                "A4",
                "current@test.com",
                "Current",
                "User",
                "true"
        );

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(rentalRepository.findAllByUserId(1L)).thenReturn(List.of(rental));
        Mockito.when(rentalMapper.toDto(rental)).thenReturn(dto);

        Page<RentalResponseDto> result = rentalService
                .getRentalByCurrentUser(Pageable.ofSize(5));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Audi", result.getContent().get(0).brand());
    }

    @Test
    @DisplayName("Should return rental when manager returns it")
    public void returnRentalByRentalId_managerRole_returnRentalResponseDto() {
        User manager = new User();
        manager.setId(1L);
        manager.setEmail("manager@test.com");
        Role managerRole = new Role();
        managerRole.setName(RoleName.ROLE_MANAGER);
        manager.setRoles(Set.of(managerRole));

        Car car = new Car();
        car.setId(1L);
        car.setBrand("Ford");
        car.setModel("Focus");
        car.setAvailableCars(1);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCar(car);
        rental.setUser(manager);
        rental.setRentalDate(LocalDateTime.now().minusDays(2));
        rental.setReturnDate(LocalDateTime.now().minusDays(1));

        RentalResponseDto expected = new RentalResponseDto(
                1L, rental.getRentalDate(), rental.getReturnDate(),
                "Ford",
                "Focus",
                "manager@test.com",
                null,
                null,
                "false"
        );

        Mockito.when(userService.getCurrentUser()).thenReturn(manager);
        Mockito.when(roleRepository.findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(managerRole));
        Mockito.when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(new Role()));
        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        Mockito.when(rentalRepository.save(Mockito.any(Rental.class))).thenReturn(rental);
        Mockito.when(carRepository.save(Mockito.any(Car.class))).thenReturn(car);
        Mockito.when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponseDto result = rentalService.returnRentalByRentalId(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals("Ford", result.brand());
    }

    @Test
    @DisplayName("Should throw ReturnRentalException when customer tries to return other's rental")
    public void returnRentalByRentalId_customerNotOwner_throwReturnRentalException() {
        User customer = new User();
        customer.setId(1L);
        customer.setEmail("cust@test.com");
        Role customerRole = new Role();
        customerRole.setName(RoleName.ROLE_CUSTOMER);
        customer.setRoles(Set.of(customerRole));

        User rentalOwner = new User();
        rentalOwner.setId(2L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(rentalOwner);

        Mockito.when(userService.getCurrentUser()).thenReturn(customer);
        Mockito.when(roleRepository.findByName(RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(new Role()));
        Mockito.when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(customerRole));
        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        Assertions.assertThrows(ReturnRentalException.class,
                () -> rentalService.returnRentalByRentalId(1L));
    }
}
