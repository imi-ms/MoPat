package de.imi.mopat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questionnaire_version")
public class QuestionnaireVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "current_questionnaire_id", nullable = false)
    private Questionnaire currentQuestionnaire;

    @ManyToOne
    @JoinColumn(name = "previous_questionnaire_id")
    private Questionnaire previousQuestionnaire;

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Questionnaire getCurrentQuestionnaire() {
        return currentQuestionnaire;
    }

    public void setCurrentQuestionnaire(Questionnaire currentQuestionnaire) {
        this.currentQuestionnaire = currentQuestionnaire;
    }

    public Questionnaire getPreviousQuestionnaire() {
        return previousQuestionnaire;
    }

    public void setPreviousQuestionnaire(Questionnaire previousQuestionnaire) {
        this.previousQuestionnaire = previousQuestionnaire;
    }
}

