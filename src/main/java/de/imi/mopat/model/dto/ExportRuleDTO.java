package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportEncounterFieldType;
import de.imi.mopat.model.enumeration.ExportScoreFieldType;

import java.util.List;

/**
 *
 */
public class ExportRuleDTO {

    private Long answerId = null;
    private Long questionId = null;
    private Long tempExportFormatId = null;
    private List<String> exportField;
    private ExportEncounterFieldType encounterField = null;
    private ExportScoreFieldType scoreField = null;
    private Long scoreId = null;
    private ExportDateFormatType encounterDateFormat = null;
    private Boolean useFreetextValue = Boolean.FALSE;

    /**
     * Returns an {@link de.imi.mopat.model.Answer} object id
     *
     * @return an {@link de.imi.mopat.model.Answer} object id
     */
    public Long getAnswerId() {
        return this.answerId;
    }

    /**
     * Sets the {@link de.imi.mopat.model.Answer} object id
     *
     * @param answerId {@link de.imi.mopat.model.Answer} object id
     */
    public void setAnswerId(final Long answerId) {
        this.answerId = answerId;
    }

    /**
     * Returns an {@link de.imi.mopat.model.Question} object id
     *
     * @return an {@link de.imi.mopat.model.Question} object id
     */
    public Long getQuestionId() {
        return questionId;
    }

    /**
     * Sets the {@link de.imi.mopat.model.Question} object id
     *
     * @param questionId {@link de.imi.mopat.model.Question} object id
     */
    public void setQuestionId(final Long questionId) {
        this.questionId = questionId;
    }

    /**
     * Return a list of export fields
     *
     * @return a list of export fields
     */
    public List<String> getExportField() {
        return this.exportField;
    }

    /**
     * Sets a list of export fields
     *
     * @param exportField list of export fields
     */
    public void setExportField(final List<String> exportField) {
        this.exportField = exportField;
    }

    /**
     * @return the encounterField
     */
    public ExportEncounterFieldType getEncounterField() {
        return encounterField;
    }

    /**
     * @param encounterField the encounterField to set
     */
    public void setEncounterField(final ExportEncounterFieldType encounterField) {
        this.encounterField = encounterField;
    }

    /**
     * @return the encounterDateFormat
     */
    public ExportDateFormatType getEncounterDateFormat() {
        return encounterDateFormat;
    }

    /**
     * @param encounterDateFormat the encounterDateFormat to set
     */
    public void setEncounterDateFormat(final ExportDateFormatType encounterDateFormat) {
        this.encounterDateFormat = encounterDateFormat;
    }

    /**
     * @return the tempExportFormatId
     */
    public Long getTempExportFormatId() {
        return tempExportFormatId;
    }

    /**
     * @param tempExportFormatId the tempExportFormatId to set
     */
    public void setTempExportFormatId(final Long tempExportFormatId) {
        this.tempExportFormatId = tempExportFormatId;
    }

    /**
     * @return the useFreetextValue
     */
    public Boolean getUseFreetextValue() {
        return useFreetextValue;
    }

    /**
     * @param useFreetextValue the useFreetextValue to set
     */
    public void setUseFreetextValue(final Boolean useFreetextValue) {
        this.useFreetextValue = useFreetextValue;
    }

    /**
     * @return the scoreField
     */
    public ExportScoreFieldType getScoreField() {
        return scoreField;
    }

    /**
     * @param scoreField the scoreField to set
     */
    public void setScoreField(final ExportScoreFieldType scoreField) {
        this.scoreField = scoreField;
    }

    public Long getScoreId() {
        return scoreId;
    }

    public void setScoreId(final Long scoreId) {
        this.scoreId = scoreId;
    }
}
