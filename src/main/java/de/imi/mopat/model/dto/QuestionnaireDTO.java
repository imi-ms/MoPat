package de.imi.mopat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.ExportTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class QuestionnaireDTO {

    private Long id;
    // @Size and @NotNull have to be combined to realize @NotEmpty
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{questionnaire.name.notNull}")
    @Size(min = 3, max = 255, message = "{questionnaire.name.size}")
    private String name = "";
    // @Size and @NotNull have to be combined to realize @NotEmpty
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{questionnaire.description.notNull}")
    @Size(min = 1, message = "{questionnaire.description.notNull}")
    @JsonIgnore
    private String description = "";
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{questionnaire.welcomeText.notNull}")
    private SortedMap<String, String> localizedWelcomeText = null;
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{questionnaire.finalText.notNull}")
    private SortedMap<String, String> localizedFinalText = null;

    @NotNull(message = "{questionnaire.displayName.notNull}")
    private SortedMap<String, String> localizedDisplayName = null;

    private String logo;

    private String logoBase64;
    private Boolean deleteLogo = false;
    private Boolean hasConditionsAsTarget;
    private Boolean hasScores;

    private int version;

    @JsonIgnore
    private Set<ExportTemplate> exportTemplates = new HashSet<>();

    private List<QuestionDTO> questionDTOs;

    @JsonIgnore
    private QuestionnaireVersionGroupDTO questionnaireVersionGroupDTO;

    public List<QuestionDTO> getQuestionDTOs() {
        return questionDTOs;
    }

    public void setQuestionDTOs(final List<QuestionDTO> questionDTOs) {
        this.questionDTOs = questionDTOs;
    }

    public Set<ExportTemplate> getExportTemplates() {
        return exportTemplates;
    }

    public void setExportTemplates(final Set<ExportTemplate> exportTemplates) {
        this.exportTemplates = exportTemplates;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public boolean isDeleteLogo() {
        return deleteLogo;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(final String logo) {
        this.logo = logo;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(final String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public void setDeleteLogo(Boolean deleteLogo) {
        this.deleteLogo = deleteLogo;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public SortedMap<String, String> getLocalizedWelcomeText() {
        return localizedWelcomeText;
    }

    public void setLocalizedWelcomeText(final SortedMap<String, String> localizedWelcomeText) {
        this.localizedWelcomeText = localizedWelcomeText;
    }

    public SortedMap<String, String> getLocalizedFinalText() {
        return localizedFinalText;
    }

    public void setLocalizedFinalText(final SortedMap<String, String> localizedFinalText) {
        this.localizedFinalText = localizedFinalText;
    }

    public SortedMap<String, String> getLocalizedDisplayName() {
        return localizedDisplayName;
    }

    public void setLocalizedDisplayName(final SortedMap<String, String> localizedDisplayName) {
        this.localizedDisplayName = localizedDisplayName;
    }

    public void setHasConditionsAsTarget(final Boolean hasConditionsAsTarget) {
        this.hasConditionsAsTarget = hasConditionsAsTarget;
    }

    public boolean getHasConditionsAsTarget() {
        return hasConditionsAsTarget;
    }

    public Boolean getHasScores() {
        return hasScores;
    }

    public void setHasScores(final Boolean hasScores) {
        this.hasScores = hasScores;
    }

    @Override
    public boolean equals(final Object obj) {
        QuestionnaireDTO questionnaireDTO = (QuestionnaireDTO) obj;

        return Objects.equals(this.getId(), questionnaireDTO.getId());
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public Long getQuestionnaireVersionGroupId() {
        return (questionnaireVersionGroupDTO != null) ? questionnaireVersionGroupDTO.getGroupId() : null;
    }

    public String getQuestionnaireVersionGroupName() {
        return  (questionnaireVersionGroupDTO != null) ? questionnaireVersionGroupDTO.getGroupName() : null;
    }

    public QuestionnaireVersionGroupDTO getQuestionnaireGroupDTO() {
        return questionnaireVersionGroupDTO;
    }

    public void setQuestionnaireGroupDTO(QuestionnaireVersionGroupDTO questionnaireVersionGroupDTO) {
        this.questionnaireVersionGroupDTO = questionnaireVersionGroupDTO;
    }
}
