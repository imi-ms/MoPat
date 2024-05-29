package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;

/**
 *
 */
public class ExportRuleFormatDTO {

    private Long id = null;
    private ExportNumberType numberType = null;
    private ExportRoundingStrategyType roundingStrategy = null;
    private String decimalPlaces = null;
    private ExportDecimalDelimiterType decimalDelimiter = null;
    private ExportDateFormatType dateFormat = null;

    /**
     * @return the numberType
     */
    public ExportNumberType getNumberType() {
        return numberType;
    }

    /**
     * @param numberType the numberType to set
     */
    public void setNumberType(final ExportNumberType numberType) {
        this.numberType = numberType;
    }

    /**
     * @return the roundingStrategy
     */
    public ExportRoundingStrategyType getRoundingStrategy() {
        return roundingStrategy;
    }

    /**
     * @param roundingStrategy the roundingStrategy to set
     */
    public void setRoundingStrategy(final ExportRoundingStrategyType roundingStrategy) {
        this.roundingStrategy = roundingStrategy;
    }

    /**
     * @return the decimalPlaces
     */
    public String getDecimalPlaces() {
        return decimalPlaces;
    }

    /**
     * @param decimalPlaces the decimalPlaces to set
     */
    public void setDecimalPlaces(final String decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    /**
     * @return the decimalDelimiter
     */
    public ExportDecimalDelimiterType getDecimalDelimiter() {
        return decimalDelimiter;
    }

    /**
     * @param decimalDelimiter the decimalDelimiter to set
     */
    public void setDecimalDelimiter(final ExportDecimalDelimiterType decimalDelimiter) {
        this.decimalDelimiter = decimalDelimiter;
    }

    /**
     * @return the dateFormat
     */
    public ExportDateFormatType getDateFormat() {
        return dateFormat;
    }

    /**
     * @param dateFormat the dateFormat to set
     */
    public void setDateFormat(ExportDateFormatType dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
}