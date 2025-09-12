package org.example.carsharingapp.repository;

import jakarta.validation.constraints.NotNull;
import org.example.carsharingapp.model.PaymentStatus;
import org.example.carsharingapp.model.PaymentStatusName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {
    Optional<PaymentStatus> findByPaymentStatusName(@NotNull PaymentStatusName paymentStatusName);
}
