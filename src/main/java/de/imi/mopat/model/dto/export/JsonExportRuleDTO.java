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

    /**
     * Retrieves the unique identifier of the object.
     *
     * @return the ID of the object as a Long.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the object.
     *
     * @param id the ID to be assigned to the object as a Long value.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retrieves the unique identifier (UUID) of the object.
     *
     * @return the UUID of the object as a String.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the unique identifier (UUID) for the object.
     *
     * @param uuid the UUID to be assigned to the object as a String.
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Retrieves the value of the export field.
     *
     * @return the export field as a String.
     */
    public String getExportField() {
        return exportField;
    }

    /**
     * Sets the value of the export field.
     *
     * @param exportField the value to assign to the export field; must not be null
     */
    public void setExportField(final String exportField) {
        assert exportField != null : "the exportField was null";
        this.exportField = exportField;
    }

    /**
     * Retrieves the export rule format associated with the object.
     *
     * @return an instance of {@code JsonExportRuleFormatDTO} representing the export rule format,
     * or {@code null} if no export rule format is set.
     */
    public JsonExportRuleFormatDTO getExportRuleFormat() {
        return exportRuleFormatDTO;
    }

    /**
     * Sets the export rule format for the object.
     *
     * @param exportRuleFormat an instance of {@code JsonExportRuleFormatDTO} representing the export rule format
     *                         to be associated with the object; must not be null
     */
    public void setExportRuleFormat(final JsonExportRuleFormatDTO exportRuleFormat) {
        this.exportRuleFormatDTO = exportRuleFormat;
    }

    /**
     * Retrieves the unique identifier associated with the answer.
     *
     * @return the answer ID as a Long, or null if no ID is set.
     */
    public Long getAnswerId() {
        return answerId;
    }

    /**
     * Sets the unique identifier associated with the answer.
     *
     * @param answerId the ID to be assigned to the answer as a Long value; can be null if no ID is to be assigned.
     */
    public void setAnswerId(final Long answerId) {
        this.answerId = answerId;
    }

    /**
     * Retrieves the value indicating whether the use of free-text is enabled.
     *
     * @return a Boolean representing the free-text usage state;
     *         {@code true} if free-text is enabled, {@code false} otherwise,
     *         or {@code null} if not explicitly set.
     */
    public Boolean getUseFreetextValue() {
        return useFreetextValue;
    }

    /**
     * Sets the value indicating whether the use of free-text is enabled.
     *
     * @param useFreetextValue a Boolean representing the free-text usage state;
     *                         {@code true} to enable free-text usage, {@code false} to disable it,
     *                         or {@code null} if the state is not explicitly set
     */
    public void setUseFreetextValue(final Boolean useFreetextValue) {
        this.useFreetextValue = useFreetextValue;
    }

    /**
     * Retrieves the unique identifier associated with the question.
     *
     * @return the question ID as a Long, or null if no ID is set.
     */
    public Long getQuestionId() {
        return questionId;
    }

    /**
     * Sets the unique identifier associated with the question.
     *
     * @param questionId the ID of the question to be set as a Long value; can be null if no ID is to be assigned.
     */
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    /**
     * Retrieves the unique identifier associated with the score.
     *
     * @return the score ID as a Long, or null if no ID is set.
     */
    public Long getScoreId() {
        return scoreId;
    }

    /**
     * Sets the unique identifier associated with the score.
     *
     * @param scoreId the ID of the score to be assigned to the object as a Long value;
     *                can be null if no ID is to be set.
     */
    public void setScoreId(Long scoreId) {
        this.scoreId = scoreId;
    }

    /**
     * Retrieves the score field assigned to the object.
     *
     * @return an instance of {@code ExportScoreFieldType} representing the score field
     *         used in the export template mapping, or {@code null} if no score field is set.
     */
    public ExportScoreFieldType getScoreField() {
        return scoreField;
    }

    /**
     * Sets the score field used in the export template mapping.
     *
     * @param scoreField an instance of {@code ExportScoreFieldType} representing the score field
     *                   to be assigned; determines how related score values are handled during
     *                   the export process.
     */
    public void setScoreField(ExportScoreFieldType scoreField) {
        this.scoreField = scoreField;
    }

    /**
     * Retrieves the type associated with the export rule.
     *
     * @return an instance of {@code ExportRuleType} representing the type of the export rule,
     *         or {@code null} if no type is set.
     */
    public ExportRuleType getType() {
        return type;
    }

    /**
     * Sets the type associated with the export rule.
     *
     * @param type an instance of {@code ExportRuleType} representing the type of the export rule;
     *             determines the specific behavior or format applied during the export process.
     */
    public void setType(ExportRuleType type) {
        this.type = type;
    }

    /**
     * Retrieves the encounter field associated with the export rule.
     *
     * @return an instance of {@code ExportEncounterFieldType} representing the encounter field,
     *         or {@code null} if no encounter field is set.
     */
    public ExportEncounterFieldType getEncounterField() {
        return encounterField;
    }

    /**
     * Sets the encounter field used in the export rule configuration.
     *
     * @param encounterField an instance of {@code ExportEncounterFieldType} representing
     *                       the encounter field to be associated with the export rule;
     *                       defines how encounter-related data is managed during the export process.
     */
    public void setEncounterField(ExportEncounterFieldType encounterField) {
        this.encounterField = encounterField;
    }

    /**
     * Retrieves the date format associated with the export rule.
     *
     * @return an instance of {@code ExportDateFormatType} representing the date format,
     *         or {@code null} if no date format is set.
     */
    public ExportDateFormatType getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the date format associated with the export rule.
     *
     * @param dateFormat an instance of {@code ExportDateFormatType} representing the date format
     *                   to be assigned to the export rule; defines how date values are represented
     *                   in the export process. Must not be null.
     */
    public void setDateFormat(ExportDateFormatType dateFormat) {
        this.dateFormat = dateFormat;
    }
}
