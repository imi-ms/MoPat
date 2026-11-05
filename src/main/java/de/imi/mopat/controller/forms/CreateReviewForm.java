package de.imi.mopat.controller.forms;

import de.imi.mopat.validator.ConditionalValidation;

@ConditionalValidation
public record CreateReviewForm(
        Long questionnaireId,
        Long reviewerId,
        String description,
        String personalMessage,
        String language
) {

    public CreateReviewForm {
        if (language == null || language.isBlank()) {
            language = "de_DE";
        }
    }
}