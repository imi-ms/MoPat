package de.imi.mopat.model;

import java.io.Serializable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Represents an {@link ExportRule} corresponding to an {@link Question}.
 */
@Entity
@DiscriminatorValue("ExportRuleQuestion")
public class ExportRuleQuestion extends ExportRule implements Serializable {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private Question question;

    /**
     * Default constructor (in protected state), should not be accessible to anything else but the
     * JPA implementation (here: Hibernate) and the JUnit tests.
     */
    protected ExportRuleQuestion() {

    }

    /**
     * Constructor. See
     * {@link ExportRule#ExportRule(de.imi.mopat.model.ExportTemplate, java.lang.String)} and
     * {ExportRuleAnswer#setQuestion(de.imi.mopat.model.Question)}.
     *
     * @param exportTemplate The {@link ExportTemplate} object to which this export rule should
     *                       belong.
     * @param exportField    The export field to indicate the export target.
     * @param question       The {@link Question} object to which this export rule should belong.
     */
    public ExportRuleQuestion(final ExportTemplate exportTemplate, final String exportField,
        final Question question) {
        super(exportTemplate, exportField);
        setQuestion(question);
    }

    /**
     * Returns the {@link Question} object to which this {@link ExportRule} belongs.
     *
     * @return The {@link Question} object to which this {@link ExportRule} belongs.
     */
    public Question getQuestion() {
        return question;
    }

    /**
     * Sets the {@link Question} object to which this {@link ExportRule} belongs.
     *
     * @param question The {@link Answer} object to which this {@link ExportRule} should belong.
     */
    public final void setQuestion(Question question) {
        this.question = question;
        // Take care that the objects know each other
        if (!question.getExportRules().contains(this)) {
            question.addExportRule(this);
        }
    }

    /**
     * Removes the {@link Question} object to which this {@link ExportRule} belongs currently.
     */
    public void removeQuestion() {
        if (question != null) {
            Question questionTemp = question;
            question = null;
            if (questionTemp.getExportRules().contains(this)) {
                questionTemp.removeExportRule(this);
            }
        }
    }
}
