package org.example.carsharingapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.model.Rental;
import org.example.carsharingapp.repository.RentalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueRentalChecker {

    private final RentalRepository rentalRepository;
    private final NotificationService telegramService;

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkOverdueRental() {
        LocalDateTime currentDay = LocalDateTime.now();

        List<Rental> overdueRentals = rentalRepository.findOverdueRentals(currentDay);

        if (overdueRentals.isEmpty()) {
            telegramService.sendNotification("No rentals overdue today!");
        } else {
            for (Rental rental : overdueRentals) {
                String msg = "Rentals overdue! \n"
                        + "User: " + rental.getUser().getEmail() + "\n"
                        + "Car: " + rental.getCar() + "\n"
                        + "Return date: " + rental.getReturnDate();

                telegramService.sendNotification(msg);
            }
        }
    }
}
