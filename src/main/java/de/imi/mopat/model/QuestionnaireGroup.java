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
import java.util.HashSet;
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

    @OneToMany(mappedBy = "questionnaireGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<QuestionnaireGroupMember> questionnaireGroupMembers = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Set<QuestionnaireGroupMember> getQuestionnaireGroupMembers() {
        return questionnaireGroupMembers;
    }
    public void setQuestionnaireGroupMembers(Set<QuestionnaireGroupMember> questionnaireGroupMembers) {
        this.questionnaireGroupMembers = questionnaireGroupMembers;
    }

    public boolean hasMembers() {
        return !questionnaireGroupMembers.isEmpty();
    }

    public void addMember(QuestionnaireGroupMember questionnaireGroupMember) {
        questionnaireGroupMembers.add(questionnaireGroupMember);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "QuestionnaireGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", questionnaireGroupMembers=" + questionnaireGroupMembers +
                '}';
    }
}


