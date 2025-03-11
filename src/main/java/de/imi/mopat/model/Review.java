package de.imi.mopat.model;

import de.imi.mopat.model.enumeration.ReviewStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review")
public class Review implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @NotNull
    @OneToOne
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "editor_id", nullable = false)
    private Long editorId;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "updated_at")
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());


    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewMessage> conversation = new ArrayList<>();

    public Review() {
    }

    public Review(Questionnaire questionnaire, Long editorId, Long reviewerId) {
        this.questionnaire = questionnaire;
        this.editorId = editorId;
        this.reviewerId = reviewerId;
    }

    public Long getId() {
        return id;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
    }

    public Long getEditorId() {
        return editorId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public String getQuestionnaireName(){
        return getQuestionnaire().getName();
    }

    public List<ReviewMessage> getConversation() {
        return conversation;
    }

    public void setStatus(ReviewStatus reviewStatus) {
        this.status = reviewStatus;
    }

    public void setConversation(List<ReviewMessage> conversation) {
        this.conversation = conversation;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isUnfinished() {
        return status.isUnfinished();
    }

    public boolean isFinished() {
        return status.isFinished();
    }
}