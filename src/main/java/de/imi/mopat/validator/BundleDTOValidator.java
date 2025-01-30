package de.imi.mopat.validator;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.helper.controller.HtmlUtils;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link Bundle Bundle} objects.
 */
@Component
public class BundleDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private BundleDao bundleDao;

    @Override
    public boolean supports(Class<?> type) {
        return BundleDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        BundleDTO bundleDTO = (BundleDTO) o;

        if (!bundleDTO.getName().isEmpty() && bundleDTO.getName().trim().isEmpty()) {
            bundleDTO.setName("");
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("bundle.error.nameIsEmpty", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (!bundleDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("bundle.error.nameContainsSpecialCharacters",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }

        if (bundleDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")
            && !bundleDao.isBundleNameUnused(bundleDTO.getName(), bundleDTO.getId())) {
            errors.rejectValue("name", "errormessage",
                messageSource.getMessage("bundle.error.nameInUse", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        String bundleDescription = HtmlUtils.removeHtmlTags(bundleDTO.getDescription());

        if (bundleDescription != null && bundleDescription.isEmpty()) {
            errors.rejectValue("description", "errormessage",
                messageSource.getMessage("bundle.description.notNull", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        // Check if at least the first questionnaire is enabled in this bundle
        if (bundleDTO.getBundleQuestionnaireDTOs() != null
            && !bundleDTO.getBundleQuestionnaireDTOs().isEmpty()) {
            BundleQuestionnaireDTO firstBundleQuestionnaireDTO = null;
            if (bundleDTO.getBundleQuestionnaireDTOs().get(0).getQuestionnaireDTO() != null) {
                firstBundleQuestionnaireDTO = bundleDTO.getBundleQuestionnaireDTOs().get(0);
            }
            if (firstBundleQuestionnaireDTO != null
                && firstBundleQuestionnaireDTO.getQuestionnaireDTO().getId() != null && (
                firstBundleQuestionnaireDTO.getIsEnabled() == null
                    || !firstBundleQuestionnaireDTO.getIsEnabled())) {
                errors.rejectValue("bundleQuestionnaireDTOs", "errormessage",
                    messageSource.getMessage("bundle.error.firstQuestionnaireNotActive",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }

        // Flag to indicate whether a welcome text was found or not
        boolean hasWelcomeText = false;
        // Loop through all localized welcome texts
        for (Map.Entry<String, String> entry : bundleDTO.getLocalizedWelcomeText().entrySet()) {
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
            for (Map.Entry<String, String> entry : bundleDTO.getLocalizedWelcomeText().entrySet()) {
                // If the current welcome text is empty
                if (entry.getValue().isEmpty()) {
                    // Attach the error for an empty welcome text
                    errors.rejectValue("localizedWelcomeText[" + entry.getKey() + "]",
                        MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("bundle.validator" + ".welcomeText" + ".notNull",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
            }
        }

        // Flag to indicate whether a final text was found or not
        boolean hasFinalText = false;
        // Loop through all localized final texts
        for (Map.Entry<String, String> entry : bundleDTO.getLocalizedFinalText().entrySet()) {
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
            for (Map.Entry<String, String> entry : bundleDTO.getLocalizedFinalText().entrySet()) {
                // If the current final text is empty
                if (entry.getValue().isEmpty()) {
                    // Attach the error for an empty final text
                    errors.rejectValue("localizedFinalText[" + entry.getKey() + "]",
                        MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("bundle.validator" + ".finalText" + ".notNull",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
            }
        }
    }
}
