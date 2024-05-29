package de.imi.mopat.model;

import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.enumeration.ExportStatus;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The database table model for table <i>encounter_export_template</i>. This model stores if an
 * encounter was exported and saves the export time and if the export was done automatically or
 * manually.
 */
@Entity
@Table(name = "encounter_export_template")
public class EncounterExportTemplate implements Serializable, Comparable<EncounterExportTemplate> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "encounter_id", referencedColumnName = "id")
    private Encounter encounter;
    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "export_template_id", referencedColumnName = "id")
    private ExportTemplate exportTemplate;
    @Enumerated(EnumType.STRING)
    @Column(name = "export_status")
    private ExportStatus exportStatus = ExportStatus.FAILURE;
    @Column(name = "export_time", nullable = false)
    private Timestamp exportTime = new Timestamp(System.currentTimeMillis());
    @Column(name = "is_manually_exported", nullable = false)
    private Boolean isManuallyExported = false;

    protected EncounterExportTemplate() { //default constructor (in protected
        // state), should not be accessible to anything else but the JPA
        // implementation (here: Hibernate) and the JUnit tests

    }

    /**
     * Creates a new object. See
     * {@link EncounterExportTemplate#setEncounter(de.imi.mopat.model.Encounter) },
     * {@link EncounterExportTemplate#setExportTemplate(de.imi.mopat.model.ExportTemplate) } and
     * {@link EncounterExportTemplate#setExportStatus(de.imi.mopat.model.enumeration.ExportStatus)
     * }.
     *
     * @param encounter      An {@link Encounter} object. Can not be
     *                       <code>null</code>.
     * @param exportTemplate An {@link ExportTemplate} object. Can not be
     *                       <code>null</code>.
     * @param exportStatus   Sets the status of the export. Can not be
     *                       <code>null</code>.
     */
    public EncounterExportTemplate(final Encounter encounter, final ExportTemplate exportTemplate,
        final ExportStatus exportStatus) {
        setEncounter(encounter);
        setExportTemplate(exportTemplate);
        setExportStatus(exportStatus);
    }

    /**
     * Returns the id of the current encounter export template object.
     *
     * @return The current id of this encounter export template object. Might be
     * <code>null</code> for newly created objects. If <code>!null</code>, it's
     * never <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the encounter of the {@link Encounter}-{@link ExportTemplate} association.
     *
     * @return The {@link Encounter} object of the {@link Encounter}-{@link ExportTemplate}
     * association.
     */
    public Encounter getEncounter() {
        return encounter;
    }

    /**
     * Sets the encounter for the {@link Encounter}-{@link ExportTemplate} association. Takes care
     * that the {@link Encounter} objects refers to this one, too.
     *
     * @param encounter The new {@link Encounter} object of the
     *                  {@link Encounter}-{@link ExportTemplate} association. Must not be
     *                  <code>null</code>.
     * @throws AssertionError if the given parameter is invalid
     */
    public void setEncounter(final Encounter encounter) {
        assert encounter != null : "The given Encounter was null";
        this.encounter = encounter;
        //take care that the objects know each other
        if (!encounter.getEncounterExportTemplates().contains(this)) {
            encounter.addEncounterExportTemplate(this);
        }
    }

    /**
     * Returns the export template of the {@link Encounter}-{@link ExportTemplate} association.
     *
     * @return The {@link ExportTemplate ExportTemplate} object of the
     * {@link Encounter}-{@link ExportTemplate} association.
     */
    public ExportTemplate getExportTemplate() {
        return exportTemplate;
    }

    /**
     * Sets the encounter for the {@link Encounter}-{@link ExportTemplate} association. Takes care
     * that the {@link ExportTemplate} objects refers to this one, too.
     *
     * @param exportTemplate The new {@link ExportTemplate} object of the
     *                       {@link Encounter}-{@link ExportTemplate} association. Must not be
     *                       <code>null</code>.
     * @throws AssertionError if the given parameter is invalid
     */
    public void setExportTemplate(final ExportTemplate exportTemplate) {
        assert exportTemplate != null : "The given Bundle was null";
        this.exportTemplate = exportTemplate;
        // Take care that the objects know each other
        if (!exportTemplate.getEncounterExportTemplates().contains(this)) {
            exportTemplate.addEncounterExportTemplate(this);
        }
    }

    /**
     * Returnes the export time of this encounter-exportTemplate association. Usually it is after
     * the questionnaire was filled out and should be nearly equal to
     * {@link Encounter#getEndTime()}. This also is an indicator that the export was initialized
     * automatically.
     * <p>
     * If {@link Encounter#getEndTime()} is <code>null</code> an incomplete encounter was exported.
     * If the export time is a lot later than {@link Encounter#getEndTime()} it means the export was
     * executed manually.
     *
     * @return The export time of the {@link Encounter}-{@link ExportTemplate} association.
     */
    public Timestamp getExportTime() {
        return exportTime;
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EncounterExportTemplate)) {
            return false;
        }
        EncounterExportTemplate other = (EncounterExportTemplate) obj;
        return getUUID().equals(other.getUUID());
    }

    /**
     * Indicates if the export was successful, is in conflict or failed.
     *
     * @return SUCCESS, CONFLICT or FAILURE. Can not be <code>null</code>.
     */
    public ExportStatus getExportStatus() {
        return exportStatus;
    }

    /**
     * Set if the export was successful.
     *
     * @param exportStatus Set if the export was successful, in conflict or failure. Can not be
     *                     <code>null</code>.
     */
    public void setExportStatus(final ExportStatus exportStatus) {
        assert exportStatus != null : "isSuccessfullyExported is null";
        this.exportStatus = exportStatus;
    }

    /**
     * Indicates if the export was done manually.
     *
     * @return True if the export was done manually, otherwise false. Can not be
     * <code>null</code>.
     */
    public Boolean getIsManuallyExported() {
        return isManuallyExported;
    }

    /**
     * Set if the export was done manually.
     *
     * @param isManuallyExported Set if the export was done manually.
     */
    public void setIsManuallyExported(final Boolean isManuallyExported) {
        assert isManuallyExported != null : "isManuallyExported is null";
        this.isManuallyExported = isManuallyExported;
    }

    @Override
    public int compareTo(final EncounterExportTemplate other) {
        return this.exportTime.compareTo(other.exportTime);
    }
}
