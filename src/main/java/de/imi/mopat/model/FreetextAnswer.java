package de.imi.mopat.model;

import java.io.Serializable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Is meant for questions with only one input, being a free text field (represented by
 * {@link de.imi.mopat.model.enumeration.QuestionType#FREE_TEXT}). The super class {@link Answer}
 * already contains all necessary fields, but is an
 * <code>abstract</code> class. Thus, this class was created for free text
 * answers.
 */
@Entity
@DiscriminatorValue("FreetextAnswer")
public class FreetextAnswer extends Answer implements Serializable {

    protected FreetextAnswer() {
        // default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation (here: Hibernate) and
        // the JUnit tests.
    }

    /**
     * Uses the setters to set attributes. See setters for constraints
     *
     * @param question  References to the actual {@link Question} connected to this answer.
     * @param isEnabled Indicates whether this answer is enabled or not.
     */
    public FreetextAnswer(final Question question, final Boolean isEnabled) {
        super(question, isEnabled);
    }

    @Override
    public FreetextAnswer cloneWithoutReferences() {
        FreetextAnswer freetextAnswer = new FreetextAnswer();
        freetextAnswer.setIsEnabled(this.getIsEnabled());
        return freetextAnswer;
    }
}
