package de.imi.mopat.validator;

import de.imi.mopat.model.Question;
import de.imi.mopat.model.SelectAnswer;

import java.util.Map;

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
public class SelectAnswerValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return SelectAnswer.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        // [bt] now it's my time to validate the more complex stuff
        SelectAnswer selectAnswer = (SelectAnswer) target;
        // [sw] Check if any added language contains an empty localized label
        for (Map.Entry<String, String> entry : selectAnswer.getLocalizedLabel().entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty() || entry.getValue()
                .matches("<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>")) {
                selectAnswer.getLocalizedLabel().put(entry.getKey(), "");
                errors.rejectValue("localizedLabel[" + entry.getKey() + "]",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("selectAnswer.validator" + ".labelNotNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
    }
}
