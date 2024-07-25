package de.imi.mopat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questionnaire_version")
public class QuestionnaireVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "original_questionnaire_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_original_questionnaire"))
    private Questionnaire originalQuestionnaire;

    @ManyToOne
    @JoinColumn(name = "duplicate_questionnaire_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_duplicate_questionnaire"))
    private Questionnaire duplicateQuestionnaire;

    @Column(name = "version_group_id", nullable = false)
    private Long versionGroupId;

    public Long getVersionGroupId() {
        return versionGroupId;
    }

    public void setVersionGroupId(Long versionGroupId) {
        this.versionGroupId = versionGroupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Questionnaire getOriginalQuestionnaire() {
        return originalQuestionnaire;
    }

    public void setOriginalQuestionnaire(Questionnaire originalQuestionnaire) {
        this.originalQuestionnaire = originalQuestionnaire;
    }

    public Questionnaire getDuplicateQuestionnaire() {
        return duplicateQuestionnaire;
    }

    public void setDuplicateQuestionnaire(Questionnaire duplicateQuestionnaire) {
        this.duplicateQuestionnaire = duplicateQuestionnaire;
    }

    public Long getOriginalQuestionnaireId() {
        return originalQuestionnaire.getId();
    }

    public Long getDuplicateQuestionnaireId() {
        return duplicateQuestionnaire.getId();
    }
}