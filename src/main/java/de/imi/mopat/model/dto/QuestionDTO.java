package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.CodedValueType;
import de.imi.mopat.model.enumeration.QuestionType;

import java.util.List;
import java.util.SortedMap;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 *
 */
public class QuestionDTO {

    private Long id = null;
    @NotNull(message = "{question.questionText.notNull}")
    private SortedMap<String, String> localizedQuestionText = null;
    @NotNull(message = "{question.isRequired.notNull}")
    private Boolean isRequired = null;
    @NotNull(message = "{question.enabled.notNull}")
    private Boolean isEnabled = null;
    @NotNull(message = "{question.questionType.notNull}")
    private QuestionType questionType = null;
    //@Pattern(regexp="\\d+", message = "{question.minNumberAnswers
    // .wrongPattern}")
    @Min(value = 0, message = "{question.minNumberAnswers.min}")
    private Integer minNumberAnswers = null;
    //@Pattern(regexp="\\d+", message = "{question.maxNumberAnswers
    // .wrongPattern}")
    @Min(value = 0, message = "{question.maxNumberAnswers.min}")
    private Integer maxNumberAnswers = null;
    private SortedMap<Long, AnswerDTO> answers = null;
    private Long questionnaireId = null;
    private Integer position;
    private boolean hasScores;
    private List<String> bodyPartImages;
    private String imageType = "FRONT";
    private CodedValueType codedValueType = null; // Type of the coded value

    public QuestionDTO() {
        this.isEnabled = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public SortedMap<String, String> getLocalizedQuestionText() {
        return localizedQuestionText;
    }

    public void setLocalizedQuestionText(final SortedMap<String, String> localizedQuestionText) {
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

    public SortedMap<Long, AnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(final SortedMap<Long, AnswerDTO> answers) {
        this.answers = answers;
    }

    public Long getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(final Long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public boolean isHasScores() {
        return hasScores;
    }

    public void setHasScores(final boolean hasScores) {
        this.hasScores = hasScores;
    }

    public List<String> getBodyPartImages() {
        return bodyPartImages;
    }

    public void setBodyPartImages(final List<String> bodyPartImages) {
        this.bodyPartImages = bodyPartImages;
    }

    public void setImageType(final String imageType) {
        this.imageType = imageType;
    }

    public CodedValueType getCodedValueType() {
        return codedValueType;
    }

    public void setCodedValueType(final CodedValueType codedValueType) {
        this.codedValueType = codedValueType;
    }

    /**
     * Returns the type of the images used in this question implied the current object is of type
     * {@link QuestionType#BODY_PART}.
     *
     * @return Type of image used in this question as String.
     */
    public String getImageType() {
        return imageType;
    }

    public Boolean isModifiable() {
        if (answers == null) {
            return Boolean.TRUE;
        }
        for (AnswerDTO answerDTO : this.answers.values()) {
            if (answerDTO.getHasResponse()) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean hasExportRules() {
        for (AnswerDTO answerDTO : this.answers.values()) {
            if (answerDTO.getHasExportRule()) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Boolean hasConditionsAsTrigger() {
        for (AnswerDTO answerDTO : this.answers.values()) {
            if (answerDTO.getHasConditionsAsTrigger()) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
