package de.imi.mopat.model.conditions;

import de.imi.mopat.model.Bundle;

import java.io.Serializable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import de.imi.mopat.model.SelectAnswer;

/**
 * A {@link Condition} that has a {@link SelectAnswer} as {@link ConditionTrigger}. If the
 * {@link SelectAnswerCondition SelectAnswerCondition's} trigger is selected (the user/patient
 * selects the multiple-choice-answer), the condition is evaluated and - if applicable - the
 * {@link ConditionActionType action} performed against the {@link ConditionTarget}.
 *
 * @since v1.2
 */
@Entity
@DiscriminatorValue("SelectAnswerCondition")
public class SelectAnswerCondition extends Condition implements Serializable {

    public SelectAnswerCondition() {

    }

    public SelectAnswerCondition(final ConditionTrigger trigger, final ConditionTarget target,
        final ConditionActionType action, final Bundle bundle) {
        super(trigger, target, action, bundle);
    }
}
