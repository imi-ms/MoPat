package de.imi.mopat.validator;

import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.QuestionnaireDTO;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link Questionnaire Questionnaire} objects.
 */
@Component
public class QuestionnaireDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Override
    public boolean supports(final Class<?> type) {
        return QuestionnaireDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        QuestionnaireDTO questionnaireDTO = (QuestionnaireDTO) o;

        if (!questionnaireDTO.getName().isEmpty() && questionnaireDTO.getName().trim().isEmpty()) {
            questionnaireDTO.setName("");
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("questionnaire.error.nameIsEmpty", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (!questionnaireDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("questionnaire.error.nameContainsSpecialCharacters",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }

        if (questionnaireDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")
            && !questionnaireDao.isQuestionnaireNameUnique(questionnaireDTO.getName(),
            questionnaireDTO.getId())) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("questionnaire.error.nameInUse", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        // [sw] Check if any added language contains an empty questionText
        for (Map.Entry<String, String> entry : questionnaireDTO.getLocalizedDisplayName()
            .entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty() || Pattern.matches(
                "<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>", entry.getValue())) {
                errors.rejectValue("localizedDisplayName[" + entry.getKey() + "]",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("questionnaire.displayName" + ".notNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }

        // Flag to indicate whether a welcome text was found or not
        boolean hasWelcomeText = false;
        // Loop through all localized welcome texts
        for (Map.Entry<String, String> entry : questionnaireDTO.getLocalizedWelcomeText()
            .entrySet()) {
            // If the current welcome text is not empty
            if (!entry.getValue().isEmpty()) {
                // Set the appropriate flag to true
                hasWelcomeText = true;
                break;
            }
        }
        // If one not empty welcome text was found
        if (hasWelcomeText) {
            // Check all welcome texts
            for (Map.Entry<String, String> entry : questionnaireDTO.getLocalizedWelcomeText()
                .entrySet()) {
                // If the current welcome text is empty
                if (entry.getValue().isEmpty()) {
                    // Attach the error for an empty welcome text
                    errors.rejectValue("localizedWelcomeText[" + entry.getKey() + "]",
                        MoPatValidator.ERRORCODE_NOT_NULL, messageSource.getMessage(
                            "questionnaire.validator" + ".welcomeText" + ".notNull", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }
            }
        }

        // Flag to indicate whether a final text was found or not
        boolean hasFinalText = false;
        // Loop through all localized final texts
        for (Map.Entry<String, String> entry : questionnaireDTO.getLocalizedFinalText()
            .entrySet()) {
            // If the current final text is not empty
            if (!entry.getValue().isEmpty()) {
                // Set the appropriate flag to true
                hasFinalText = true;
                break;
            }
        }
        // If one not empty final text was found
        if (hasFinalText) {
            // Check all final texts
            for (Map.Entry<String, String> entry : questionnaireDTO.getLocalizedFinalText()
                .entrySet()) {
                // If the current final text is empty
                if (entry.getValue().isEmpty()) {
                    // Attach the error for an empty final text
                    errors.rejectValue("localizedFinalText[" + entry.getKey() + "]",
                        MoPatValidator.ERRORCODE_NOT_NULL, messageSource.getMessage(
                            "questionnaire.validator" + ".finalText" + ".notNull", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }
            }
        }
    }
}