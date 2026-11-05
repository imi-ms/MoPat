package de.imi.mopat.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for conditional validation that uses different validators
 * depending on the target class (DTO).
 */
@Constraint(validatedBy = ConditionalValidatorDispatcher.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalValidation {
    /**
     * Standard error message.
     */
    String message() default "Invalid input";

    /**
     * Groups for validation.
     */
    Class<?>[] groups() default {};

    /**
     * Payload to pass additional metadata.
     */
    Class<? extends Payload>[] payload() default {};
}