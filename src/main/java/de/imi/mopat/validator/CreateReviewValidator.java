package de.imi.mopat.validator;

import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Questionnaire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CreateReviewValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateReviewForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof CreateReviewForm form)) {
            return;
        }

        if (form.reviewerId() == null) {
            addError(errors, "reviewerId", "review.error.reviewer.empty");
        }

        Questionnaire questionnaire = questionnaireDao.getElementById(form.questionnaireId());
        if (questionnaire == null) {
            addError(errors, "questionnaireId", "review.error.questionnaire.not.found");
            return;
        }

        if (questionnaire.getVersion() > 1 && (form.description() == null || form.description().isBlank())) {
            addError(errors, "description", "review.error.description.empty.for.duplicate");
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