package de.imi.mopat.validator;

import de.imi.mopat.controller.forms.ReviewDecisionForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ReviewDecisionValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(Class<?> clazz) {
        return ReviewDecisionForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof ReviewDecisionForm form)) {
            return;
        }

        if (form.action().equalsIgnoreCase("reject") && (form.description() == null || form.description().isBlank())) {
            addError(errors, "description", "review.error.description.empty.for.reject");
        }

        if (form.action().equalsIgnoreCase("review") && (form.description() == null || form.description().isBlank())){
            addError(errors, "description", "review.error.description.empty.for.review");
        }

        if (form.action().equalsIgnoreCase("assignReviewer") && form.reviewerId() == null) {
            addError(errors, "reviewerId", "review.error.reviewer.empty.for.assign");
        }
    }

    private void addError(Errors errors, String field, String messageKey) {
        String localizedMessage = messageSource.getMessage(
                messageKey,
                null,
                "Validation error",
                LocaleContextHolder.getLocale()
        );
        errors.rejectValue(field, messageKey, localizedMessage);
    }
}
