package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.enumeration.CodedValueType;
import de.imi.mopat.model.enumeration.QuestionType;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class represents the data transfer obejct of model {@link Question} to convert a model to
 * json for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("question")
public class JsonQuestionDTO {

    private Long id = null;
    private Map<String, String> localizedQuestionText = new HashMap<>();
    private Boolean isRequired = null;
    private Boolean isEnabled = null;
    private QuestionType questionType = null;
    private Integer minNumberAnswers = null;
    private Integer maxNumberAnswers = null;
    private CodedValueType codedValueType = null;
    private SortedMap<Long, JsonAnswerDTO> answers = new TreeMap<>();
    private JsonQuestionnaireDTO jsonQuestionnaireDTO = null;
    private Integer position = null;

    public JsonQuestionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Map<String, String> getLocalizedQuestionText() {
        return localizedQuestionText;
    }

    public void setLocalizedQuestionText(final Map<String, String> localizedQuestionText) {
        this.localizedQuestionText = localizedQuestionText;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(final Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(final Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(final QuestionType questionType) {
        this.questionType = questionType;
    }

    public Integer getMinNumberAnswers() {
        return minNumberAnswers;
    }

    public void setMinNumberAnswers(final Integer minNumberAnswers) {
        this.minNumberAnswers = minNumberAnswers;
    }

    public Integer getMaxNumberAnswers() {
        return maxNumberAnswers;
    }

    public void setMaxNumberAnswers(final Integer maxNumberAnswers) {
        this.maxNumberAnswers = maxNumberAnswers;
    }

    public CodedValueType getCodedValueType() {
        return codedValueType;
    }

    public void setCodedValueType(CodedValueType codedValueType) {
        this.codedValueType = codedValueType;
    }

    public SortedMap<Long, JsonAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(SortedMap<Long, JsonAnswerDTO> answers) {
        this.answers = answers;
    }

    public JsonQuestionnaireDTO getJsonQuestionnaireDTO() {
        return jsonQuestionnaireDTO;
    }

    public void setJsonQuestionnaireDTO(JsonQuestionnaireDTO jsonQuestionnaireDTO) {
        this.jsonQuestionnaireDTO = jsonQuestionnaireDTO;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public void setAnswers(long id, JsonAnswerDTO jsonAnswerDTO){
        this.answers.put(
            id,
            jsonAnswerDTO);

    }
    public Question convertToQuestion() {
        Question question = new Question();
        question.setLocalizedQuestionText(this.getLocalizedQuestionText());
        question.setIsEnabled(this.getIsEnabled());
        question.setIsRequired(this.getIsRequired());
        question.setMinMaxNumberAnswers(this.getMinNumberAnswers(), this.getMaxNumberAnswers());
        question.setCodedValueType(this.getCodedValueType());
        question.setPosition(this.getPosition());
        question.setQuestionType(this.getQuestionType());

        return question;
    }
}
