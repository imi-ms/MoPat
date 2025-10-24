package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;

/**
 * This class represents the data transfer object of model {@link de.imi.mopat.model.ExportRuleFormat} including its
 * export templates and mappings to convert a model to json for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ExportNumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(final ExportNumberType numberType) {
        this.numberType = numberType;
    }

    public ExportRoundingStrategyType getRoundingStrategy() {
        return roundingStrategy;
    }

    public void setRoundingStrategy(final ExportRoundingStrategyType roundingStrategy) {
        this.roundingStrategy = roundingStrategy;
    }

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(final Integer decimalPlaces) {
        assert decimalPlaces != null && decimalPlaces >= 0 : "The given paramter was < 0";
        this.decimalPlaces = decimalPlaces;
    }

    public ExportDecimalDelimiterType getDecimalDelimiter() {
        return decimalDelimiter;
    }

    public void setDecimalDelimiter(final ExportDecimalDelimiterType decimalDelimiter) {
        this.decimalDelimiter = decimalDelimiter;
    }

    public ExportDateFormatType getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(final ExportDateFormatType dateFormat) {
        this.dateFormat = dateFormat;
    }

}