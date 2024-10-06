package de.imi.mopat.helper.model;

import de.imi.mopat.model.Questionnaire;
import org.springframework.stereotype.Component;

@Component
public class QuestionnaireFactory {
    public Questionnaire createQuestionnaire(String name, String description, Long changedBy, Boolean isPublished) {
        return new Questionnaire(
                name,
                description,
                changedBy,
                isPublished
        );
    }
}
