package de.imi.mopat.model;

import java.io.Serializable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Represents an {@link ExportRule} corresponding to an {@link Answer}.
 */
@Entity
@DiscriminatorValue("ExportRuleAnswer")
public class ExportRuleAnswer extends ExportRule implements Serializable {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "answer_id", referencedColumnName = "id")
    private Answer answer;
    @Column(name = "use_freetext_value")
    private Boolean useFreetextValue = false;

    /**
     * Default constructor (in protected state), should not be accessible to anything else but the
     * JPA implementation (here: Hibernate) and the JUnit tests.
     */
    protected ExportRuleAnswer() {

    }

    /**
     * Constructor. See
     * {@link ExportRule#ExportRule(de.imi.mopat.model.ExportTemplate, java.lang.String)} and
     * {@link ExportRuleAnswer#setAnswer(de.imi.mopat.model.Answer)}.
     *
     * @param exportTemplate The {@link ExportTemplate} object to which this export rule should
     *                       belong.
     * @param exportField    The export field to indicate the export target.
     * @param answer         The {@link Answer} object to which this export rule should belong.
     */
    public ExportRuleAnswer(final ExportTemplate exportTemplate, final String exportField,
        final Answer answer) {
        super(exportTemplate, exportField);
        setAnswer(answer);
    }

    /**
     * Constructor. See
     * {@link ExportRule#ExportRule(de.imi.mopat.model.ExportTemplate, java.lang.String)} and
     * {@link ExportRuleAnswer#setAnswer(de.imi.mopat.model.Answer)} and null null
     * {@link ExportRuleAnswer#setUseFreetextValue(java.lang.Boolean)}.
     *
     * @param exportTemplate   The {@link ExportTemplate} object to which this export rule should
     *                         belong.
     * @param exportField      The export field to indicate the export target.
     * @param answer           The {@link Answer} object to which this export rule should belong.
     * @param useFreetextValue Indicates this export rule should use the freetext value for
     *                         exporting.
     */
    public ExportRuleAnswer(final ExportTemplate exportTemplate, final String exportField,
        final Answer answer, final Boolean useFreetextValue) {
        super(exportTemplate, exportField);
        setAnswer(answer);
        setUseFreetextValue(useFreetextValue);
    }

    /**
     * Returns the {@link Answer} object to which this {@link ExportRule} belongs.
     *
     * @return The {@link Answer} object to which this {@link ExportRule} belongs.
     */
    public Answer getAnswer() {
        return answer;
    }

    /**
     * Sets the {@link Answer} object to which this {@link ExportRule} belongs.
     *
     * @param answer The {@link Answer} object to which this {@link ExportRule} should belong.
     */
    public final void setAnswer(final Answer answer) {
        this.answer = answer;
        // Take care that the objects know each other
        if (!answer.getExportRules().contains(this)) {
            answer.addExportRule(this);
        }
    }

    /**
     * Removes the {@link Answer} object to which this {@link ExportRule} belongs currently.
     */
    public void removeAnswer() {
        if (answer != null) {
            Answer answerTemp = answer;
            answer = null;
            if (answerTemp.getExportRules().contains(this)) {
                answerTemp.removeExportRule(this);
            }
        }
    }

    /**
     * Indicates if the exporter should access the freetext value of the {@link Answer}. Is never
     * <code>null</code>.
     *
     * @return Indicates if the exporter should access the freetext value of the {@link Answer}.
     */
    public Boolean getUseFreetextValue() {
        return useFreetextValue;
    }

    /**
     * Set if the exporter should access the freetext value of the {@link Answer}.
     *
     * @param useFreetextValue If the exporter should access the freetext value of the
     *                         {@link Answer}. Can not be
     *                         <code>null</code>.
     */
    public void setUseFreetextValue(final Boolean useFreetextValue) {
        assert useFreetextValue != null : "The useFreetextValue was null";
        this.useFreetextValue = useFreetextValue;
    }
}
