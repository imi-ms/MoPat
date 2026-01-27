package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.model.enumeration.ExportRuleType;
import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportEncounterFieldType;
import de.imi.mopat.model.enumeration.ExportScoreFieldType;

/**
 * This class represents the data transfer object of model {@link de.imi.mopat.model.ExportRule}
 * including its export templates and mappings to convert a model to json for import and export.
 */
@JsonInclude(Include.ALWAYS)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class JsonExportRuleDTO {

    private Long id;
    private String uuid;
    private String exportField;

    private JsonExportRuleFormatDTO exportRuleFormatDTO;
    private Long answerId;
    private Boolean useFreetextValue;
    private Long questionId = null;
    private Long scoreId = null;
    private ExportScoreFieldType scoreField = null;
    private ExportEncounterFieldType encounterField = null;
    private ExportDateFormatType dateFormat = null;
    private ExportRuleType type = null;

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

    public String getExportField() {
        return exportField;
    }

    public void setExportField(final String exportField) {
        assert exportField != null : "the exportField was null";
        this.exportField = exportField;
    }

    public JsonExportRuleFormatDTO getExportRuleFormat() {
        return exportRuleFormatDTO;
    }

    public void setExportRuleFormat(final JsonExportRuleFormatDTO exportRuleFormat) {
        this.exportRuleFormatDTO = exportRuleFormat;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(final Long answerId) {
        this.answerId = answerId;
    }

    public Boolean getUseFreetextValue() {
        return useFreetextValue;
    }

    public void setUseFreetextValue(final Boolean useFreetextValue) {
        this.useFreetextValue = useFreetextValue;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getScoreId() {
        return scoreId;
    }

    public void setScoreId(Long scoreId) {
        this.scoreId = scoreId;
    }

    public ExportScoreFieldType getScoreField() {
        return scoreField;
    }

    public void setScoreField(ExportScoreFieldType scoreField) {
        this.scoreField = scoreField;
    }

    public ExportRuleType getType() {
        return type;
    }

    public void setType(ExportRuleType type) {
        this.type = type;
    }

    public ExportEncounterFieldType getEncounterField() {
        return encounterField;
    }

    public void setEncounterField(ExportEncounterFieldType encounterField) {
        this.encounterField = encounterField;
    }

    public ExportDateFormatType getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(ExportDateFormatType dateFormat) {
        this.dateFormat = dateFormat;
    }
}