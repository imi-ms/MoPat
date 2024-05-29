package de.imi.mopat.model.conditions;

/**
 * Defines, what MoPat should do with a {@link ConditionTarget} if the respective {@link Condition}
 * is triggered by the {@link ConditionTrigger}. E.g. if the {@link Condition Condition's} action
 * type is set to {@link ConditionActionType#DISABLE}, the respective {@link ConditionTarget} shall
 * not be presented to the user/patient.
 *
 * @since v1.2
 */
public enum ConditionActionType {

    DISABLE, ENABLE;
}