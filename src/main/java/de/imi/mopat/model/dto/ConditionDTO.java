package de.imi.mopat.model.dto;

import de.imi.mopat.model.conditions.ThresholdComparisonType;

/**
 *
 */
public class ConditionDTO {

    private Long id;
    private String action;
    private String targetClass;
    private Long targetId;
    private Long triggerId;
    private Long bundleId;
    private Long targetAnswerQuestionId;
    private ThresholdComparisonType thresholdType;
    private Double thresholdValue;


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

    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(final Long bundleId) {
        this.bundleId = bundleId;
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
}