package de.imi.mopat.validator;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.dto.ConditionDTO;
import de.imi.mopat.model.dto.ConditionListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link ConditionListDTO} objects.
 */
@Component
public class ConditionListDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private AnswerDao answerDao;

    @Override
    public boolean supports(final Class<?> type) {
        return ConditionListDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (javax.validation
        // .constraints annotations)
        validator.validate(target, errors);

        ConditionListDTO conditionListDTO = (ConditionListDTO) target;
        ConditionDTO conditionDTO = conditionListDTO.getConditionDTOs().get(0);

        if (conditionDTO.getTriggerId() == null) {
            errors.rejectValue("conditionDTOs[0].triggerId", MoPatValidator.ERRORCODE_NOT_NULL,
                messageSource.getMessage("condition.error.triggerIdIsNull", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }
        if (!(answerDao.getElementById(conditionDTO.getTriggerId()) instanceof SelectAnswer)) {
            if (conditionDTO.getThresholdType() == null) {
                errors.rejectValue("conditionDTOs[0].thresholdType",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("condition.error" + ".thresholdTypeIsNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
            if (conditionDTO.getThresholdValue() == null) {
                errors.rejectValue("conditionDTOs[0].thresholdValue",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("condition.error" + ".thresholdValueIsNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }

    }

}
