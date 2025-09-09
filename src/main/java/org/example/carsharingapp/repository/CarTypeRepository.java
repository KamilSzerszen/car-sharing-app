package org.example.carsharingapp.repository;

import jakarta.validation.constraints.NotNull;
import org.example.carsharingapp.model.CarType;
import org.example.carsharingapp.model.TypeName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarTypeRepository extends JpaRepository<CarType, Long> {
    Optional<CarType> findByTypeName(@NotNull TypeName typeName);
}
