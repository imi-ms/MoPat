package de.imi.mopat.dao;

import de.imi.mopat.model.Questionnaire;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface QuestionnaireDao extends MoPatDao<Questionnaire> {

    /**
     * Check if the given name is free for usage.
     *
     * @param name The name which will be checked
     * @param id   The id of the questionniare which should be saved
     * @return true if the name is free for usage and false otherwise
     */
    boolean isQuestionnaireNameUnused(String name, Long id);
    boolean isQuestionnaireNameUnique(String name, Long id);
}