package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.model.Questionnaire;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class represents the data transfer obejct of model {@link Questionnaire} to convert a model
 * to json for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("questionnaire")
@JsonSubTypes({
    @JsonSubTypes.Type(value = JsonCompleteQuestionnaireDTO.class, name = "questionnaireComplete")
})
public class JsonQuestionnaireDTO {

    private Long id;
    private String name = null;
    private String description = null;
    private Map<String, String> localizedWelcomeText = new HashMap<>();
    private Map<String, String> localizedFinalText = new HashMap<>();
    private Map<String, String> localizedDisplayName = new HashMap<>();
    private SortedMap<Long, JsonQuestionDTO> questionDTOs = new TreeMap<>();
    private Map<Long, JsonScoreDTO> scoreDTOs = new HashMap<>();
    private String logoBase64 = null;

    public JsonQuestionnaireDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public Map<String, String> getLocalizedWelcomeText() {
        return localizedWelcomeText;
    }

    public void setLocalizedWelcomeText(final Map<String, String> localizedWelcomeText) {
        this.localizedWelcomeText = localizedWelcomeText;
    }

    public Map<String, String> getLocalizedFinalText() {
        return localizedFinalText;
    }

    public void setLocalizedFinalText(final Map<String, String> localizedFinalText) {
        this.localizedFinalText = localizedFinalText;
    }

    public Map<String, String> getLocalizedDisplayName() {
        return localizedDisplayName;
    }

    public void setLocalizedDisplayName(final Map<String, String> localizedDisplayName) {
        this.localizedDisplayName = localizedDisplayName;
    }

    public SortedMap<Long, JsonQuestionDTO> getQuestionDTOs() {
        return questionDTOs;
    }

    public void setQuestionDTOs(final SortedMap<Long, JsonQuestionDTO> questionDTOs) {
        this.questionDTOs = questionDTOs;
    }

    public Map<Long, JsonScoreDTO> getScoreDTOs() {
        return scoreDTOs;
    }

    public void setScoreDTOs(final Map<Long, JsonScoreDTO> scoreDTOs) {
        this.scoreDTOs = scoreDTOs;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(final String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public void setQuestionDTO(long id, JsonQuestionDTO jsonQuestionDTO){
        this.questionDTOs.put(
            id,
            jsonQuestionDTO);
    }
    public void setScoreDTO(long id, JsonScoreDTO jsonScoreDTO){
        this.scoreDTOs.put(
            id,
            jsonScoreDTO);
    }
    public Questionnaire convertToQuestionnaire() {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setDescription(this.getDescription());
        questionnaire.setLocalizedDisplayName(this.getLocalizedDisplayName());
        questionnaire.setLocalizedFinalText(this.getLocalizedFinalText());
        questionnaire.setLocalizedWelcomeText(this.getLocalizedWelcomeText());
        questionnaire.setName(this.getName());
        questionnaire.setPublished(false);
        questionnaire.setHasConditions(false);
        questionnaire.setUpdatedAt(new Timestamp(new Date().getTime()));
        return questionnaire;
    }
}
