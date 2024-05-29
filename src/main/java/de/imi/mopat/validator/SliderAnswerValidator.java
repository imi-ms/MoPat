package de.imi.mopat.validator;

import de.imi.mopat.model.Question;
import de.imi.mopat.model.SliderAnswer;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
public class SliderAnswerValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(Class<?> type) {
        return SliderAnswer.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        SliderAnswer sliderAnswer = (SliderAnswer) target;

        try {
            Double minValue = sliderAnswer.getMinValue();
            Double maxValue = sliderAnswer.getMaxValue();
            if (minValue == null) {
                errors.rejectValue("minValue", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("sliderAnswer.validator" + ".minValueNotNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
            if (maxValue == null) {
                errors.rejectValue("maxValue", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("sliderAnswer.validator" + ".maxValueNotNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
            Double stepsize = sliderAnswer.getStepsize();
            if (minValue != null && maxValue != null) {
                if (minValue >= maxValue) { // [bt] validate that the min value is
                    // lower than the max value
                    errors.rejectValue("minValue", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("sliderAnswer.validator" + ".minBiggerThanMax",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                } else {
                    if (sliderAnswer.getStepsize() != null) {
                        if (stepsize <= 0) {
                            errors.rejectValue("stepsize", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                                messageSource.getMessage(
                                    "sliderAnswer.validator" + ".stepsizeLowerEqualZero",
                                    new Object[]{}, LocaleContextHolder.getLocale()));
                        } else {
                            BigDecimal unroundedDifferenceMaxMin = BigDecimal.valueOf(
                                Math.abs(maxValue - minValue));
                            // Round the difference to two decimal places
                            BigDecimal differenceMaxMin = unroundedDifferenceMaxMin.setScale(2,
                                RoundingMode.HALF_UP);
                            if (BigDecimal.valueOf(stepsize).compareTo(differenceMaxMin)
                                > 0.00) { // [bt] validate that the step
                                // is not bigger than the difference between
                                // min value and max value
                                errors.rejectValue("stepsize",
                                    MoPatValidator.ERRORCODE_ERRORMESSAGE, messageSource.getMessage(
                                        "sliderAnswer.validator"
                                            + ".stepsizeBiggerThanDifferenceMaxMin", new Object[]{},
                                        LocaleContextHolder.getLocale()));
                            }
                            if (differenceMaxMin.remainder(BigDecimal.valueOf(stepsize))
                                .compareTo(new BigDecimal(0)) != 0) {
                                errors.rejectValue("stepsize",
                                    MoPatValidator.ERRORCODE_ERRORMESSAGE, messageSource.getMessage(
                                        "sliderAnswer.validator"
                                            + ".differenceMaxMinNotDivisibleByStepsize",
                                        new Object[]{}, LocaleContextHolder.getLocale()));
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException ex) {
        }
    }
}
