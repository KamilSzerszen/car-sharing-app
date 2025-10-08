package org.example.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.RentalRequestDto;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.example.carsharingapp.exception.EntityNotFoundException;
import org.example.carsharingapp.exception.NoAvailableCarsException;
import org.example.carsharingapp.exception.PaymentException;
import org.example.carsharingapp.exception.ReturnRentalException;
import org.example.carsharingapp.mapper.RentalMapper;
import org.example.carsharingapp.model.*;
import org.example.carsharingapp.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service("rentalServiceImpl")
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserService userService;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService telegramService;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;


    @Override
    @Transactional
    public RentalResponseDto addNewRental(RentalRequestDto request) {
        User currentUser = userService.getCurrentUser();

        List<Payment> allByUserId = paymentRepository.findAllByUserId(currentUser.getId());

        PaymentStatus pendingStatus = paymentStatusRepository
                .findByPaymentStatusName(PaymentStatusName.PENDING)
                .orElseThrow(
                () -> new EntityNotFoundException("RentalService: No payment status found")
        );

        for (Payment payment : allByUserId) {
            if (payment.getStatus().equals(pendingStatus)) {
                throw new PaymentException("User: " + currentUser + " already has unpaid payment");
            }
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found"));

        List<Rental> activeRentals = rentalRepository.findActiveRentalsForCar(
                car, request.returnDate(), request.rentalDate()
        );

        int availableInPeriod = car.getAvailableCars() - activeRentals.size();
        if (availableInPeriod <= 0) {
            throw new NoAvailableCarsException("No cars available in the selected period");
        }

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setUser(currentUser);
        rental.setRentalDate(request.rentalDate());
        rental.setReturnDate(request.returnDate());

        Rental savedRental = rentalRepository.save(rental);
        Rental rentalWithDetails = rentalRepository.findById(savedRental.getId())
                .orElseThrow(() -> new EntityNotFoundException("Rental after save not found"));

        telegramService.sendNotification(
                "New rental:" + "\n"
                + "Car: " + car.getBrand() + "\n"
                + "Model: " + car.getModel() + "\n"
                + "Date: " + rental.getRentalDate() + "\n"
                + "Return date: " + rental.getReturnDate() + "\n"
                + "User: " + currentUser.getEmail()
        );

        return rentalMapper.toDto(rentalWithDetails);
    }

    @Override
    public Page<RentalResponseDto> getRentalsByUserIdAndIsActive(
            Long userId, Boolean isActive, Pageable pageable
    ) {
        Page<Rental> rentalPage;

        if (isActive == null) {
            rentalPage = rentalRepository
                    .findAllByUserId(userId, pageable);
        } else if (isActive) {
            rentalPage = rentalRepository
                    .findAllByUserIdAndActualReturnDateIsNull(userId, pageable);
        } else {
            rentalPage = rentalRepository
                    .findAllByUserIdAndActualReturnDateIsNotNull(userId, pageable);
        }

        return rentalPage.map(rentalMapper::toDto);
    }

    @Override
    public Page<RentalResponseDto> getRentalByCurrentUser(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Rental> rentalPage = rentalRepository.findAllByUserId(currentUser.getId(), pageable);

        return rentalPage.map(rentalMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('MANAGER') or @rentalServiceImpl.isOwner(#id, principal.username)")
    public RentalResponseDto returnRentalByRentalId(Long id) {

        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("RentalService: No rental found")
        );

        rental.setActualReturnDate(LocalDateTime.now());
        Rental saved = rentalRepository.save(rental);
        
        return rentalMapper.toDto(saved);
    }

    public boolean isOwner(Long rentalId, String username) {
        return rentalRepository.findById(rentalId)
                .map(r -> r.getUser().getEmail().equals(username))
                .orElse(false);
    }
}
