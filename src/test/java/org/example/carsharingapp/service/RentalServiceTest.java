package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.RentalRequestDto;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.example.carsharingapp.exception.NoAvailableCarsException;
import org.example.carsharingapp.exception.ReturnRentalException;
import org.example.carsharingapp.mapper.RentalMapper;
import org.example.carsharingapp.model.*;
import org.example.carsharingapp.repository.CarRepository;
import org.example.carsharingapp.repository.PaymentRepository;
import org.example.carsharingapp.repository.PaymentStatusRepository;
import org.example.carsharingapp.repository.RentalRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStatusRepository paymentStatusRepository;

    @Mock
    private TelegramNotificationService telegramNotificationService;

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

        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.setId(1L);
        paymentStatus.setPaymentStatusName(PaymentStatusName.PAID);

        RentalResponseDto expected = new RentalResponseDto(
                1L, start, end, "Toyota", "Corolla",
                "test@test.com", null, null, "true"
        );

        Mockito.when(paymentRepository.findAllByUserId(user.getId()))
                .thenReturn(Collections.emptyList());
        Mockito.when(paymentStatusRepository.findByPaymentStatusName(PaymentStatusName.PENDING))
                .thenReturn(Optional.of(paymentStatus));
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

        User mockUser = new User();
        mockUser.setId(1L);

        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.setId(1L);
        paymentStatus.setPaymentStatusName(PaymentStatusName.PAID);


        Mockito.when(userService.getCurrentUser()).thenReturn(mockUser);
        Mockito.when(paymentStatusRepository.findByPaymentStatusName(PaymentStatusName.PENDING))
                .thenReturn(Optional.of(paymentStatus));
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

        RentalResponseDto dto = new RentalResponseDto(
                1L, rental.getRentalDate(), rental.getReturnDate(),
                "BMW",
                "X5",
                "user@test.com",
                "Test",
                "User",
                "true"
        );

        Page<Rental> rentalPage = new PageImpl<>(List.of(rental));



        Mockito.when(rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                1L, Pageable.ofSize(5)))
                .thenReturn(rentalPage);
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

        RentalResponseDto dto = new RentalResponseDto(
                1L, rental.getRentalDate(), rental.getReturnDate(),
                "Audi",
                "A4",
                "current@test.com",
                "Current",
                "User",
                "true"
        );

        Page<Rental> rentalPage = new PageImpl<>(List.of(rental));

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(rentalRepository.findAllByUserId(user.getId(), Pageable.ofSize(5)))
                .thenReturn(rentalPage);
        Mockito.when(rentalMapper.toDto(rental)).thenReturn(dto);

        Page<RentalResponseDto> result = rentalService.getRentalByCurrentUser(Pageable.ofSize(5));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Audi", result.getContent().get(0).brand());
    }

    @Test
    @DisplayName("Should return rental when owner returns it")
    public void returnRentalByRentalId_owner_returnsRentalResponseDto() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(user);

        RentalResponseDto expected = new RentalResponseDto(
                1L, rental.getRentalDate(), rental.getReturnDate(),
                "Ford",
                "Focus",
                "user@test.com",
                null,
                null,
                "false"
        );

        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        Mockito.when(rentalRepository.save(Mockito.any(Rental.class))).thenReturn(rental);
        Mockito.when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponseDto result = rentalService.returnRentalByRentalId(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
    }

    @Test
    @DisplayName("Should return false for isOwner if user is not owner")
    public void isOwner_notOwner_returnsFalse() {
        User user = new User();
        user.setEmail("user1@test.com");

        User rentalOwner = new User();
        rentalOwner.setEmail("owner@test.com");

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(rentalOwner);

        Mockito.when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        boolean result = rentalService.isOwner(1L, user.getEmail());

        Assertions.assertFalse(result);
    }
}
