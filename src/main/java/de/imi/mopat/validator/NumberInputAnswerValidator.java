package de.imi.mopat.validator;

import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link Question Question} objects.
 */
@Component
public class NumberInputAnswerValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return NumberInputAnswer.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        // [bt] now it's my time to validate the more complex stuff
        NumberInputAnswer numberInputAnswer = (NumberInputAnswer) target;
        try {
            Double minValue = numberInputAnswer.getMinValue();
            Double maxValue = numberInputAnswer.getMaxValue();

            // [sw] stepsize must not be <= 0
            if (numberInputAnswer.getStepsize() != null) {
                if (numberInputAnswer.getStepsize() <= 0) {
                    errors.rejectValue("stepsize", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage(
                            "numberInputAnswer" + ".validator" + ".stepsizeLowerEqualZero",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
            }

            // [sw] If either the minValue or the maxValue is null, no need
            // for further validation
            if (minValue == null || maxValue == null) {
                return;
            }

            if (minValue >= maxValue) { // [bt] validate that the min value is
                // lower than the max value
                errors.rejectValue("minValue", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("numberInputAnswer.validator" + ".minBiggerThanMax",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            } else {
                if (numberInputAnswer.getStepsize() != null) {
                    if (numberInputAnswer.getStepsize() > 0) {
                        BigDecimal differenceMaxMin = BigDecimal.valueOf(
                            Math.abs(maxValue - minValue));
                        if (BigDecimal.valueOf(numberInputAnswer.getStepsize())
                            .compareTo(differenceMaxMin)
                            > 0) { // [bt] validate that the step is not
                            // bigger than the difference between min value
                            // and max value
                            errors.rejectValue("stepsize", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                                messageSource.getMessage("numberInputAnswer.validator"
                                        + ".stepsizeBiggerThanDifferenceMaxMin", new Object[]{},
                                    LocaleContextHolder.getLocale()));
                        }
                        if (differenceMaxMin.remainder(
                                BigDecimal.valueOf(numberInputAnswer.getStepsize()))
                            .compareTo(new BigDecimal(0)) != 0) {
                            errors.rejectValue("stepsize", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                                messageSource.getMessage("numberInputAnswer.validator"
                                        + ".differenceMaxMinNotDivisibleByStepsize", new Object[]{},
                                    LocaleContextHolder.getLocale()));
                        }
                    }
                }
            }
        } catch (NumberFormatException ex) {
        }
    }
}