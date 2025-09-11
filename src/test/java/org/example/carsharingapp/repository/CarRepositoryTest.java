package org.example.carsharingapp.repository;

import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.CarType;
import org.example.carsharingapp.model.TypeName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import java.math.BigDecimal;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Should return existing car in database")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void findByModelAndBrandAndTypeAndDailyPrice_carExists_returnsCar() {

        CarType carType = new CarType();
        carType.setId(1L);
        carType.setTypeName(TypeName.SUV);

        Optional<Car> byModelAndBrandAndTypeAndDailyPrice = carRepository.findByModelAndBrandAndTypeAndDailyPrice(
                "test",
                "test",
                carType,
                BigDecimal.TEN
        );

        Assertions.assertTrue(byModelAndBrandAndTypeAndDailyPrice.isPresent());

        Car car = byModelAndBrandAndTypeAndDailyPrice.get();
        Assertions.assertNotNull(car);
        Assertions.assertEquals("test", car.getBrand());
        Assertions.assertEquals(0, car.getDailyPrice().compareTo(BigDecimal.TEN));
        Assertions.assertInstanceOf(CarType.class, car.getType());
    }
}
