package de.imi.mopat.model;

import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.helper.model.UUIDGenerator;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The database table model for table <i>export_rule_format</i>. An export rule format object
 * represents a configuration of different format options. One or more {@link ExportRule} objects
 * refers to an {@link ExportRuleFormat} object.
 */
@Entity
@Table(name = "export_rule_format")
public class ExportRuleFormat implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();

    @Column(name = "number_type")
    @Enumerated(EnumType.STRING)
    private ExportNumberType numberType;

    @Column(name = "rounding_strategy")
    @Enumerated(EnumType.STRING)
    private ExportRoundingStrategyType roundingStrategy;

    @Column(name = "decimal_places")
    private Integer decimalPlaces;

    @Column(name = "decimal_delimiter")
    @Enumerated(EnumType.STRING)
    private ExportDecimalDelimiterType decimalDelimiter;

    @Column(name = "date_format")
    @Enumerated(EnumType.STRING)
    private ExportDateFormatType dateFormat;

    @OneToMany(mappedBy = "exportRuleFormat", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<ExportRule> exportRules = new HashSet<>();

    /**
     * Default constructor to create empty objects and accessible to the JPA implementation (here:
     * Hibernate) and the JUnit tests.
     */
    public ExportRuleFormat() {

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
     * Return the Uuid of this object.
     *
     * @return The Uuid of this object.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the current {@link ExportNumberType} object.
     *
     * @return The current {@link ExportNumberType} object. Can be
     * <code>null</code>.
     */
    public ExportNumberType getNumberType() {
        return numberType;
    }

    /**
     * Sets the {@link ExportNumberType} object.
     *
     * @param numberType The new {@link ExportNumberType} object to be set.
     */
    public void setNumberType(final ExportNumberType numberType) {
        this.numberType = numberType;
    }

    /**
     * Returns the current {@link ExportRoundingStrategyType} object.
     *
     * @return The current {@link ExportRoundingStrategyType} object. Can be
     * <code>null</code>.
     */
    public ExportRoundingStrategyType getRoundingStrategy() {
        return roundingStrategy;
    }

    /**
     * Sets the {@link ExportRoundingStrategyType} object.
     *
     * @param roundingStrategy The new {@link ExportRoundingStrategyType} object to be set.
     */
    public void setRoundingStrategy(final ExportRoundingStrategyType roundingStrategy) {
        this.roundingStrategy = roundingStrategy;
    }

    /**
     * Returns the number of decimal places a exported value should have. Will be used only if the
     * value is a floating point number indicated by {@link ExportRuleFormat#getNumberType()}.
     *
     * @return The number of decimal places a exported value should have. Can be
     * <code>null</code> otherwise must be {@code >= 0}.
     */
    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    /**
     * Sets the number of decimal places a export value should have. Can be
     * <code>null</code> otherwise must be {@code >= 0} .
     *
     * @param decimalPlaces The decimal places to be set.
     */
    public void setDecimalPlaces(final Integer decimalPlaces) {
        assert decimalPlaces != null && decimalPlaces >= 0 : "The given paramter was < 0";
        this.decimalPlaces = decimalPlaces;
    }

    /**
     * Returns a {@link ExportDecimalDelimiterType} object which indicates which decimal separator
     * should be used.
     *
     * @return The {@link ExportDecimalDelimiterType} object which indicates which decimal separator
     * should be used. Can be <code>null</code>.
     */
    public ExportDecimalDelimiterType getDecimalDelimiter() {
        return decimalDelimiter;
    }

    /**
     * Sets the new {@link ExportDecimalDelimiterType} object.
     *
     * @param decimalDelimiter the new {@link ExportDecimalDelimiterType} object to be set.
     */
    public void setDecimalDelimiter(final ExportDecimalDelimiterType decimalDelimiter) {
        this.decimalDelimiter = decimalDelimiter;
    }

    /**
     * Returns the {@link ExportDateFormatType} object which indicates how a date should be
     * formatted.
     *
     * @return The {@link ExportDateFormatType} which indicates how a date should be formatted. Can
     * be <code>null</code>.
     */
    public ExportDateFormatType getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the new {@link ExportDateFormatType} object.
     *
     * @param dateFormat The new {@link ExportDateFormatType} object to be set.
     */
    public void setDateFormat(final ExportDateFormatType dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * See {@link ExportTemplate#getExportRules()} for a description for export rules.
     * <p>
     * Returns all {@link ExportRule} objects using this export rule format object and therefore
     * contains all export rules owning this export rule format.
     *
     * @return Can be empty, but is never <code>null</code>. Is unmodifiable.
     */
    public Set<ExportRule> getExportRules() {
        return Collections.unmodifiableSet(exportRules);
    }

    /**
     * Adds all given {@link ExportRuleAnswer} objects that are not already associated with this
     * export rule format to the corresponding set of ExportRules. Takes care that the export rules
     * refer to this export rule format, too.
     *
     * @param exportRules The set of additional {@link ExportRuleAnswer} objects for this export
     *                    rule format object. Is never
     *                    <code>null</code>.
     * @throws AssertionError if the given parameter is invalid.
     */
    public void addExportRules(final Set<ExportRule> exportRules) {
        assert exportRules != null : "The given ExportRules were null";
        for (ExportRule exportRule : exportRules) {
            addExportRule(exportRule);
        }
    }

    /**
     * See {@link ExportRuleFormat#getExportRules()} for a description for exportRule.
     * <p>
     * Takes care that the {@link ExportRule} refers to this export rule format as well.
     *
     * @param exportRule Must not be <code>null</code>.
     * @throws AssertionError if the given parameter is invalid
     */
    public void addExportRule(final ExportRule exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        if (!exportRules.contains(exportRule)) {
            exportRules.add(exportRule);
        }
        // take care the objects know each other
        if (exportRule.getExportRuleFormat() == null || !exportRule.getExportRuleFormat()
            .equals(this)) {
            exportRule.setExportRuleFormat(this);
        }
    }

    /**
     * Removes an {@link ExportRule ExportRule} object.
     *
     * @param exportRule A {@link ExportRule ExportRule} object.
     */
    public void removeExportRule(final ExportRule exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        exportRules.remove(exportRule);
        if (exportRule.getExportRuleFormat() != null && exportRule.getExportRuleFormat()
            .equals(this)) {
            exportRule.removeExportRuleFormat();
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
        if (!(obj instanceof ExportRuleFormat)) {
            return false;
        }
        ExportRuleFormat other = (ExportRuleFormat) obj;
        return getUuid().equals(other.getUuid());
    }
}