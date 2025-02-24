package de.imi.mopat.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.SimpleErrors;
import org.springframework.validation.Validator;

public class ConditionalValidatorDispatcher implements ConstraintValidator<ConditionalValidation, Object> {

    @Autowired
    private CreateReviewValidator createReviewValidator;


    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        Validator validator = getValidatorFor(target);
        if (validator == null) {
            return true;
        }

        Errors errors = new SimpleErrors(target);
        validator.validate(target, errors);

        if (!errors.hasErrors()) {
            return true;
        }

        for (var error : errors.getAllErrors()) {
            context.disableDefaultConstraintViolation();

            if (error instanceof FieldError fieldError) {
                context.buildConstraintViolationWithTemplate(fieldError.getDefaultMessage())
                        .addPropertyNode(fieldError.getField())
                        .addConstraintViolation();
            } else {
                context.buildConstraintViolationWithTemplate(error.getDefaultMessage())
                        .addConstraintViolation();
            }
        }
        return false;
    }

    private Validator getValidatorFor(Object target) {
        if (createReviewValidator.supports(target.getClass())) {
            return createReviewValidator;
        }
        return null;
    }
}