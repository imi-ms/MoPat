package de.imi.mopat.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import de.imi.mopat.model.conditions.ConditionTrigger;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;

/**
 * A {@link SliderFreetextAnswer} represents the constraints a {@link Question} of type
 * {@link de.imi.mopat.model.enumeration.QuestionType#NUMBER_CHECKBOX_TEXT} can have. It contains
 * and offers all properties of {@link SliderAnswer}, extended by a freetext export field to know
 * where to export the free text a patient might enter to.
 */
@Entity
@DiscriminatorValue("SliderFreetextAnswer")
public class SliderFreetextAnswer extends SliderAnswer implements Serializable, ConditionTrigger {

    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "freetext_label")
    @CollectionTable(name = "answer_freetext", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedFreetextLabel = new HashMap<>();

    protected SliderFreetextAnswer() {
        // default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation (here: Hibernate) and
        // the JUnit tests.
    }

    /**
     * Uses the setters to set attributes.See setters for constraints
     *
     * @param question               References to the actual {@link Question} connected to this
     *                               answer.
     * @param isEnabled              Indicates whether this answer is enabled or not.
     * @param min                    states the lowest point of the range
     * @param max                    states the highest point of the range
     * @param stepsize               states the difference between two marks
     * @param localizedFreetextLabel see {@link SliderFreetextAnswer#getLocalizedFreetextLabel()} for
     *                                  description
     * @param vertical               States if the slider should be displayed vertically.
     */
    public SliderFreetextAnswer(final Question question, final Boolean isEnabled, final Double min,
        final Double max, final Double stepsize, final Map<String, String> localizedFreetextLabel,
        final Boolean vertical) {
        super(question, isEnabled, min, max, stepsize, vertical);
        setLocalizedFreetextLabel(localizedFreetextLabel);
    }

    @Override
    public SliderFreetextAnswer cloneWithoutReferences() {
        SliderFreetextAnswer sliderFreetextAnswer = new SliderFreetextAnswer();
        sliderFreetextAnswer.setLocalizedFreetextLabel(new HashMap<>(getLocalizedFreetextLabel()));
        if (getLocalizedMinimumText() == null) {
            sliderFreetextAnswer.setLocalizedMinimumText(null);
        } else {
            sliderFreetextAnswer.setLocalizedMinimumText(new HashMap<>(getLocalizedMinimumText()));
        }
        if (getLocalizedMaximumText() == null) {
            sliderFreetextAnswer.setLocalizedMaximumText(null);
        } else {
            sliderFreetextAnswer.setLocalizedMaximumText(new HashMap<>(getLocalizedMaximumText()));
        }
        sliderFreetextAnswer.setIsEnabled(this.getIsEnabled());
        sliderFreetextAnswer.setMinMax(getMinValue(), getMaxValue());
        sliderFreetextAnswer.setStepsize(getStepsize());
        sliderFreetextAnswer.setVertical(this.getVertical());
        sliderFreetextAnswer.setShowValueOnButton(this.getShowValueOnButton());
        return sliderFreetextAnswer;
    }

    /**
     * Returns a map with labels in different languages. The key of this map is the appropriate
     * language. The labels are shown directly beside the freetext input field, to let the user know
     * what to enter in the freetext input field.
     *
     * @return Is never <code>null</code>. Is never empty.
     */
    public Map<String, String> getLocalizedFreetextLabel() {
        return localizedFreetextLabel;
    }

    /**
     * See {@link SliderFreetextAnswer#getLocalizedFreetextLabel()} for a description of the field
     *
     * @param localizedFreetextLabel must not be <code>null</code>. Must not be empty.
     */
    public void setLocalizedFreetextLabel(final Map<String, String> localizedFreetextLabel) {
        assert
            localizedFreetextLabel != null :
            "The given map with localized freetext labels was " + "null";
        assert !localizedFreetextLabel.isEmpty() :
            "The given map with " + "localized freetext labels was empty";
        this.localizedFreetextLabel = localizedFreetextLabel;
    }

    @Override
    public String toString() {
        return "{min: " + getMinValue() + ", max: " + getMaxValue() + ", stepsize: " + getStepsize()
            + "}";
    }
}
