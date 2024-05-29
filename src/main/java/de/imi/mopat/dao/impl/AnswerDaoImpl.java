package de.imi.mopat.dao.impl;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.model.Answer;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AnswerDaoImpl extends MoPatDaoImpl<Answer> implements AnswerDao {

    @Override
    public Answer getAnswerWhichIsTheOriginForCondition(final Long conditionId) {
        assert conditionId != null : "The conditionId given was null";
        assert conditionId > 0L : "The conditionId given was not positive";

        try {
            // [bt] get the answer that has the condition with the given
            // conditionId in its Set of Conditions
            TypedQuery<Answer> query = moPatEntityManager.createQuery(
                "SELECT a FROM Answer a JOIN a.conditions c WHERE c.id = " + ":conditionId",
                getEntityClass());
            query.setParameter("conditionId", conditionId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println(e);
            return null;
        }
        // FIXME go on implementing here
    }
}
