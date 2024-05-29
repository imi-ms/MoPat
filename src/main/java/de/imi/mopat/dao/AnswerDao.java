package de.imi.mopat.dao;

import de.imi.mopat.model.Answer;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface AnswerDao extends MoPatDao<Answer> {

    /**
     * @param conditionId must not be <code>null</code>, must be positive.
     * @return a <code>null</code> value if no {@link Answer} could be found.
     */
    Answer getAnswerWhichIsTheOriginForCondition(Long conditionId);
}