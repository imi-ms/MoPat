package de.imi.mopat.model.conditions;

import de.imi.mopat.model.Bundle;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

/**
 * A {@link Condition} that has a {@link SliderAnswerThresholdCondition} as
 * {@link ConditionTrigger}. If the
 * {@link SliderAnswerThresholdCondition SliderAnswerThresholdCondition's} trigger is manipulated by
 * the user (e.g. the user/patient moves the slider button on the slider), the condition is
 * evaluated and - if applicable - the {@link ConditionActionType action} performed against the
 * {@link ConditionTarget}. A {@link SliderAnswerThresholdCondition} is evaluated by comparing the
 * actual value of the {@link ConditionTrigger} (the slider value) to the
 * {@link SliderAnswerThresholdCondition SliderAnswerThresholdCondition's} threshold (see
 * {@link SliderAnswerThresholdCondition#getThreshold()}) with the given comparator (see
 * {@link SliderAnswerThresholdCondition#getThresholdComparisonType()}). Comparison is done in the
 * form:<br> $ACTUAL_VALUE $THRESHOLD_COMPARISON_TYPE $THRESHOLD<br> E.g. if the actual value is 42,
 * the threshold comparison type is {@link ThresholdComparisonType#BIGGER_THAN} and the threshold is
 * 23, this condition will be evaluated as true (and the action performed against the
 * {@link ConditionTarget}).
 */
@Entity
@DiscriminatorValue("SliderAnswerThresholdCondition")
public class SliderAnswerThresholdCondition extends Condition implements Serializable {

    @NotNull(message = "{sliderAnswerThresholdCondition" + ".thresholdComparisonType.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(name = "threshold_comparison_type")
    private ThresholdComparisonType thresholdComparisonType;

    @NotNull(message = "{sliderAnswerThresholdCondition.threshold.notNull}")
    @Column(name = "threshold")
    private Double threshold;

    public SliderAnswerThresholdCondition() {

    }

    public SliderAnswerThresholdCondition(final ConditionTrigger trigger,
        final ConditionTarget target, final ConditionActionType action, final Bundle bundle,
        final ThresholdComparisonType thresholdComparisonType, final Double threshold) {
        super(trigger, target, action, bundle);
        setThresholdComparisonType(thresholdComparisonType);
        setThreshold(threshold);
    }

    /**
     * The condition's threshold value that is compared to the actual value of it's trigger (e.g.
     * slider value) with the given comparator (see
     * {@link SliderAnswerThresholdCondition#getThresholdComparisonType()}).
     *
     * @return is never <code>null</code>. Is always in between the
     * {@link ConditionTrigger ConditionTrigger's} min and max values, including these values. Does
     * not have to be an exactly possible value of the {@link ConditionTrigger}, i.e. a valid step
     * within the range of min and max, in steps based on the stepsize.
     */
    public Double getThreshold() {
        return threshold;
    }

    /**
     * @param threshold must not be <code>null</code>. Must be between the
     *                  {@link ConditionTrigger ConditionTrigger's} min and max values, including
     *                  these values. Does not have to be an exactly possible value of the
     *                  {@link ConditionTrigger}, i.e. a valid step within the range of min and max,
     *                  in steps based on the stepsize.
     */
    public void setThreshold(final Double threshold) {
        assert threshold != null : "The threshold given was null";
        this.threshold = threshold;
    }

    /**
     * The comparator utilized to compare the current {@link ConditionTrigger ConditionTrigger's}
     * value against the given threshold. Comparison is done in the form:<br> $ACTUAL_VALUE
     * $THRESHOLD_COMPARISON_TYPE $THRESHOLD<br> E.g. if the actual value is 42, the threshold
     * comparison type is {@link ThresholdComparisonType#BIGGER_THAN} and the threshold is 23, this
     * condition will be evaluated as true (and the action performed against the
     * {@link ConditionTarget}).
     *
     * @return is never <code>null</code>.
     */
    public ThresholdComparisonType getThresholdComparisonType() {
        return thresholdComparisonType;
    }

    /**
     * @param thresholdComparisonType must not be <code>null</code>.
     */
    public void setThresholdComparisonType(final ThresholdComparisonType thresholdComparisonType) {
        assert thresholdComparisonType != null : "The ThresholdComparisonType given was null";
        this.thresholdComparisonType = thresholdComparisonType;
    }
}
