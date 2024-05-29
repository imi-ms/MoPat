package de.imi.mopat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.enumeration.ExportTemplateType;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class BundleQuestionnaireDTO {

    private QuestionnaireDTO questionnaireDTO;
    private Long position = null;
    @JsonIgnore
    private Set<ExportTemplateType> exportTemplateTypes = new HashSet<>();
    @JsonIgnore
    private Set<Long> exportTemplates = new HashSet<>();
    private Boolean isEnabled;
    private Boolean showScores;
    private Long bundleId;

    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(final Long bundleId) {
        this.bundleId = bundleId;
    }

    public QuestionnaireDTO getQuestionnaireDTO() {
        return questionnaireDTO;
    }

    public void setQuestionnaireDTO(final QuestionnaireDTO questionnaire) {
        this.questionnaireDTO = questionnaire;
    }

    /**
     * @return the exportTemplateTypes
     */
    public Set<ExportTemplateType> getExportTemplateTypes() {
        return exportTemplateTypes;
    }

    /**
     * @param exportTemplateTypes the exportTemplateTypes to set
     */
    public void setExportTemplateTypes(final Set<ExportTemplateType> exportTemplateTypes) {
        this.exportTemplateTypes = exportTemplateTypes;
    }

    /**
     * @return the exportTemplates
     */
    public Set<Long> getExportTemplates() {
        return exportTemplates;
    }

    /**
     * @param exportTemplates the exportTemplates to set
     */
    public void setExportTemplates(final Set<Long> exportTemplates) {
        this.exportTemplates = exportTemplates;
    }

    /**
     * @return the position
     */
    public Long getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(final Long position) {
        this.position = position;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(final Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Boolean getShowScores() {
        return showScores;
    }

    public void setShowScores(final Boolean showScores) {
        this.showScores = showScores;
    }
}