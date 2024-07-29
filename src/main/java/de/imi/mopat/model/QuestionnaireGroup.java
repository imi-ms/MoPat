package de.imi.mopat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "questionnaire_group")
public class QuestionnaireGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionnaireGroup that = (QuestionnaireGroup) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(questionnaire, that.questionnaire);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, questionnaire);
    }

    @Override
    public String toString() {
        return "QuestionnaireGroup{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", questionnaire=" + questionnaire.getId() +
                '}';
    }
}