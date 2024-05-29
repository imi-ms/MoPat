package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
public class QuestionnaireDaoImpl extends MoPatDaoImpl<Questionnaire> implements QuestionnaireDao {

    @Autowired
    private QuestionDao questionDao;

    @Override
    @Transactional("MoPat")
    public void remove(final Questionnaire element) {
        // because question is on the owner side, we first have to remove the
        // questions
        for (Question question : element.getQuestions()) {
            questionDao.remove(question);
        }
        element.removeAllQuestions();
        super.remove(element);
    }

    @Override
    public boolean isQuestionnaireNameUnused(final String name, final Long id) {
        try {
            Query query = moPatEntityManager.createQuery(
                "SELECT q FROM " + "Questionnaire q WHERE q.name='" + name + "'");
            Questionnaire questionnaire = (Questionnaire) query.getSingleResult();
            // If there is a result, check if it is the same questionnaire
            // that should be edit
            return id != null && questionnaire.getId().longValue() == id.longValue();
        } catch (NoResultException e) {
            return true;
        }
    }
}
