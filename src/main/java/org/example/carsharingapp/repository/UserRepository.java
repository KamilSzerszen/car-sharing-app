package org.example.carsharingapp.repository;

import jakarta.validation.constraints.NotNull;
import org.example.carsharingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(@NotNull(message = "Email cannot be null") String email);
}
