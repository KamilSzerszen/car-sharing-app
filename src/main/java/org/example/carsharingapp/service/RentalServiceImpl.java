package org.example.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.RentalRequestDto;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.example.carsharingapp.exception.EntityNotFoundException;
import org.example.carsharingapp.exception.NoAvailableCarsException;
import org.example.carsharingapp.exception.ReturnRentalException;
import org.example.carsharingapp.mapper.RentalMapper;
import org.example.carsharingapp.model.*;
import org.example.carsharingapp.repository.CarRepository;
import org.example.carsharingapp.repository.RentalRepository;
import org.example.carsharingapp.repository.RoleRepository;
import org.example.carsharingapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserService userService;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService telegramService;


    @Override
    @Transactional
    public RentalResponseDto addNewRental(RentalRequestDto request) {
        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found"));

        List<Rental> activeRentals = rentalRepository.findActiveRentalsForCar(
                car, request.returnDate(), request.rentalDate()
        );

        int availableInPeriod = car.getAvailableCars() - activeRentals.size();
        if (availableInPeriod <= 0) {
            throw new NoAvailableCarsException("No cars available in the selected period");
        }

        User currentUser = userService.getCurrentUser();

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
        User userById = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(
                        "RentalService: User with id: " + userId + " not found"
                ));

        List<Rental> allByUserId = rentalRepository.findAllByUserId(userById.getId());
        List<RentalResponseDto> byParams = allByUserId.stream()
                .filter(
                        r -> isActive == null
                                || isActive ? r.getActualReturnDate() == null
                                : r.getActualReturnDate() != null
                )
                .map(rentalMapper::toDto)
                .toList();

        return new PageImpl<>(byParams, pageable, byParams.size());
    }

    @Override
    public Page<RentalResponseDto> getRentalByCurrentUser(Pageable pageable) {
        User currentUser = userService.getCurrentUser();

        List<Rental> allByUserId = rentalRepository.findAllByUserId(currentUser.getId());
        List<RentalResponseDto> rentalResponseDtos = allByUserId.stream()
                .map(rentalMapper::toDto)
                .toList();

        return new PageImpl<>(rentalResponseDtos, pageable, rentalResponseDtos.size());
    }

    @Override
    @Transactional
    public RentalResponseDto returnRentalByRentalId(Long id) {
        User currentUser = userService.getCurrentUser();
        Set<Role> currentUserRoles = currentUser.getRoles();

        Optional<Role> managerOptional = roleRepository.findByName(RoleName.ROLE_MANAGER);
        Role managerRole = managerOptional.orElseThrow(
                () -> new EntityNotFoundException(
                        "RoleService: Role with name: " + RoleName.ROLE_MANAGER + " not found"
                ));

        Optional<Role> customerOptional = roleRepository.findByName(RoleName.ROLE_CUSTOMER);
        Role customerRole = customerOptional.orElseThrow(
                () -> new EntityNotFoundException(
                        "RoleService: Role with name: " + RoleName.ROLE_CUSTOMER + " not found"
                ));

        Optional<Rental> rentalById = rentalRepository.findById(id);
        Rental rental = rentalById.orElseThrow(
                () -> new EntityNotFoundException(
                        "RentalService: Rental with id: " + id + " not found"
                ));

        Car rentalCar = rental.getCar();

        /*
         * User has role CUSTOMER cannot see other customer rental.
         */

        if (!currentUserRoles.contains(managerRole)) {

            if (!rental.getUser().getId().equals(currentUser.getId())) {
                throw new ReturnRentalException(
                        "ReturnRental: User "
                        + currentUser.getEmail()
                        + " does not have a rental with id: "
                        + id
                );
            }

            if (rental.getActualReturnDate() != null) {
                throw new ReturnRentalException(
                        "RentalService: Rental with id: "
                                + id
                                + " has returned: "
                                + rental.getActualReturnDate()
                );
            }
            
            rental.setActualReturnDate(LocalDateTime.now());

            Rental saved = rentalRepository.save(rental);

            return rentalMapper.toDto(saved);
        }
        
        rental.setActualReturnDate(LocalDateTime.now());
        rentalCar.setAvailableCars(rentalCar.getAvailableCars() + 1);

        Rental saved = rentalRepository.save(rental);
        carRepository.save(rentalCar);
        
        return rentalMapper.toDto(saved);
    }
}
