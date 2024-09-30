package de.imi.mopat.validator;

import de.imi.mopat.model.dto.AnswerDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.imi.mopat.model.dto.export.SliderIconDTO;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link AnswerDTO AnswerDTO} objects for the Questiontypes SLIDER,
 * NUMBER_CHECKBOX, NUMBER_CHECKBOX_TEXT.
 */
@Component
public class SliderAnswerDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SliderIconDTOValidator sliderIconDTOValidator;

    @Autowired
    private SliderIconConfigDTOValidator sliderIconConfigDTOValidator;

    @Override
    public boolean supports(final Class<?> type) {
        return AnswerDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        AnswerDTO sliderAnswer = (AnswerDTO) target;

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

            if (!sliderAnswer.getStepsize().matches("\\d+([,|\\.]{0,1}\\d*)?")) {
                errors.rejectValue("stepsize", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("sliderAnswer.validator" + ".stepsizeWrongPattern",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }

            Double stepsize = Double.parseDouble(sliderAnswer.getStepsize().replace(',', '.'));
            if (minValue != null && maxValue != null) {
                if (minValue >= maxValue) { // [bt] validate that the min value is
                    // lower than the max value
                    errors.rejectValue("minValue", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("sliderAnswer.validator" + ".minBiggerThanMax",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                } else {
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
                            > 0.00) { // [bt] validate that the step is
                            // not bigger than the difference between min
                            // value and max value
                            errors.rejectValue("stepsize", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                                messageSource.getMessage("sliderAnswer.validator"
                                        + ".stepsizeBiggerThanDifferenceMaxMin", new Object[]{},
                                    LocaleContextHolder.getLocale()));
                        }
                        if (differenceMaxMin.remainder(BigDecimal.valueOf(stepsize))
                            .compareTo(new BigDecimal(0)) != 0) {
                            errors.rejectValue("stepsize", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                                messageSource.getMessage("sliderAnswer.validator"
                                        + ".differenceMaxMinNotDivisibleByStepsize", new Object[]{},
                                    LocaleContextHolder.getLocale()));
                        }
                    }
                }
            }
            if (Objects.equals(sliderAnswer.getSliderIconConfigDTO().getConfigType(), "newConfig")
                || Objects.equals(sliderAnswer.getSliderIconConfigDTO().getConfigType(),
                "oldConfig")) {
                errors.pushNestedPath("sliderIconConfigDTO");
                sliderIconConfigDTOValidator.validate(sliderAnswer.getSliderIconConfigDTO(), errors);
                errors.popNestedPath();


            } else if (sliderAnswer.getShowIcons() != null && sliderAnswer.getShowIcons()) {
                for (int i = 0; i < sliderAnswer.getIcons().size(); i++) {
                    SliderIconDTO sliderIconDTO = sliderAnswer.getIcons().get(i);
                    // tell the errors object that from now on the validation
                    // refers to correct icon object.
                    errors.pushNestedPath("icons[" + i + "]");
                    sliderIconDTOValidator.validateWithAnswer(sliderIconDTO, errors, sliderAnswer);
                    // Pop the current icons path to iterate over all
                    // SliderIconDTOs seperately
                    errors.popNestedPath();
                }
            }
        } catch (NumberFormatException ex) {
        }
    }
}
