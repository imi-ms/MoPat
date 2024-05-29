package de.imi.mopat.model;

import de.imi.mopat.model.conditions.ConditionTrigger;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * A <i>numberInput</i> answer represents a number as an answer from a question.
 */
@Entity
@DiscriminatorValue("NumberInputAnswer")
public class NumberInputAnswer extends Answer implements ConditionTrigger, Serializable {

    @Column(name = "min_value")
    private Double minValue;    //Lowest point for slider questions
    @Column(name = "max_value")
    private Double maxValue;    // Highest point for slider questions
    @Column(name = "stepsize")
    private Double stepsize; // Used for slider questions

    protected NumberInputAnswer() {
        // default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation
        // (here: Hibernate) and the JUnit tests
    }

    /**
     * The model NumberInputAnswer gives the option to set boundaries for the selectable number.
     *
     * @param question  References to the actual {@link Question} connected to this answer.
     * @param isEnabled Indicates whether this answer is enabled or not.
     * @param min       states the lowest point of the range
     * @param max       states the highest point of the range
     * @param stepsize  states the difference between two marks
     */
    public NumberInputAnswer(final Question question, final Boolean isEnabled, final Double min,
        final Double max, final Double stepsize) {
        super(question, isEnabled);
        setMinMax(min, max);
        setStepsize(stepsize);
    }

    @Override
    public NumberInputAnswer cloneWithoutReferences() {
        NumberInputAnswer numberInputAnswer = new NumberInputAnswer();
        numberInputAnswer.setIsEnabled(this.getIsEnabled());
        numberInputAnswer.setMinMax(getMinValue(), getMaxValue());
        numberInputAnswer.setStepsize(getStepsize());
        return numberInputAnswer;
    }

    /**
     * @return The lowest possible value for this number input question. Is never <code>null</code>.
     */
    public Double getMinValue() {
        return minValue;
    }

    /**
     * @param min The new lowest possible value for a number input question. Must not be
     *            <code>null</code>. Has to be lower than the maximum.
     */
    public void setMinValue(final Double min) {
        if (min != null && maxValue != null) {
            assert
                min < maxValue :
                "The given min value was not lower than the " + "existing max value";
        }

        this.minValue = min;
    }

    /**
     * @return The highest possible value for this number input question. Is never
     * <code>null</code>.
     */
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * @param max The new highest possible value for a number input question. Must not be
     *            <code>null</code>. Has to be greater than the minimum.
     */
    public void setMaxValue(final Double max) {
        if (max != null && minValue != null) {
            assert
                max > minValue :
                "The given max value was not greater than " + "the existing min value";
        }

        this.maxValue = max;
    }

    /**
     * @param min Must not be <code>null</code>. Has to be lower than
     *            <code>max</code>.
     * @param max Must not be <code>null</code>. Has to be larger than
     *            <code>min</code>.
     */
    public void setMinMax(final Double min, final Double max) {
        if (min != null && max != null) {
            assert min < max : "The given min value was not < max";
        }
        this.minValue = min;
        this.maxValue = max;
    }

    /**
     * Returns the step size of the number input.
     *
     * @return Returns the step size of the number input. Is never
     * <code>0.0</code>. Is never negative (&lt; 0). Is never <code>null</code>.
     */
    public Double getStepsize() {
        return stepsize;
    }

    /**
     * Sets a new step size for the number input.<br>
     *
     * @param stepsize The new step size of the number input. Must not be
     *                 <code>null</code>. Must not be <code>&lt;= 0</code>.
     */
    public void setStepsize(final Double stepsize) {
        if (stepsize != null) {
            assert stepsize > 0.0 : "The given step size is <= 0.0";
        }
        this.stepsize = stepsize;
    }
}
