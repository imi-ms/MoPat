package de.imi.mopat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class BundleDTO {

    private Long id = null;
    // @Size and @NotNull have to be combined to realize @NotEmpty
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{bundle.name.notNull}")
    @Size(min = 1, message = "{bundle.name.notNull}")
    private String name = null;
    // @Size and @NotNull have to be combined to realize @NotEmpty
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{bundle.description.notNull}")
    @Size(min = 1, message = "{bundle.description.notNull}")
    @JsonIgnore
    private String description = null;
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{questionnaire.welcomeText.notNull}")
    private SortedMap<String, String> localizedWelcomeText = null;
    // If the annotations are changed, they have to be changed in the
    // corresponding database entitiy class
    @NotNull(message = "{questionnaire.finalText.notNull}")
    private SortedMap<String, String> localizedFinalText = null;
    @JsonIgnore
    private Long changedBy = null;
    @JsonIgnore
    private Boolean isPublished = null;
    private Boolean showProgressPerBundle;
    private Boolean deactivateProgressAndNameDuringSurvey;
    @JsonIgnore
    private Boolean isModifiable = true;
    @JsonIgnore
    private List<String> availableLanguages = null;
    private List<BundleQuestionnaireDTO> bundleQuestionnaireDTOs = new ArrayList<>();

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return the changedBy
     */
    public Long getChangedBy() {
        return changedBy;
    }

    /**
     * @param changedBy the changedBy to set
     */
    public void setChangedBy(final Long changedBy) {
        this.changedBy = changedBy;
    }

    /**
     * @return the isPublished
     */
    public Boolean getIsPublished() {
        return isPublished;
    }

    /**
     * @param isPublished the isPublished to set
     */
    public void setIsPublished(final Boolean isPublished) {
        this.isPublished = isPublished;
    }

    /**
     * @return the deactivateProgressAndNameDuringSurvey
     */
    public Boolean getdeactivateProgressAndNameDuringSurvey() {
        return deactivateProgressAndNameDuringSurvey;
    }

    /**
     * @param deactivateProgressAndNameDuringSurvey the deactivateProgressAndNameDuringSurvey to
     *                                              set
     */
    public void setdeactivateProgressAndNameDuringSurvey(
        final Boolean deactivateProgressAndNameDuringSurvey) {
        this.deactivateProgressAndNameDuringSurvey = deactivateProgressAndNameDuringSurvey;
    }

    public void setAvailableLanguages(final List<String> availableLanguages) {
        this.availableLanguages = availableLanguages;
    }

    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }

    /**
     * @return the bundleQuestionnaires
     */
    public List<BundleQuestionnaireDTO> getBundleQuestionnaireDTOs() {
        return bundleQuestionnaireDTOs;
    }

    /**
     * @param bundleQuestionnaires the bundleQuestionnaires to set
     */
    public void setBundleQuestionnaireDTOs(
        final List<BundleQuestionnaireDTO> bundleQuestionnaires) {
        this.bundleQuestionnaireDTOs = bundleQuestionnaires;
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

    public Boolean getShowProgressPerBundle() {
        return showProgressPerBundle;
    }

    public void setShowProgressPerBundle(final boolean showProgressPerBundle) {
        this.showProgressPerBundle = showProgressPerBundle;
    }

    public Boolean getIsModifiable() {
        return this.isModifiable;
    }

    public void setIsModifiable(final Boolean isModifiable) {
        this.isModifiable = isModifiable;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        BundleDTO compareBundleDTO = null;
        if (object instanceof BundleDTO) {
            compareBundleDTO = (BundleDTO) object;
        }
        if (compareBundleDTO != null && compareBundleDTO.getId() != null && this.getId() != null) {
            return this.getId().equals(compareBundleDTO.getId());
        } else {
            return false;
        }
    }

    @JsonIgnore
    public String getJSON() {
        String value;
        try {
            value = new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            value = null;
        }
        return value;
    }
}
