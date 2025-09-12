package org.example.carsharingapp.repository;

import org.example.carsharingapp.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
            SELECT p
            FROM Payment p
            WHERE p.rental.user.id = :userId
            """)
    Page<Payment> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
