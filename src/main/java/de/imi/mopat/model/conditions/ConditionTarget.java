package de.imi.mopat.model.conditions;

/**
 * A marker/tagging interface to provide the information that a certain model interface/class can be
 * a {@link Condition Condition's} target. A ConditionTarget's presentation to the user/patient is
 * affected by the {@link Condition Condition's} action type, if the {@link Condition} is triggered
 * by it's {@link ConditionTrigger}. The most common actions are
 * {@link ConditionActionType#DISABLE disabling} and {@link ConditionActionType#ENABLE enabling} the
 * ConditionTarget and thus (not) showing it to the user.
 *
 * @since v1.2
 */
public interface ConditionTarget {

    /**
     * When triggering a {@link Condition}, MoPat needs to know for which target the specified
     * {@link ConditionActionType} shall be performed. The target is determined by its ID. Thus,
     * every ConditionTarget needs to provide its ID.
     *
     * @return the ID of the implementing target. Is never <code>null</code>.
     */
    public Long getId();
}