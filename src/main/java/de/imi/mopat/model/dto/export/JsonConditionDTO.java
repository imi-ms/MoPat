package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionActionType;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.conditions.ThresholdComparisonType;

/**
 * This class represents the data transfer obejct of model {@link Condition} to convert a model to
 * json for import and export.
 */
@JsonInclude(Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("condition")
public class JsonConditionDTO {

    private Long id;
    private String action;
    private String targetClass;
    private Long targetId;
    private Long triggerId;
    private Long targetAnswerQuestionId;
    private ThresholdComparisonType thresholdType;
    private Double thresholdValue;

    public JsonConditionDTO() {
    }

    public JsonConditionDTO(Condition condition) {
        this.setId(condition.getId());
        this.setAction(condition.getAction().name());
        this.setTargetClass(condition.getTargetClass());
        this.setTargetId(condition.getTarget().getId());
        this.setTriggerId(condition.getTrigger().getId());
        if (condition.getTargetAnswerQuestion() != null) {
            this.setTargetAnswerQuestionId(condition.getTargetAnswerQuestion().getId());
        }

        if (condition instanceof SliderAnswerThresholdCondition) {
            SliderAnswerThresholdCondition sliderAnswerCondition = (SliderAnswerThresholdCondition) condition;
            this.setThresholdType(sliderAnswerCondition.getThresholdComparisonType());
            this.setThresholdValue(sliderAnswerCondition.getThreshold());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(final String targetClass) {
        this.targetClass = targetClass;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(final Long targetId) {
        this.targetId = targetId;
    }

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(final Long triggerId) {
        this.triggerId = triggerId;
    }

    public Long getTargetAnswerQuestionId() {
        return targetAnswerQuestionId;
    }

    public void setTargetAnswerQuestionId(final Long targetAnswerQuestionId) {
        this.targetAnswerQuestionId = targetAnswerQuestionId;
    }

    public ThresholdComparisonType getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(final ThresholdComparisonType thresholdType) {
        this.thresholdType = thresholdType;
    }

    public Double getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(final Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    /**
     * Convert instance of this class to {@link Condition} object.
     *
     * @return Object of model {@link Condition}
     */
    public Condition convertToCondition() {
        Condition condition = null;
        if (this.getThresholdValue() != null) {
            SliderAnswerThresholdCondition sliderAnswerCondition = new SliderAnswerThresholdCondition();
            sliderAnswerCondition.setThreshold(this.getThresholdValue());
            sliderAnswerCondition.setThresholdComparisonType(this.getThresholdType());
            condition = sliderAnswerCondition;
        } else {
            condition = new SelectAnswerCondition();
        }
        condition.setAction(ConditionActionType.valueOf(this.getAction()));
        return condition;
    }
}
