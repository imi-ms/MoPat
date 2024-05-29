package de.imi.mopat.model;

import de.imi.mopat.model.enumeration.BodyPart;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * A <i>BodyPartAnswer</i> represents a selectable region inside an image of a {@link Question} of
 * QuestionType#BODY_PART.
 */
@Entity
@DiscriminatorValue("BodyPartAnswer")
public class BodyPartAnswer extends Answer implements Serializable {

    // Holds information about the selectable body region this answer represents
    @Column(name = "body_part")
    private BodyPart bodyPart;

    // Default constructor
    public BodyPartAnswer() {
        super();
    }

    /**
     * Constructor of BodyPartAnswer
     *
     * @param bodyPart  Selectable body region this answer represents.
     * @param question  Question this answer belongs to.
     * @param isEnabled Holds information if this answer is enabled or not.
     */
    public BodyPartAnswer(final BodyPart bodyPart, final Question question,
        final Boolean isEnabled) {
        super(question, isEnabled);
        this.bodyPart = bodyPart;
    }

    @Override
    public Answer cloneWithoutReferences() {
        BodyPartAnswer bodyPartAnswer = new BodyPartAnswer();
        bodyPartAnswer.setBodyPart(this.getBodyPart());
        bodyPartAnswer.setIsEnabled(this.getIsEnabled());
        return bodyPartAnswer;
    }

    /**
     * Get the {@link BodyPart} this answer represents.
     *
     * @return {@link BodyPart} that represents a selectable body region.
     */
    public BodyPart getBodyPart() {
        return bodyPart;
    }

    /**
     * Set the {@link BodyPart} this answer represents.
     *
     * @param bodyPart {@link BodyPart} that represents a selectable body region.
     */
    public void setBodyPart(final BodyPart bodyPart) {
        this.bodyPart = bodyPart;
    }
}
