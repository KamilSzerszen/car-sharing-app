package org.example.carsharingapp.repository;

import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.Rental;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Query("""
            SELECT r 
            FROM Rental r 
            WHERE r.car = :car
              AND r.actualReturnDate IS NULL
              AND r.rentalDate < :requestReturnDate
              AND r.returnDate > :requestRentalDate
            """)
    List<Rental> findActiveRentalsForCar(
            @Param("car") Car car,
            @Param("requestReturnDate") LocalDateTime requestReturnDate,
            @Param("requestRentalDate") LocalDateTime requestRentalDate
    );

<<<<<<< HEAD
    @EntityGraph(attributePaths = {"user", "car"})
    List<Rental> findAllByUserId(@Param("userId") Long userId);
=======
    @Query("""
            SELECT r
            FROM Rental r
            WHERE r.actualReturnDate IS NULL
            AND r.returnDate <= :requestReturnDate
            """)
    List<Rental> findOverdueRentals(
            @Param("requestReturnDate") LocalDateTime requestReturnDate
    );

    @EntityGraph(attributePaths = {"user", "car"})
    List<Rental> findAllByUserId(@Param("userId") Long userId);

>>>>>>> 75ed46d (Create Payment: controller, service, repository, mapper. Add Stripe and Telegram API)
}
