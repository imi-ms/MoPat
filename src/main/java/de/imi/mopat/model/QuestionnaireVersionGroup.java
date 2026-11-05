package de.imi.mopat.model;

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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@Entity
@Table(name = "questionnaire_version_group")
public class QuestionnaireVersionGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "main_questionnaire_id")
    private Long mainQuestionnaireId;

    @OneToMany(mappedBy = "questionnaireVersionGroup", fetch = FetchType.LAZY)
    private Set<Questionnaire> questionnaires = new HashSet<>();

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

    public void setMainQuestionnaire(Questionnaire questionnaire) {
        if (questionnaire == null){
            this.mainQuestionnaireId = null;
            return;
        }
        if (!questionnaires.contains(questionnaire)) {
            throw new IllegalArgumentException("The questionnaire does not belong to this group.");
        }
        this.mainQuestionnaireId = questionnaire.getId();
    }

    public Optional<Questionnaire> getMainQuestionnaire() {
        if (questionnaires.isEmpty()){
            return Optional.empty();
        }

        // Search for the questionnaire marked as "main"
        return questionnaires.stream()
                .filter(q -> q.getId().equals(mainQuestionnaireId))
                .findFirst();
    }

    public Optional<Questionnaire> determineNewMainQuestionnaire() {
        return questionnaires.stream()
                .filter(Questionnaire::isApproved) // preferred approved questionnaires
                .max(Comparator.comparingInt(Questionnaire::getVersion)); // choose the highest version
    }

    public int getHighestVersionInGroup() {
        int defaultVersionNumber = 1;
        return questionnaires.stream()
                .map(Questionnaire::getVersion)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(defaultVersionNumber);
    }

    public boolean isMainQuestionnaire(Questionnaire questionnaire) {
        if (questionnaire == null || questionnaire.getId() == null) {
            return false;
        }
        return Objects.equals(this.mainQuestionnaireId, questionnaire.getId());
    }
}