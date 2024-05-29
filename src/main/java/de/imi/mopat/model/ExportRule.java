package de.imi.mopat.model;

import de.imi.mopat.helper.model.UUIDGenerator;

import java.io.Serializable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The database table model for table <i>export_rule</i>. An export rule object knows which
 * questionnaire value should be exported to which field in the export template. It also holds a
 * reference to an {@link ExportRuleFormat} object to format the questionnaire value. Every field in
 * an export template can only be in a single export rule which belongs to the same export
 * template.
 */
@Entity
@Table(name = "export_rule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "export_rule_type", discriminatorType = DiscriminatorType.STRING)
public abstract class ExportRule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @Column(name = "export_field")
    private String exportField;
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    @JoinColumn(name = "export_template_id", referencedColumnName = "id")
    private ExportTemplate exportTemplate;
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "export_rule_format_id", referencedColumnName = "id")
    private ExportRuleFormat exportRuleFormat;

    /**
     * Default constructor (in protected state), should not be accessible to anything else but the
     * JPA implementation (here: Hibernate) and the JUnit tests
     */
    protected ExportRule() {
    }

    /**
     * Constructor. See {@link ExportRule#setExportTemplate(de.imi.mopat.model.ExportTemplate) } and
     * {@link ExportRule#setExportField(java.lang.String) }.
     *
     * @param export      An {@link ExportTemplate} object.
     * @param exportField An export field.
     */
    public ExportRule(final ExportTemplate export, final String exportField) {
        setExportTemplate(export);
        setExportField(exportField);
    }

    /**
     * Returns the Id of this object.
     *
     * @return The Id of this object.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the uuid of this object.
     *
     * @return The uuid of this object.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the export field.
     *
     * @param exportField An export field. Can not be <code>null</code>.
     */
    public void setExportField(final String exportField) {
        assert exportField != null : "the exportField was null";
        this.exportField = exportField;
    }

    /**
     * Returns the export field defined by the template.
     *
     * @return The export field. Can not be <code>null</code>.
     */
    public String getExportField() {
        return exportField;
    }

    /**
     * Sets the {@link ExportTemplate} object to which this export rule belongs.
     * <p>
     * Takes care that the {@link ExportTemplate} refers to this export rule as well.
     *
     * @param exportTemplate An {@link ExportTemplate} object. Can not be
     *                       <code>null</code>.
     */
    public void setExportTemplate(final ExportTemplate exportTemplate) {
        assert exportTemplate != null : "The ExportTemplate was null";
        this.exportTemplate = exportTemplate;
        // Take care that the objects know each other
        if (!exportTemplate.getExportRules().contains(this)) {
            exportTemplate.addExportRule(this);
        }
    }

    /**
     * Returns the {@link ExportTemplate} object to which this export rule belongs.
     *
     * @return The {@link ExportTemplate} object to which this export rule belongs. Can not be
     * <code>null</code>.
     */
    public ExportTemplate getExportTemplate() {
        return exportTemplate;
    }

    /**
     * Returns the {@link ExportRuleFormat}.
     *
     * @return The {@link ExportRuleFormat}. Can not be <code>null</code>.
     */
    public ExportRuleFormat getExportRuleFormat() {
        return exportRuleFormat;
    }

    /**
     * Sets the {@link ExportRuleFormat} which indicates how the value should be formatted.
     *
     * @param exportRuleFormat The {@link ExportRuleFormat} to be set.
     */
    public void setExportRuleFormat(final ExportRuleFormat exportRuleFormat) {
        this.exportRuleFormat = exportRuleFormat;
        // Take care that the objects know each other
        if (!exportRuleFormat.getExportRules().contains(this)) {
            exportRuleFormat.addExportRule(this);
        }
    }

    /**
     * Removes the {@link ExportRuleFormat} object.
     */
    public void removeExportRuleFormat() {
        if (exportRuleFormat != null) {
            ExportRuleFormat exportRuleFormatTemp = exportRuleFormat;
            exportRuleFormat = null;
            if (exportRuleFormatTemp.getExportRules().contains(this)) {
                exportRuleFormatTemp.removeExportRule(this);
            }
        }
    }

    /**
     * Removes the {@link ExportTemplate} from the {@link ExportRule}. Will only be called if this
     * {@link ExportRule} gets removed.
     */
    public void removeExportTemplate() {
        if (exportTemplate != null) {
            ExportTemplate exportTemplateTemp = exportTemplate;
            exportTemplate = null;
            if (exportTemplateTemp.getExportRules().contains(this)) {
                exportTemplateTemp.removeExportRule(this);
            }
        }
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExportRule)) {
            return false;
        }
        ExportRule other = (ExportRule) obj;
        return getUuid().equals(other.getUuid());
    }
}
