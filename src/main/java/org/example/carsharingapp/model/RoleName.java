package org.example.carsharingapp.model;

import java.util.Optional;

public enum RoleName {
    ROLE_MANAGER,
    ROLE_CUSTOMER;

    public static Optional<RoleName> fromString(String value) {
        try {
            return Optional.of(RoleName.valueOf("ROLE_" + value.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("RoleName: Invalid role name: " + value);
        }
    }
}
