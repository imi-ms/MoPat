package de.imi.mopat.validator;

import de.imi.mopat.model.dto.AnswerDTO;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link AnswerDTO AnswerDTO} objects for the Questiontypes MULTIPLE_CHOICE,
 * DROP_DOWN.
 */
@Component
public class SelectAnswerDTOValidator implements Validator {

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
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        // [bt] now it's my time to validate the more complex stuff
        AnswerDTO selectAnswer = (AnswerDTO) target;

        // [sw] Check if any added language contains an empty localized label
        for (Map.Entry<String, String> entry : selectAnswer.getLocalizedLabel().entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty() || Pattern.matches(
                "<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>", entry.getValue())) {
                selectAnswer.getLocalizedLabel().put(entry.getKey(), "");
                errors.rejectValue("localizedLabel[" + entry.getKey() + "]",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("selectAnswer.validator" + ".labelNotNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
    }
}
