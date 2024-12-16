package de.imi.mopat.dao;

import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Questionnaire;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public interface BundleQuestionnaireDao extends MoPatDao<BundleQuestionnaire> {

    /**
     * Searches for the {@link BundleQuestionnaire} object by its {@link Bundle} and
     * {@link Questionnaire}. Provides the unique {@link BundleQuestionnaire} object with given
     * {@link Bundle} and Questionnaire.
     *
     * @param bundle        {@link Bundle} object of the searched BundleQuestionnaire object.
     * @param questionnaire {@link Questionnaire} object of the searched BundleQuestionnaire
     *                      object.
     * @return The unique {@link BundleQuestionnaire} object, which was found by its {@link Bundle}
     * and {@link Questionnaire}.
     */
    BundleQuestionnaire getBundleQuestionnaire(Bundle bundle, Questionnaire questionnaire);

    List<BundleQuestionnaire> findByQuestionnaire(Long questionnaireId);
}