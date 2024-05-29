package de.imi.mopat.validator;

import java.lang.annotation.Documented;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 *
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = DateFormatValidatorForString.class)
@Documented
public @interface DateFormatForString {

    String message() default "Date format is wrong";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value();
}