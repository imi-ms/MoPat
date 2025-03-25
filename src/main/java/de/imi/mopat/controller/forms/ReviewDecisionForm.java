package de.imi.mopat.controller.forms;

import de.imi.mopat.validator.ConditionalValidation;
import jakarta.validation.constraints.NotBlank;

@ConditionalValidation
public record ReviewDecisionForm(
        Long reviewId,
        @NotBlank(message = "Action is required") String action,
        String description,
        String personalMessage,
        Boolean isMainVersion,
        Long reviewerId,
        String language
){
    public ReviewDecisionForm {
        if (language == null || language.isBlank()) {
            language = "de_DE";
        }
    }
}