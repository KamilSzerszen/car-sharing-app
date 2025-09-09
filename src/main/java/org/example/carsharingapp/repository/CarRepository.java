package org.example.carsharingapp.repository;

import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.CarType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByModelAndBrandAndTypeAndDailyPrice(
            String model,
            String brand,
            CarType type,
            BigDecimal dailyPrice);
}
