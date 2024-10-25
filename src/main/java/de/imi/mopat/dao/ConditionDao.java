package de.imi.mopat.dao;

import java.util.Set;
import org.springframework.stereotype.Component;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTarget;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.ThresholdComparisonType;
import java.util.List;

/**
 *
 */
@Component
public interface ConditionDao extends MoPatDao<Condition> {

    /**
     * Returns true if the {@link ConditionTarget} has {@link Condition conditions}, false
     * otherwise.
     *
     * @param target The {@link Object}, which should be checked for {@link Condition conditions},
     *               can be a {@link Bundle}, a {@link Questionnaire}, a {@link Question} or an
     *               {@link Answer}.
     * @return Returns true if the {@link Object} has {@link Condition conditions}, false otherwise.
     */
    boolean isConditionTarget(Object target);

    /**
     * Return a Set of {@link Condition} IDs where the target is a {@link Questionnaire} and
     * the target id matches with the passed list of questionnaireIds.
     * Optimized
     * @param ids questionnaire ids to bulk check with
     * @return {@link List} of target ids
     */
    Set<Long> findConditionTargetIds(List<Long> ids, String type);

    /**
     * Returns the list of associated {@link Condition conditions} to the given
     * {@link ConditionTarget}.
     *
     * @param target The {@link Object}, can be a {@link Bundle}, a {@link Questionnaire}, a
     *               {@link Question} or an {@link Answer}, for which the conditions should be
     *               returned.
     * @return The list of associated {@link Condition conditions} to the given
     * {@link ConditionTarget}.
     */
    List<Condition> getConditionsByTarget(Object target);

    /**
     * Returns the list of all {@link Condition conditions} with the same {@link ConditionTrigger}
     * and possibly the same thresholds.
     *
     * @param condition A {@link Condition} whose trigger should be used.
     * @return The list of all {@link Condition conditions} with the same {@link ConditionTrigger}.
     */
    List<Condition> getConditionsByTriggerCondition(Condition condition);

    /**
     * Returns the list of all {@link Condition conditions} with the same
     * <p>
     * {@link Answer} as {@link ConditionTrigger} and possibly the same thresholds.
     *
     * @param triggerAnswer           A {@link Answer} that may trigger some
     *                                {@link Condition Conditions}.
     * @param thresholdValue          A {@link Double} value, may be null.
     * @param thresholdComparisonType The {@link ThresholdComparisonType} of the conditions.
     * @return The list of all {@link Condition conditions} with the given {@link Answer}as trigger
     * and possibly the same threshold value.
     */
    List<Condition> getConditionsByTriggerAnswer(Answer triggerAnswer, Double thresholdValue,
        ThresholdComparisonType thresholdComparisonType);
}
