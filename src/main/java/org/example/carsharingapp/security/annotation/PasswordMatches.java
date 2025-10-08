package org.example.carsharingapp.security.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.carsharingapp.security.PasswordMatchesValidator;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface PasswordMatches {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
