package org.example.carsharingapp.repository;

import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.Rental;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Should return existing rental for specific car in database")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-three-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add_default_rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void findActiveRentalsForCar_oneActiveRentals_returnListOfOneRental() {

        Optional<Car> carById = carRepository.findById(1L);
        Assertions.assertTrue(carById.isPresent());

        Car car = carById.get();
        LocalDateTime rentalDateTime = LocalDateTime.of(
                2025, 9, 11, 10, 0, 0
        );
        LocalDateTime returnDateTime = LocalDateTime.of(
                2025, 9, 15,18,0,0
        );

        List<Rental> activeRentalsForCar = rentalRepository
                .findActiveRentalsForCar(car, returnDateTime, rentalDateTime);

        Assertions.assertFalse(activeRentalsForCar.isEmpty());
        Assertions.assertEquals(1, activeRentalsForCar.size());
    }

    @Test
    @DisplayName("Should return existing rental for specific car in database")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-three-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add_default_rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void findAllByUserID_validId_returnListOfOneUser() {
        List<Rental> allByUserId = rentalRepository.findAllByUserId(1L);
        Assertions.assertFalse(allByUserId.isEmpty());
        Assertions.assertEquals(1, allByUserId.size());
    }
}
