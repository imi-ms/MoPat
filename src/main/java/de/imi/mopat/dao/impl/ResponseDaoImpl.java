package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ResponseDao;
import de.imi.mopat.model.Response;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ResponseDaoImpl extends MoPatDaoImpl<Response> implements ResponseDao {

    @Override
    public Response getResponseByAnswerInEncounter(final long answerId, final long encounterId) {
        try {
            TypedQuery<Response> query = moPatEntityManager.createQuery(
                "SELECT r FROM Response r WHERE r.answer.id=" + (answerId) + " AND r.encounter.id="
                    + (encounterId), Response.class);
            Response response = query.getSingleResult();
            return response;
        } catch (NoResultException e) {
            return null;
        }
    }
}
