package de.imi.mopat.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "questionnaire_group")
public class QuestionnaireGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "questionnaireGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Questionnaire> questionnaires = new HashSet<Questionnaire>();

    public Long getId() {
        return id;
    }

    public Set<Questionnaire> getQuestionnaires() {
        return questionnaires;
    }
    public void setQuestionnaires(Set<Questionnaire> questionnaires) {
        this.questionnaires = questionnaires;
    }

    public boolean hasQuestionnaires() {
        return !questionnaires.isEmpty();
    }

    public void addQuestionnaire(Questionnaire questionnaire) {
        questionnaires.add(questionnaire);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void sortQuestionnairesByVersion() {
        List<Questionnaire> sortedQuestionnaires = new ArrayList<>(questionnaires);
        sortedQuestionnaires.sort(Comparator.comparingInt(Questionnaire::getVersion));
        this.questionnaires = new LinkedHashSet<>(sortedQuestionnaires);
    }

    public void addQuestionnaires(List<Questionnaire> questionnaires) {
        this.questionnaires.addAll(questionnaires);
    }
}


