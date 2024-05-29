package de.imi.mopat.model;

import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import de.imi.mopat.model.conditions.ConditionTrigger;

import java.util.*;

/**
 * A <i>slider</i> answer represents the answer of a slider question. A slider question is a
 * question where the user is given the choice to mark a spot on a horizontal or vertical line.
 */
@Entity
@DiscriminatorValue("SliderAnswer")
public class SliderAnswer extends Answer implements Serializable, ConditionTrigger {

    @NotNull(message = "{sliderAnswer.minValue.notNull}")
    @Column(name = "min_value")
    //@Column(nullable = "false") is not possible due to our inheritance
    // strategy (see Answer). Thus, the annotation @NotNull for jakarta
    // .validation and JavaDoc together with asserts and tests will be used
    private Double minValue;    //Lowest point for slider questions
    @NotNull(message = "{sliderAnswer.maxValue.notNull}")
    @Column(name = "max_value")
    //@Column(nullable = "false") is not possible due to our inheritance
    // strategy (see Answer). Thus, the annotation @NotNull for jakarta
    // .validation and JavaDoc together with asserts and tests will be used
    private Double maxValue;    // Highest point for slider questions
    @NotNull(message = "{sliderAnswer.stepsize.notNull}")
    @Column(name = "stepsize")
    //@Column(nullable = "false") is not possible due to our inheritance
    // strategy (see Answer). Thus, the annotation @NotNull for jakarta
    // .validation and JavaDoc together with asserts and tests will be used
    private Double stepsize; // Used for slider questions

    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "minimum_text")
    @CollectionTable(name = "answer_minimum_text", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedMinimumText;

    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "maximum_text")
    @CollectionTable(name = "answer_maximum_text", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedMaximumText;
    //text to show besides/above the maximum value

    @Column(name = "show_value_on_button")
    private Boolean showValueOnButton;

    @Column(name = "show_icons")
    private Boolean showIcons;

    @NotNull(message = "{sliderAnswer.vertical.notNull}")
    @Column(name = "vertical")
    //@Column(nullable = "false") is not possible due to our inheritance
    // strategy (see Answer). Thus, the annotation @NotNull for jakarta
    // .validation and JavaDoc together with asserts and tests will be used
    private Boolean vertical;

    @OneToMany(mappedBy = "answer", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<SliderIcon> icons = new HashSet<>();

    protected SliderAnswer() {
        // default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation (here: Hibernate) and
        // the JUnit tests.
    }

    /**
     * The answer object inherit different options of answers.Thus the model SliderAnswer represents
     * the option to choose a value within a given range.
     *
     * @param question  References to the actual {@link Question} connected to this answer.
     * @param isEnabled Indicates whether this answer is enabled or not.
     * @param min       States the lowest point of the range
     * @param max       States the highest point of the range
     * @param stepsize  States the difference between two marks
     * @param vertical  States if the slider should be displayed vertically.
     */
    public SliderAnswer(final Question question, final Boolean isEnabled, final Double min,
        final Double max, final Double stepsize, final Boolean vertical) {
        super(question, isEnabled);
        setMinMax(min, max);
        setStepsize(stepsize);
        setVertical(vertical);
    }

    @Override
    public SliderAnswer cloneWithoutReferences() {
        SliderAnswer sliderAnswer = new SliderAnswer();
        sliderAnswer.setIsEnabled(this.getIsEnabled());
        sliderAnswer.setMinMax(this.getMinValue(), this.getMaxValue());
        sliderAnswer.setStepsize(this.getStepsize());
        if (getLocalizedMinimumText() == null) {
            sliderAnswer.setLocalizedMinimumText(null);
        } else {
            sliderAnswer.setLocalizedMinimumText(new HashMap<>(this.getLocalizedMinimumText()));
        }
        if (getLocalizedMaximumText() == null) {
            sliderAnswer.setLocalizedMaximumText(null);
        } else {
            sliderAnswer.setLocalizedMaximumText(new HashMap<>(this.getLocalizedMaximumText()));
        }

        sliderAnswer.setShowValueOnButton(this.getShowValueOnButton());
        sliderAnswer.setVertical(this.getVertical());
        sliderAnswer.setShowIcons(this.getShowIcons());
        return sliderAnswer;
    }

    /**
     * @return The lowest point for this slider question. Is never
     * <code>null</code>.
     */
    public Double getMinValue() {
        return minValue;
    }

    /**
     * @param min The new lowest point for a slider question. Must not be
     *            <code>null</code>. Has to be lower than the maximum.
     */
    public void setMinValue(Double min) {
        assert min != null : "The given min value was null";
        assert
            min < maxValue : "The given min value was not lower than the " + "existing max value";
        this.minValue = min;
    }

    /**
     * @return The highest point for this slider question. Is never
     * <code>null</code>.
     */
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * @param max The new highest point for a slider question. Must not be
     *            <code>null</code>. Has to be greater than the minimum.
     */
    public void setMaxValue(Double max) {
        assert max != null : "The given max value was null";
        assert
            max > minValue : "The given max value was not greater than the " + "existing min value";
        this.maxValue = max;
    }

    /**
     * @param min Must not be <code>null</code>. Has to be lower than
     *            <code>max</code>.
     * @param max Must not be <code>null</code>. Has to be larger than
     *            <code>min</code>.
     */
    public void setMinMax(Double min, Double max) {
        assert min != null : "The given min value was null";
        assert max != null : "The given max value was null";
        assert min < max : "The given min value was not < max";
        this.minValue = min;
        this.maxValue = max;
    }

    /**
     * Returns the step size of the slider.
     *
     * @return Returns the step size of the slider. Is never <code>0.0</code>. Is never negative
     * (&lt; 0). Is never <code>null</code>.
     */
    public Double getStepsize() {
        return stepsize;
    }

    /**
     * Sets a new step size for the slider.<br>
     *
     * @param stepsize The new step size of the slider. Must not be
     *                 <code>null</code>. Must not be <code>&lt;= 0</code>.
     */
    public void setStepsize(Double stepsize) {
        assert stepsize != null : "The given stepsize was null";
        assert stepsize > 0.0 : "The given step size is <= 0.0";
        this.stepsize = stepsize;
    }

    /**
     * @return might be <code>null</code>, might be empty.
     */
    public Map<String, String> getLocalizedMinimumText() {
        return localizedMinimumText;
    }

    /**
     * @param localizedMinimumText can be <code>null</code>, can be empty.
     */
    public void setLocalizedMinimumText(final Map<String, String> localizedMinimumText) {
        this.localizedMinimumText = localizedMinimumText;
    }

    /**
     * @return might be <code>null</code>, might be empty (<code>""</code> after trimming). Will not
     * be trimmed
     */
    public Map<String, String> getLocalizedMaximumText() {
        return localizedMaximumText;
    }

    /**
     * @param localizedMaximumText can be <code>null</code>, can be empty.
     */
    public void setLocalizedMaximumText(final Map<String, String> localizedMaximumText) {
        this.localizedMaximumText = localizedMaximumText;
    }

    /**
     * @return might be <code>null</code>
     */
    public Boolean getShowValueOnButton() {
        return showValueOnButton;
    }

    /**
     * @param showValueOnButton can be <code>null</code>
     */
    public void setShowValueOnButton(final Boolean showValueOnButton) {
        this.showValueOnButton = showValueOnButton;
    }

    @Override
    public String toString() {
        return "{min: " + getMinValue() + ", max: " + getMaxValue() + ", stepsize: " + getStepsize()
            + "}";
    }

    public Boolean getVertical() {
        return vertical;
    }

    public void setVertical(final Boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * @return might be <code>null</code>
     */
    public Boolean getShowIcons() {
        return showIcons;
    }

    /**
     * @param showIcons can be <code>null</code>
     */
    public void setShowIcons(final Boolean showIcons) {
        this.showIcons = showIcons;
    }

    /**
     * Returns the icons that are assigned to the slider
     *
     * @return Set with SliderIcon Objects that are assigned to the slider
     */
    public Set<SliderIcon> getIcons() {
        return icons;
    }

    /**
     * Sets the slider icons
     *
     * @param icons set with SliderIcon Objects
     */
    public void setIcons(final Set<SliderIcon> icons) {
        this.icons = icons;
    }

    /**
     * Adds a new SliderIcon object to the icons set
     *
     * @param icon SliderIcon Object
     */
    public void addIcon(final SliderIcon icon) {
        this.icons.add(icon);
    }
}
