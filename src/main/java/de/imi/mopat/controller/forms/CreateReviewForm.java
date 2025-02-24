package de.imi.mopat.controller.forms;

import de.imi.mopat.validator.ConditionalValidation;

@ConditionalValidation
public record CreateReviewForm(
        Long questionnaireId,
        Long reviewerId,
        String description
) {

    public CreateReviewForm {
    }
}