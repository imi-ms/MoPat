package de.imi.mopat.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * The database table model for table <i>Statistic</i>. Each day is an object that the values ​​in
 * each case stores a value . This includes how many complete and not complete encounters there that
 * day . These values ​​are applicable for hospitals , bundleCount , users and questioannaires . In
 * addition to the free export various types .
 */
@Entity
@Table(name = "statistic")
public class Statistic implements Serializable, Comparable<Statistic> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "date", unique = true)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "questionnaireCount")
    private Long questionnaireCount;

    @Column(name = "bundleCount")
    private Long bundleCount;

    @Column(name = "clinicCount")
    private Long clinicCount;

    @Column(name = "userCount")
    private Long userCount;

    @Column(name = "encounterCount")
    private Long encounterCount;

    @Column(name = "incompleteEncounterCount")
    private Long incompleteEncounterCount;

    @Column(name = "completeEncounterDeletedCount")
    private Long completeEncounterDeletedCount;

    @Column(name = "incompleteEncounterDeletedCount")
    private Long incompleteEncounterDeletedCount;

    @Column(name = "ODMExportCount")
    private Long ODMExportCount;

    @Column(name = "ORBISExportCount")
    private Long ORBISExportCount;

    @Column(name = "HL7ExportCount")
    private Long HL7ExportCount;

    public Statistic() { //default constructor (in protected state), should
        // not be accessible to anything else but the JPA implementation
        // (here: Hibernate) and the JUnit tests
    }

    /**
     * Returns the id of the current statistic object.
     *
     * @return The current id of this statistic object. Might be
     * <code>null</code> for newly created objects. Is never <code> &lt;= 0
     * </code>
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the date of the current statistic object.
     *
     * @return The date of the current statistic object. Is never
     * <code>null</code>.
     */
    public Date getDate() {
        return date;
    }

    /**
     * See {@link Statistic#getDate()} for a description.Sets a new date for this statistic object.
     *
     * @param date The new date for this statistic object. Must not be
     *             <code>null</code>.
     */
    public void setDate(final Date date) {
        assert date != null : "The given date was null";
        this.date = date;
    }

    /**
     * Returns the questionnaire count of the current statistic object.
     *
     * @return The questionnaire count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getQuestionnaireCount() {
        return questionnaireCount;
    }

    /**
     * See {@link Statistic#getQuestionnaireCount()} for a description.Sets a new questionnaire
     * count for this statistic object.
     *
     * @param questionnaireCount The new questionnaire count for this statistic object. Must not be
     *                           <code>null</code>.
     */
    public void setQuestionnaireCount(final Long questionnaireCount) {
        assert questionnaireCount != null : "The given questionnaire count was null";
        this.questionnaireCount = questionnaireCount;
    }

    /**
     * Returns the bundle count of the current statistic object.
     *
     * @return The bundle count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getBundleCount() {
        return bundleCount;
    }

    /**
     * See {@link Statistic#getBundleCount()} for a description.Sets a new bundle count for this
     * statistic object.
     *
     * @param bundleCount The new bundle count for this statistic object. Must not be
     *                    <code>null</code>.
     */
    public void setBundleCount(final Long bundleCount) {
        assert bundleCount != null : "The given bundle count was null";
        this.bundleCount = bundleCount;
    }

    /**
     * Returns the clinic count of the current statistic object.
     *
     * @return The clinic count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getClinicCount() {
        return clinicCount;
    }

    /**
     * See {@link Statistic#getClinicCount()} for a description.Sets a new clinic count for this
     * statistic object.
     *
     * @param clinicCount The new clinic count for this statistic object. Must not be
     *                    <code>null</code>.
     */
    public void setClinicCount(final Long clinicCount) {
        assert clinicCount != null : "The given clinic count was null";
        this.clinicCount = clinicCount;
    }

    /**
     * Returns the user count of the current statistic object.
     *
     * @return The user count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getUserCount() {
        return userCount;
    }

    /**
     * See {@link Statistic#getUserCount()} for a description.Sets a new user count for this
     * statistic object.
     *
     * @param userCount The new user count for this statistic object. Must not be
     *                  <code>null</code>.
     */
    public void setUserCount(final Long userCount) {
        assert userCount != null : "The given user count was null";
        this.userCount = userCount;
    }

    /**
     * Returns the encounter count of the current statistic object.
     *
     * @return The encounter count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getEncounterCount() {
        return encounterCount;
    }

    /**
     * See {@link Statistic#getEncounterCount()} for a description.Sets a new encounter count for
     * this statistic object.
     *
     * @param encounterCount The new encounter count for this statistic object. Must not be
     *                       <code>null</code>.
     */
    public void setEncounterCount(final Long encounterCount) {
        assert encounterCount != null : "The given encounter count was null";
        this.encounterCount = encounterCount;
    }

    /**
     * Returns the incomplete encounter count of the current statistic object.
     *
     * @return The incomplete encounter count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getIncompleteEncounterCount() {
        return incompleteEncounterCount;
    }

    /**
     * See {@link Statistic#getIncompleteEncounterCount()} for a description .Sets a new incomplete
     * encounter count for this statistic object.
     *
     * @param incompleteEncounterCount The new incomplete encounter count for this statistic object.
     *                                 Must not be
     *                                 <code>null</code>.
     */
    public void setIncompleteEncounterCount(final Long incompleteEncounterCount) {
        assert incompleteEncounterCount != null : "The given incomplete encounter count was null";
        this.incompleteEncounterCount = incompleteEncounterCount;
    }

    /**
     * Returns the complete encounter deleted count of the current statistic object.
     *
     * @return The complete encounter deleted count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getCompleteEncounterDeletedCount() {
        return completeEncounterDeletedCount;
    }

    /**
     * See {@link Statistic#getCompleteEncounterDeletedCount()} for a description.Sets a new
     * complete encounter deleted count for this statistic object.
     *
     * @param completeEncounterDeletedCount The new complete encounter deleted count for this
     *                                      statistic object. Must not be <code>null</code>.
     */
    public void setCompleteEncounterDeletedCount(final Long completeEncounterDeletedCount) {
        assert
            completeEncounterDeletedCount != null : "The given complete encounter count was null";
        this.completeEncounterDeletedCount = completeEncounterDeletedCount;
    }

    /**
     * Returns the incomplete encounter deleted count of the current statistic object.
     *
     * @return The incomplete encounter deleted count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getIncompleteEncounterDeletedCount() {
        return incompleteEncounterDeletedCount;
    }

    /**
     * See {@link Statistic#getIncompleteEncounterDeletedCount()} for a description.Sets a new
     * incomplete encounter deleted count for this statistic object.
     *
     * @param incompleteEncounterDeletedCount The new incomplete encounter deleted count for this
     *                                        statistic object. Must not be
     *                                        <code>null</code>.
     */
    public void setIncompleteEncounterDeletedCount(final Long incompleteEncounterDeletedCount) {
        assert incompleteEncounterDeletedCount != null :
            "The given incomplete encounter deleted count was " + "null";
        this.incompleteEncounterDeletedCount = incompleteEncounterDeletedCount;
    }

    /**
     * Returns the ODM export count of the current statistic object.
     *
     * @return The ODM export count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getODMExportCount() {
        return ODMExportCount;
    }

    /**
     * See {@link Statistic#getODMExportCount()} for a description.Sets a new ODM export count for
     * this statistic object.
     *
     * @param ODMExportCount The new ODM export count for this statistic object. Must not be
     *                       <code>null</code>.
     */
    public void setODMExportCount(final Long ODMExportCount) {
        assert ODMExportCount != null : "The given ODM export count was null";
        this.ODMExportCount = ODMExportCount;
    }

    /**
     * Returns the ORBIS export count of the current statistic object.
     *
     * @return The ORBIS export count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getORBISExportCount() {
        return ORBISExportCount;
    }

    /**
     * See {@link Statistic#getORBISExportCount()} for a description.Sets a new ORBIS export count
     * for this statistic object.
     *
     * @param ORBISExportCount The new ORBIS export count for this statistic object. Must not be
     *                         <code>null</code>.
     */
    public void setORBISExportCount(final Long ORBISExportCount) {
        assert ORBISExportCount != null : "The given ORBIS export count was null";
        this.ORBISExportCount = ORBISExportCount;
    }

    /**
     * Returns the HL7 export count of the current statistic object.
     *
     * @return The HL7 export count of the current statistic object. Is never
     * <code>null</code>.
     */
    public Long getHL7ExportCount() {
        return HL7ExportCount;
    }

    /**
     * See {@link Statistic#getHL7ExportCount()} for a description.Sets a new HL7 export count for
     * this statistic object.
     *
     * @param HL7ExportCount The new HL7 export count for this statistic object. Must not be
     *                       <code>null</code>.
     */
    public void setHL7ExportCount(final Long HL7ExportCount) {
        assert HL7ExportCount != null : "The given HL7 export count was null";
        this.HL7ExportCount = HL7ExportCount;
    }

    @Override
    public int compareTo(final Statistic statistic) {
        if (this.getDate().after(statistic.getDate())) {
            return 1;
        } else if (statistic.getDate().after(this.getDate())) {
            return -1;
        } else {
            return 0;
        }
    }
}
