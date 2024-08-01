package de.imi.mopat.model;

import java.io.Serializable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "questionnaire_group_member")
public class QuestionnaireGroupMember implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id", nullable = false)
    private QuestionnaireGroup questionnaireGroup;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuestionnaireGroup getQuestionnaireGroup() { return questionnaireGroup; }
    public void setQuestionnaireGroup(QuestionnaireGroup questionnaireGroup) { this.questionnaireGroup = questionnaireGroup; }

    public Questionnaire getQuestionnaire() { return questionnaire; }
    public void setQuestionnaire(Questionnaire questionnaire) { this.questionnaire = questionnaire; }

    @Override
    public String toString() {
        return "QuestionnaireGroupMember{" +
                "id=" + id +
                ", questionnaireGroupId=" + questionnaireGroup.getId() +
                ", questionnaireId=" + questionnaire.getId() +
                '}';
    }
}