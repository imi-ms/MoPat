package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;

/**
 * This class represents the data transfer object of model {@link de.imi.mopat.model.ExportRuleFormat} including its
 * export templates and mappings to convert a model to json for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("exportRuleFormat")
public class JsonExportRuleFormatDTO {

    private Long id;
    private String uuid;

    private ExportNumberType numberType;

    private ExportRoundingStrategyType roundingStrategy;

    private Integer decimalPlaces;

    private ExportDecimalDelimiterType decimalDelimiter;

    private ExportDateFormatType dateFormat;


    /**
     * Retrieves the unique identifier.
     *
     * @return the ID as a Long value
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this object.
     *
     * @param id the ID to be set, represented as a Long value
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retrieves the unique UUID of this object.
     *
     * @return the UUID as a String
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the unique identifier (UUID) for this object.
     *
     * @param uuid the UUID to be set, represented as a String
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Retrieves the number type that determines whether the value is an integer or a float.
     *
     * @return the number type as an ExportNumberType
     */
    public ExportNumberType getNumberType() {
        return numberType;
    }

    /**
     * Sets the number type that determines whether the value is an integer or a float.
     *
     * @param numberType the number type to be set, represented as an ExportNumberType
     */
    public void setNumberType(final ExportNumberType numberType) {
        this.numberType = numberType;
    }

    /**
     * Retrieves the rounding strategy used for exporting numerical values.
     *
     * @return the rounding strategy as an ExportRoundingStrategyType
     */
    public ExportRoundingStrategyType getRoundingStrategy() {
        return roundingStrategy;
    }

    /**
     * Sets the rounding strategy to be used for exporting numerical values.
     *
     * @param roundingStrategy the rounding strategy to be set, represented as an {@link ExportRoundingStrategyType}
     */
    public void setRoundingStrategy(final ExportRoundingStrategyType roundingStrategy) {
        this.roundingStrategy = roundingStrategy;
    }

    /**
     * Retrieves the number of decimal places used in numerical values.
     *
     * @return the number of decimal places as an Integer
     */
    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    /**
     * Sets the number of decimal places to use for numerical values.
     *
     * @param decimalPlaces the number of decimal places to be set, represented as a non-negative Integer
     *                      value. Must not be null or less than zero.
     */
    public void setDecimalPlaces(final Integer decimalPlaces) {
        assert decimalPlaces != null && decimalPlaces >= 0 : "The given paramter was < 0";
        this.decimalPlaces = decimalPlaces;
    }

    /**
     * Retrieves the decimal delimiter used for formatting numerical values in the export.
     *
     * @return the decimal delimiter as an ExportDecimalDelimiterType
     */
    public ExportDecimalDelimiterType getDecimalDelimiter() {
        return decimalDelimiter;
    }

    /**
     * Sets the decimal delimiter to be used for formatting numerical values in the export.
     *
     * @param decimalDelimiter the decimal delimiter to be set, represented as an {@code ExportDecimalDelimiterType}.
     *                         It determines whether to use a dot (.) or a comma (,) as the decimal delimiter.
     */
    public void setDecimalDelimiter(final ExportDecimalDelimiterType decimalDelimiter) {
        this.decimalDelimiter = decimalDelimiter;
    }

    /**
     * Retrieves the date format to be used for exporting dates.
     *
     * @return the date format as an {@link ExportDateFormatType}
     */
    public ExportDateFormatType getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the date format to be used for exporting dates.
     *
     * @param dateFormat the date format to be set, represented as an {@link ExportDateFormatType}.
     *                   Determines how a date should be formatted.
     */
    public void setDateFormat(final ExportDateFormatType dateFormat) {
        this.dateFormat = dateFormat;
    }

}