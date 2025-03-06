package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.ReviewStatus;

import java.sql.Timestamp;
import java.util.List;

public class ReviewDTO {


    private Long id;
    private QuestionnaireDTO questionnaire;
    private ReviewStatus status;
    private Long editorId;
    private String editorName;
    private String editorInitials;
    private Long reviewerId;
    private String reviewerInitials;
    private String reviewerName;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<ReviewMessageDTO> conversation;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionnaireDTO getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(QuestionnaireDTO questionnaire) {
        this.questionnaire = questionnaire;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Long getEditorId() {
        return editorId;
    }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getEditorInitials() {
        return editorInitials;
    }

    public void setEditorInitials(String editorInitials) {
        this.editorInitials = editorInitials;
    }

    public String getReviewerInitials() {
        return reviewerInitials;
    }

    public void setReviewerInitials(String reviewerInitials) {
        this.reviewerInitials = reviewerInitials;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getQuestionnaireName(){
        return getQuestionnaire().getName();
    }

    public Long getQuestionnaireId(){
        QuestionnaireDTO questionnaireDTO = getQuestionnaire();
        if (questionnaireDTO != null){
            return questionnaireDTO.getId();
        }
        return null;
    }

    public void setConversation(List<ReviewMessageDTO> conversation) {
        this.conversation = conversation;
    }

    public List<ReviewMessageDTO> getConversation() {
        return conversation;
    }

    public boolean isUnfinished() {
        return status.isUnfinished();
    }

    public boolean isFinished() {
        return status.isFinished();
    }
}