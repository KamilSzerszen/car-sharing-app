package org.example.carsharingapp.repository;

import jakarta.validation.constraints.NotNull;
import org.example.carsharingapp.model.PaymentType;
import org.example.carsharingapp.model.PaymentTypeName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTypeRepository extends JpaRepository<PaymentType, Long> {

    Optional<PaymentType> findByPaymentTypeName(@NotNull PaymentTypeName paymentTypeName);
}
