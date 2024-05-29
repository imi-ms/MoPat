package de.imi.mopat.model;

import de.imi.mopat.model.enumeration.ExportEncounterFieldType;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Represents an {@link ExportRule} corresponding to an {@link Encounter} field.
 */
@Entity
@DiscriminatorValue("ExportRuleEncounter")
public class ExportRuleEncounter extends ExportRule implements Serializable {

    @Column(name = "encounter_field")
    @Enumerated(EnumType.STRING)
    private ExportEncounterFieldType encounterField;

    /**
     * Default constructor (in protected state), should not be accessible to anything else but the
     * JPA implementation (here: Hibernate) and the JUnit tests
     */
    protected ExportRuleEncounter() {

    }

    /**
     * Constructor. See
     * {@link ExportRule#ExportRule(de.imi.mopat.model.ExportTemplate, java.lang.String)} and
     * {@link
     * ExportRuleEncounter#setEncounterField(de.imi.mopat.model.enumeration.ExportEncounterFieldType)
     * }.
     *
     * @param exportTemplate The {@link ExportTemplate} object to which this export rule should
     *                       belong.
     * @param exportField    The export field to indicate the export target.
     * @param encounterField The {@link ExportEncounterFieldType} object to which this export rule
     *                       should belong.
     */
    public ExportRuleEncounter(final ExportTemplate exportTemplate, final String exportField,
        final ExportEncounterFieldType encounterField) {
        super(exportTemplate, exportField);
        setEncounterField(encounterField);
    }

    /**
     * Returns the {@link ExportEncounterFieldType} to which this {@link ExportRule} belongs.
     *
     * @return The {@link ExportEncounterFieldType} to which this {@link ExportRule} belongs.
     */
    public ExportEncounterFieldType getEncounterField() {
        return encounterField;
    }

    /**
     * Sets the {@link ExportEncounterFieldType} object to which this {@link ExportRule} should
     * belong.
     *
     * @param encounterField The {@link ExportEncounterFieldType} object to which this
     *                       {@link ExportRule} should belong.
     */
    public void setEncounterField(final ExportEncounterFieldType encounterField) {
        this.encounterField = encounterField;
    }
}
