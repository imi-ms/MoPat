package de.imi.mopat.dao;

import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.Response;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface ResponseDao extends MoPatDao<Response> {

    /**
     * Searches for the {@link Response} object by its {@link Answer} and {@link Encounter}.
     * Provides the unique {@link Response} object with given {@link Answer} and {@link Encounter}.
     *
     * @param answerId    Id of the {@link Answer} object of the searched BundleQuestionnaire
     *                    object.
     * @param encounterId Id of the {@link Encounter} object of the searched BundleQuestionnaire
     *                    object.
     * @return The unique {@link Response} object with given {@link Answer} and {@link Encounter}.
     */
    Response getResponseByAnswerInEncounter(long answerId, long encounterId);
}
