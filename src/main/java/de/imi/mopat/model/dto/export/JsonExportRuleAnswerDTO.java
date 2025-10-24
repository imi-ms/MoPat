package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * This class represents the data transfer object of model {@link de.imi.mopat.model.ExportRule}
 * including its export templates and mappings to convert a model to json for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("exportRuleAnswer")
public class JsonExportRuleAnswerDTO {

    private Long id;
    private String uuid;
    private String exportField;

    private JsonExportRuleFormatDTO exportRuleFormatDTO;
    private Long answerId;
    private Boolean useFreetextValue;


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

    public void setExportField(final String exportField) {
        assert exportField != null : "the exportField was null";
        this.exportField = exportField;
    }

    public String getExportField() {
        return exportField;
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
}