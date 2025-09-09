package org.example.carsharingapp.model;

import java.util.Optional;

public enum TypeName {
    SEDAN,
    SUV,
    HATCHBACK;

    public static Optional<TypeName> fromString(String name) {
        try {
            return Optional.of(TypeName.valueOf(name.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("TypeName: Invalid type name: " + name);
        }
    }
}
