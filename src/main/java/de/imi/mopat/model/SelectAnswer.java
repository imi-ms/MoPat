package de.imi.mopat.model;

import de.imi.mopat.model.enumeration.BodyPart;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.MapKeyColumn;

/**
 * A <i>select</i> answer represents one answer of a multiple-choice question. The user can select
 * one of these answers.
 */
@Entity
@DiscriminatorValue("SelectAnswer")
public class SelectAnswer extends Answer implements Serializable {

    @NotNull(message = "{selectAnswer.label.notNull}")
    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "label")
    @CollectionTable(name = "answer_label", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedLabel;
    @Column(name = "isOther")
    private Boolean isOther;
    @Column(name = "value")
    //@Column(nullable = "false") is not possible due to our inheritance
    // strategy (see Answer). Thus, the annotation @NotNull
    // for jakarta.validation and JavaDoc together with asserts and tests will
    // be used
    private Double value;
    // Numerical value used for scoring; can be null since scoring is
    // not mandatory
    @Column(name = "coded_value")
    private String codedValue;

    protected SelectAnswer() {
        // default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation
        // (here: Hibernate) and the JUnit tests
    }

    /**
     * The answer object inherits different options of {@link Answer Answers}. Thus the model
     * {@link SelectAnswer} represents the option to choose between a number of
     * {@link Answer Answers}.
     *
     * @param question       References to the actual {@link Question} connected to this answer.
     * @param isEnabled      Indicates whether this answer is enabled or not.
     * @param localizedLabel A map with localized labels for this {@link Answer}.
     * @param isOther        Indicates whether this answer is marked as other or not.
     */
    public SelectAnswer(final Question question, final Boolean isEnabled,
        final Map<String, String> localizedLabel, final Boolean isOther) {
        super(question, isEnabled);
        setLocalizedLabel(localizedLabel);
        setIsOther(isOther);
    }

    public SelectAnswer(final Question question, final Boolean isEnabled, final BodyPart bodyPart) {
        super(question, isEnabled);
    }

    @Override
    public SelectAnswer cloneWithoutReferences() {
        SelectAnswer selectAnswer = new SelectAnswer();
        selectAnswer.setIsEnabled(this.getIsEnabled());
        selectAnswer.setIsOther(this.getIsOther());
        selectAnswer.setLocalizedLabel(new HashMap<>(getLocalizedLabel()));
        selectAnswer.setValue(this.getValue());
        selectAnswer.setCodedValue(this.getCodedValue());
        return selectAnswer;
    }

    /**
     * Returns a map with localized display texts of the {@link Answer}.
     *
     * @return A map with localized display texts of the {@link Answer}. Is never <code>null</code>.
     * Is not empty.
     */
    public Map<String, String> getLocalizedLabel() {
        return localizedLabel;
    }

    /**
     * Groups all localized answer labels for this select {@link Answer} by country. The country
     * code is the key in the outer map. The inner map contains the language code as key and the
     * localized answer labels as value.
     *
     * @return A map with localized answer labels grouped by country
     */
    public SortedMap<String, Map<String, String>> getLocalizedAnswerLabelGroupedByCountry() {
        SortedMap<String, Map<String, String>> groupedLocalizedAnswerLabelByCountry = new TreeMap<>();
        // Loop through each localized answer label
        for (Map.Entry<String, String> entry : this.getLocalizedLabel().entrySet()) {
            // Get the locale code
            String localeCode = entry.getKey();
            // Set the country to the locale code by default
            String country = localeCode.toUpperCase();
            // Set the answer label to the localized answer label by default
            String answerLabel = entry.getValue();
            // If the locale contains country and language code seperated by '_'
            // split this locale code and get the country from the second part.
            // (i.e. de_DE --> country is DE). The first part of the split
            // result is the language code.
            if (localeCode.contains("_")) {
                String[] parts = localeCode.split("_");
                country = parts[1];
                answerLabel = entry.getValue();
            }

            // If the sorted map already contains the country, add this
            // localized
            // answer label with its related language code
            if (groupedLocalizedAnswerLabelByCountry.containsKey(country)) {
                groupedLocalizedAnswerLabelByCountry.get(country).put(localeCode, answerLabel);
                // Otherwise this is the first answer label for this country
                // and a new
                // map for the answer labels has to be setup and filled with
                // the first
                // answer label and its related language code
            } else {
                Map<String, String> localeAnswerLabelMap = new HashMap<>();
                localeAnswerLabelMap.put(localeCode, answerLabel);
                groupedLocalizedAnswerLabelByCountry.put(country, localeAnswerLabelMap);
            }
        }
        return groupedLocalizedAnswerLabelByCountry;
    }

    /**
     * Sets the new display text of the {@link Answer}.
     *
     * @param localizedLabel The map with the new localized display texts of the {@link Answer}.
     *                       Must not be <code>null</code>. Must not be empty.
     */
    public void setLocalizedLabel(final Map<String, String> localizedLabel) {
        assert localizedLabel != null : "The given map with localized labels was null";
        assert !localizedLabel.isEmpty() : "The given map with localized " + "labels was empty";
        this.localizedLabel = localizedLabel;
    }

    /**
     * Some {@link Questionnaire Questionnaires} use values for their {@link Answer Answers} to
     * calculate a score for classification.
     *
     * @return value Might be <code>null</code>.
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets a new numerical value for the {@link Answer} (used for scoring). See
     * {@link SelectAnswer#getValue()} for a description.
     *
     * @param value Can be <code>null</code>.
     */
    public void setValue(final Double value) {
        this.value = value;
    }

    /**
     * Returns whether this {@link SelectAnswer} is marked as 'other' or not.
     *
     * @return boolean value whether this {@link SelectAnswer} is marked as 'other' or not.
     */
    public boolean getIsOther() {
        return this.isOther;
    }

    /**
     * Sets a new boolean value for the {@link SelectAnswer SelectAnswers} isOther attribute. See
     * {@link SelectAnswer#getIsOther()} for a description.
     *
     * @param isOther New isOther value for this answer.
     */
    public void setIsOther(final boolean isOther) {
        this.isOther = isOther;
    }

    /**
     * Returns the coded value of this {@link Answer Answers}.
     *
     * @return value Might be <code>null</code>.
     */
    public String getCodedValue() {
        return codedValue;
    }

    /**
     * Sets a new coded value for the {@link Answer}. See {@link SelectAnswer#getCodedValue()} for a
     * description.
     *
     * @param codedValue Can be <code>null</code>.
     */
    public void setCodedValue(final String codedValue) {
        this.codedValue = codedValue;
    }
}
