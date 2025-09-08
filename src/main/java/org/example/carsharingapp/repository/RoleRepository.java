package org.example.carsharingapp.repository;

import jakarta.validation.constraints.NotNull;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(@NotNull RoleName name);
}
