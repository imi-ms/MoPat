package de.imi.mopat.model;

import de.imi.mopat.model.enumeration.ExportScoreFieldType;
import de.imi.mopat.model.score.Score;

import java.io.Serializable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Represents an {@link ExportRule} corresponding to an {@link Score} field.
 */
@Entity
@DiscriminatorValue("ExportRuleScore")
public class ExportRuleScore extends ExportRule implements Serializable {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "score_id", referencedColumnName = "id")
    private Score score;
    @Column(name = "score_field")
    @Enumerated(EnumType.STRING)
    private ExportScoreFieldType scoreField;

    /**
     * Default constructor (in protected state), should not be accessible to anything else but the
     * JPA implementation (here: Hibernate) and the JUnit tests
     */
    protected ExportRuleScore() {

    }

    /**
     * Constructor. See null
     * {@link ExportRule#ExportRule(de.imi.mopat.model.ExportTemplate, java.lang.String)}
     * ,{@link ExportRuleScore#setScore(de.imi.mopat.model.score.Score)} and
     * {@link ExportRuleScore#setScoreField(de.imi.mopat.model.enumeration.ExportScoreFieldType)}.
     *
     * @param exportTemplate The {@link ExportTemplate} object to which this export rule should
     *                       belong.
     * @param exportField    The export field to indicate the export target.
     * @param score          The {@link Score} object to which this export rule should belong.s
     * @param scoreField     The {@link ExportScoreFieldType} object to which this export rule
     *                       should belong.
     */
    public ExportRuleScore(final ExportTemplate exportTemplate, final String exportField,
        final Score score, final ExportScoreFieldType scoreField) {
        super(exportTemplate, exportField);
        setScoreField(scoreField);
        setScore(score);
    }

    /**
     * Returns the {@link ExportScoreFieldType} to which this {@link ExportRule} belongs.
     *
     * @return The {@link ExportScoreFieldType} to which this {@link ExportRule} belongs.
     */
    public ExportScoreFieldType getScoreField() {
        return scoreField;
    }

    /**
     * Sets the {@link ExportScoreFieldType} object to which this {@link ExportRule} should belong.
     *
     * @param scoreField The {@link ExportScoreFieldType} object to which this {@link ExportRule}
     *                   should belong.
     */
    public void setScoreField(final ExportScoreFieldType scoreField) {
        this.scoreField = scoreField;
    }

    /**
     * Returns the {@link Score} to which this {@link ExportRule} belongs.
     *
     * @return The {@link Score} to which this {@link ExportRule} belongs.
     */
    public Score getScore() {
        return score;
    }

    /**
     * Sets the {@link Score} object to which this {@link ExportRule} should belong.
     *
     * @param score The {@link Score} object to which this {@link ExportRule} should belong.
     */
    public void setScore(final Score score) {
        this.score = score;
        // Take care that the objects know each other
        if (!score.getExportRules().contains(this)) {
            score.addExportRule(this);
        }
    }

    /**
     * Removes the {@link Score} object to which this {@link ExportRule} belongs currently.
     */
    public void removeScore() {
        if (score != null) {
            Score scoreTemp = score;
            score = null;
            // No contains neccessary, it will be removed if contained
            // otherwise nothing happens
            scoreTemp.removeExportRule(this);
        }
    }
}
