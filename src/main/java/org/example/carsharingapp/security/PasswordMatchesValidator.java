package org.example.carsharingapp.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.carsharingapp.dto.UserRegistrationRequestDto;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.security.annotation.PasswordMatches;

public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, UserRegistrationRequestDto> {

    @Override
    public boolean isValid(UserRegistrationRequestDto value, ConstraintValidatorContext context) {
        if (value.password() == null || value.repeatPassword() == null) {
            return false;
        }
        return value.password().equals(value.repeatPassword());
    }
}
