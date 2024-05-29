package de.imi.mopat.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * An <i>image</i> answer represents an image as an answer from a question.
 */
@Entity
@DiscriminatorValue("ImageAnswer")
public class ImageAnswer extends Answer implements Serializable {

    @Column(name = "image_path")
    private String imagePath;

    protected ImageAnswer() {
        // default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation (here: Hibernate) and
        // the JUnit tests.
    }

    /**
     * Uses the setters to set attributes. See setters for constraints
     *
     * @param question  References to the actual {@link Question} connected to this answer.
     * @param isEnabled Indicates whether this answer is enabled or not.
     * @param imagePath The path where the image is stored.
     */
    public ImageAnswer(final Question question, final Boolean isEnabled, final String imagePath) {
        super(question, isEnabled);
        setImagePath(imagePath);
    }

    @Override
    public ImageAnswer cloneWithoutReferences() {
        ImageAnswer imageAnswer = new ImageAnswer();
        imageAnswer.setIsEnabled(this.getIsEnabled());
        imageAnswer.setImagePath(this.getImagePath());
        return imageAnswer;
    }

    /**
     * Sets the path where the image of an image question is stored.
     *
     * @param imagePath Can be <code>null</code>.
     */
    public void setImagePath(final String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Returns the path where the image is stored. Is only important for questions of type
     * {QuestionType#IMAGE}
     *
     * @return Might be <code>null</code>.
     */
    public String getImagePath() {
        return imagePath;
    }
}
