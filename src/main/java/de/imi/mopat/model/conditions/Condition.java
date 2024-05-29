package de.imi.mopat.model.conditions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.dto.ConditionDTO;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.DiscriminatorClass;
import org.eclipse.persistence.annotations.VariableOneToOne;

/**
 * A condition defines that a certain {@link ConditionActionType action} (e.g. not to show) shall be
 * performed with/against a certain {@link ConditionTarget element} (e.g. a targetAnswerQuestion) if
 * a {@link ConditionTrigger trigger} (e.g. a certain answer) has been activated/chosen and the
 * {@link Condition Condition's} expression/definition is true/applicable. Conditions have
 * properties that are mainly based on their {@link ConditionTrigger ConditionTrigger's} type, which
 * leads to sub-classes of this.
 */
@Entity
// [bt] different table name for conditions, since 'condition' is a reserved
// sql/database keyword
@Table(name = "mopat_condition")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "condition_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Condition implements Serializable {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @Id
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @JsonIgnore
    @VariableOneToOne( // [bt] A ConditionTrigger is just an interface, the
        // implementing classes are different. Here we're
        // specifying, that the type of class is stored in the
        // column 'trigger_class', so that the JPA vendor and
        // the
        // DB don't have to store a BLOB but just the type and
        // id and can make a POJO out of this info
        discriminatorClasses = {
            // [bt] if there's a new class at that a Condition can
            // point (i.e.
            // the target) to, enhance this configuration by another
            // @DiscriminatorClass
            @DiscriminatorClass(discriminator = "Question", value = Question.class),
            @DiscriminatorClass(discriminator = "SelectAnswer", value = SelectAnswer.class),
            @DiscriminatorClass(discriminator = "SliderAnswer", value = SliderAnswer.class)}, discriminatorColumn = @DiscriminatorColumn(name = "trigger_class"))
    @JoinColumn(name = "trigger_id", referencedColumnName = "id")
    private ConditionTrigger trigger;

    @VariableOneToOne( // [bt] A ConditionTarget is just an interface, the
        // implementing classes are different. Here we're
        // specifying, that the type of class is stored in the
        // column 'target_class', so that the JPA vendor and the
        // DB don't have to store a BLOB but just the type and
        // id and can make a POJO out of this info
        discriminatorClasses = {
            // [bt] if there's a new class at that a Condition can
            // point (i.e.
            // the target) to, enhance this configuration by another
            // @DiscriminatorClass
            @DiscriminatorClass(discriminator = "Question", value = Question.class),
            @DiscriminatorClass(discriminator = "Questionnaire", value = Questionnaire.class),
            @DiscriminatorClass(discriminator = "SelectAnswer", value = SelectAnswer.class)}, discriminatorColumn = @DiscriminatorColumn(name = "target_class"))
    @JoinColumn(name = "target_id", referencedColumnName = "id")
    private ConditionTarget target;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private ConditionActionType action = ConditionActionType.DISABLE;

    @OneToOne
    @JoinColumn(name = "bundle_id", referencedColumnName = "id")
    private Bundle bundle;

    @OneToOne
    @JoinColumn(name = "target_answer_question_id", referencedColumnName = "id")
    private Question targetAnswerQuestion;

    protected Condition() {
        // default constructor (in protected state), should not be accessible to
        // anything else but the JPA implementation and the JUnit tests
    }

    public Condition(final ConditionTrigger trigger, final ConditionTarget target,
        final ConditionActionType action, final Bundle bundle) {
        setTrigger(trigger);
        setTarget(target);
        setAction(action);
        setBundle(bundle);

        if (target instanceof Answer) {
            Answer targetAnswer = (Answer) target;
            this.setTargetAnswerQuestion(targetAnswer.getQuestion());
        }
    }

    /**
     * Takes care that the given {@link ConditionTrigger} refers to this, as well.
     *
     * @param trigger must not be <code>null</code>.
     */
    public void setTrigger(final ConditionTrigger trigger) {
        assert trigger != null : "The trigger was null";
        if (this.trigger != null && !this.trigger.equals(trigger)) {
            removeTrigger();
        }
        this.trigger = trigger;
        if (!trigger.contains(this)) {
            trigger.addCondition(this);
        }
    }

    /**
     * The trigger that has to be activated/chosen by the user/patient to make MoPat evaluate the
     * condition.
     *
     * @return is never <code>null</code>.
     */
    public ConditionTrigger getTrigger() {
        return trigger;
    }

    /**
     * Should not be called by you, but will be called by
     * {@link ConditionTrigger#removeCondition(Condition)} and
     * {@link ConditionTrigger#removeConditions(java.util.Set)}. Takes care that the
     * {@link ConditionTrigger} does not refer to this, as well.
     */
    public void removeTrigger() {
        if (trigger != null) {
            ConditionTrigger triggerTemp = trigger;
            trigger = null;
            if (triggerTemp.contains(this)) {
                triggerTemp.removeCondition(this);
            }
        }
    }

    /**
     * The Condition's target to to perform the {@link ConditionActionType action} against (if the
     * condition is true/applicable).
     *
     * @return is never <code>null</code>.
     */
    public ConditionTarget getTarget() {
        return target;
    }

    /**
     * Since MoPat is working with Java Interfaces, the implementing class of a
     * {@link Condition Condition's} {@link ConditionTarget} is hidden. This is, in general, the way
     * to implement such concepts. However, when (part of) the model is transferred to the client
     * (using JavaScript), the implementing class needs to be known to initialize/refer to the
     * correct client-side model. Thus, a {@link Condition} needs to be able to tell the class of
     * it's current {@link ConditionTarget}.
     *
     * @return is never <code>null</code>.
     */
    public String getTargetClass() {
        return target.getClass().getName();
    }

    /**
     * Takes care that the given {@link ConditionTarget} refers to this, as well.
     *
     * @param target must not be <code>null</code>.
     */
    public void setTarget(final ConditionTarget target) {
        assert target != null : "The target was null";
        this.target = target;
    }

    /**
     * The Condition's action to perfom.
     *
     * @return is never <code>null</code>.
     */
    public ConditionActionType getAction() {
        return action;
    }

    /**
     * Sets the action.
     *
     * @param action The Condition's action to perfom.
     */
    public void setAction(final ConditionActionType action) {
        assert action != null : "The Action was null";
        this.action = action;
    }

    /**
     * @return id The current id of this condition object. Might be
     * <code>null</code> for newly created objects. Is never
     * <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the assigned bundle of this condition object.
     *
     * @return {@link Bundle Bundle} object
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Sets the bundle of this condition object.
     *
     * @param bundle {@link Bundle Bundle} object to set.
     */
    public void setBundle(final Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Returns the question where the target answers of this condition belongs to.
     *
     * @return {@link Question question} object.
     */
    public Question getTargetAnswerQuestion() {
        return targetAnswerQuestion;
    }

    /**
     * Sets the question where the target answers of this condition belongs to.
     *
     * @param targetAnswerQuestion {@link Question question} object to set.
     */
    public void setTargetAnswerQuestion(final Question targetAnswerQuestion) {
        this.targetAnswerQuestion = targetAnswerQuestion;
    }

    public ConditionDTO toConditionDTO() {
        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setId(this.getId());
        conditionDTO.setAction(this.getAction().name());
        conditionDTO.setTargetClass(this.getTargetClass());
        conditionDTO.setTargetId(this.getTarget().getId());
        conditionDTO.setTriggerId(this.getTrigger().getId());
        if (this.getTargetAnswerQuestion() != null) {
            conditionDTO.setTargetAnswerQuestionId(this.getTargetAnswerQuestion().getId());
        }
        if (this.bundle != null) {
            conditionDTO.setBundleId(this.bundle.getId());
        } else {
            conditionDTO.setBundleId(null);
        }

        if (this instanceof SliderAnswerThresholdCondition) {
            // If condition is a SliderAnswerThresholdCondition set the
            // appropriate values
            SliderAnswerThresholdCondition tresholdCondition = (SliderAnswerThresholdCondition) this;
            conditionDTO.setThresholdType(tresholdCondition.getThresholdComparisonType());
            conditionDTO.setThresholdValue(tresholdCondition.getThreshold());
        }
        return conditionDTO;
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Condition)) {
            return false;
        }
        Condition other = (Condition) obj;
        return getUUID().equals(other.getUUID());
    }
}
