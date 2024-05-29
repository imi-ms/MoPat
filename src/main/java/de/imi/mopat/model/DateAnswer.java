package de.imi.mopat.model;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * A <i>date</i> answer represents a date as an answer from a question. The user can select a date
 * between optional specified boundaries.
 */
@Entity
@DiscriminatorValue("DateAnswer")
public class DateAnswer extends Answer implements Serializable {

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    protected DateAnswer() {
        // default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation
        // (here: Hibernate) and the JUnit tests
    }

    /**
     * The model DateAnswer gives the option to set boundaries for the selectable date.
     *
     * @param question  References to the actual {@link Question} connected to this answer.
     * @param isEnabled Indicates whether this answer is enabled or not.
     * @param startDate Sets the earliest date the user can choose. Can be
     *                  <code>null</code>
     * @param endDate   Sets the latest date the user can choose. Can be
     *                  <code>null</code>
     */
    public DateAnswer(final Question question, final Boolean isEnabled, final Date startDate,
        final Date endDate) {
        super(question, isEnabled);
        this.setStartDate(startDate);
        this.setEndDate(endDate);
    }

    @Override
    public DateAnswer cloneWithoutReferences() {
        DateAnswer dateAnswer = new DateAnswer();
        dateAnswer.setIsEnabled(this.getIsEnabled());
        dateAnswer.setStartDate(getStartDate());
        dateAnswer.setEndDate(getEndDate());
        return dateAnswer;
    }

    /**
     * Gets the earliest date the user can choose.
     *
     * @return Gets the earliest date the user can choose. Can be
     * <code>null</code>
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the earliest date the user can choose.
     *
     * @param date Sets the earliest date the user can choose. Can be
     *             <code>null</code>
     */
    public void setStartDate(final Date date) {
        this.startDate = date;
    }

    /**
     * Gets the latest date the user can choose.
     *
     * @return Gets the latest date the user can choose. Can be
     * <code>null</code>
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the latest date the user can choose.
     *
     * @param date Sets the latest date the user can choose. Can be
     *             <code>null</code>
     */
    public void setEndDate(final Date date) {
        this.endDate = date;
    }
}
