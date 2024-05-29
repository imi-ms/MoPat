package de.imi.mopat.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 *
 */
public class DateFormatValidatorForString implements
    ConstraintValidator<DateFormatForString, String> {

    private String format;

    @Override
    public void initialize(final DateFormatForString constraintAnnotation) {
        format = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(final String date,
        final ConstraintValidatorContext constraintValidatorContext) {
        if (date == null || date.isEmpty()) {
            return true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}