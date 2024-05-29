package de.imi.mopat.validator;

import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.dto.AnswerDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link AnswerDTO AnswerDTO} objects for the Questiontype DATE.
 */
@Component
public class DateAnswerDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return AnswerDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (javax.validation
        // .constraints annotations)
        validator.validate(target, errors);

        AnswerDTO answerDTO = (AnswerDTO) target;

        SimpleDateFormat dateFormat = Constants.DATE_FORMAT;
        try {
            if (answerDTO.getStartDate() != null && !answerDTO.getStartDate().isEmpty()
                && answerDTO.getEndDate() != null && !answerDTO.getEndDate().isEmpty()) {

                Date startDate = dateFormat.parse(answerDTO.getStartDate());
                Date endDate = dateFormat.parse(answerDTO.getEndDate());

                if (startDate.equals(endDate)) {
                    errors.rejectValue("startDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("dateAnswer.validator" + ".startEqualsEnd",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }

                if (endDate.before(startDate)) {
                    errors.rejectValue("startDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("dateAnswer.validator" + ".endEarlierThanStart",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
            }
        } catch (ParseException ex) {
            // errors already catched before
        }
    }
}
