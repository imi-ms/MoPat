package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Questionnaire;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public class BundleQuestionnaireDaoImpl extends MoPatDaoImpl<BundleQuestionnaire> implements
    BundleQuestionnaireDao {

    @Override
    public BundleQuestionnaire getBundleQuestionnaire(final Bundle bundle,
        final Questionnaire questionnaire) {
        try {
            TypedQuery<BundleQuestionnaire> query = moPatEntityManager.createQuery(
                "SELECT bs FROM BundleQuestionnaire bs WHERE bs.bundle.id" + " = " + bundle.getId()
                    + " " + "and bs" + ".questionnaire.id = " + questionnaire.getId(),
                getEntityClass());
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<BundleQuestionnaire> findByQuestionnaire(Long questionnaireId) {
        try {
            TypedQuery<BundleQuestionnaire> query = moPatEntityManager.createQuery(
                    "SELECT bq FROM BundleQuestionnaire bq WHERE bq.questionnaire.id = :questionnaireId",
                    BundleQuestionnaire.class);
            query.setParameter("questionnaireId", questionnaireId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
