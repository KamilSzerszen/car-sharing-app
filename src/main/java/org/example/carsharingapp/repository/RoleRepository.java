package org.example.carsharingapp.repository;

import jakarta.validation.constraints.NotNull;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(@NotNull RoleName name);
}
