package de.imi.mopat.model.conditions;

import java.util.Set;
import de.imi.mopat.model.SelectAnswer;

/**
 * A marker/tagging interface to provide the information that a certain model interface/class can be
 * a {@link Condition Condition's} trigger. Whenever a ConditionTrigger is present, shown or
 * selected during a survey, the attached {@link Condition Conditions} shall be evaluated and - if
 * applicable - performed. The most common ConditionTrigger will be
 * {@link SelectAnswer SelectAnswers}, who trigger their respective {@link Condition} whenever they
 * are chosen by the user/patient.
 */
public interface ConditionTrigger {

    /**
     * Returns the id of the current object.
     *
     * @return The id as Long value.
     */
    public Long getId();

    /**
     * Removes the {@link ConditionTrigger} of the given {@link Condition} if it was set
     * before.Takes care that the given {@link Condition} refers to this trigger as well.
     *
     * @param condition must not be <code>null</code>.
     */
    public void addCondition(Condition condition);

    /**
     * Removes the repective {@link ConditionTrigger ConditionTriggers} of the given
     * {@link Condition Conditions} they it was set before.Takes care that the given
     * {@link Condition Conditions} refer to this trigger as well. Won't do anything with an empty
     * Set.
     *
     * @param conditions must not be <code>null</code>.
     */
    public void addConditions(Set<Condition> conditions);

    /**
     * @param condition must not be <code>null</code>.
     * @return <code>true</code> if this {@link ConditionTrigger} contains the
     * given {@link Condition}, <code>false</code> otherwise.
     */
    public boolean contains(Condition condition);

    /**
     * @return is never <code>null</code>, might be empty. Is unmodifiable (see
     * {@link java.util.Collections#unmodifiableSet(Set)}.
     */
    public Set<Condition> getConditions();

    /**
     * Only removes the given {@link Condition} if it refers to this {@link ConditionTrigger} and
     * vice versa.Takes care that the given {@link Condition} does not refer to this
     * {@link ConditionTrigger} anymore, as well.
     *
     * @param condition must not be <code>null</code>.
     */
    public void removeCondition(Condition condition);

    /**
     * For each of the given {@link Condition Conditions}: Only removes the given {@link Condition}
     * if it refers to this {@link ConditionTrigger} and vice versa.Takes care that the given
     * {@link Condition Conditions} do not refer to this {@link ConditionTrigger} anymore, as well.
     * Won't do anything with an empty Set.
     *
     * @param conditions must not be <code>null</code>.
     */
    public void removeConditions(Set<Condition> conditions);
}