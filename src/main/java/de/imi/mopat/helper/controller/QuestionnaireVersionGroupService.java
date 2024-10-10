package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireVersionGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireVersionGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionnaireVersionGroupService {

    private final QuestionnaireVersionGroupDao questionnaireVersionGroupDao;

    private final QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper;

    @Autowired
    public QuestionnaireVersionGroupService(QuestionnaireVersionGroupDao questionnaireVersionGroupDao, QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper) {
        this.questionnaireVersionGroupDao = questionnaireVersionGroupDao;
        this.questionnaireGroupDTOMapper = questionnaireGroupDTOMapper;
    }

    public QuestionnaireVersionGroup createQuestionnaireGroup(String name) {
        QuestionnaireVersionGroup questionnaireVersionGroup = new QuestionnaireVersionGroup();
        questionnaireVersionGroup.setName(name);
        questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
        return questionnaireVersionGroup;
    }

    public QuestionnaireVersionGroup createOrFindQuestionnaireGroup(QuestionnaireDTO questionnaireDTO) {
        if (questionnaireDTO.getQuestionnaireVersionGroupId() != null) {
            return getQuestionnaireGroupById(questionnaireDTO.getQuestionnaireVersionGroupId()).orElseGet(() -> {
                QuestionnaireVersionGroup newGroup = new QuestionnaireVersionGroup();
                newGroup.setName(questionnaireDTO.getName());
                questionnaireVersionGroupDao.merge(newGroup);
                return newGroup;
            });
        } else {
            QuestionnaireVersionGroup newGroup = new QuestionnaireVersionGroup();
            newGroup.setName(questionnaireDTO.getName());
            questionnaireVersionGroupDao.merge(newGroup);
            return newGroup;
        }
    }

    /**
     * Retrieves the group by its ID.
     *
     * @param groupId the ID of the group to retrieve
     * @return an Optional containing the group if it exists, or an empty Optional
     */
    public Optional<QuestionnaireVersionGroup> getQuestionnaireGroupById(Long groupId) {
        return questionnaireVersionGroupDao.getAllElements().stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst();
    }

    public Optional<QuestionnaireVersionGroup> findGroupForQuestionnaire(Questionnaire questionnaire) {
        validateQuestionnaires(questionnaire);
        return questionnaireVersionGroupDao.getAllElements().stream()
                .filter(group -> group.getQuestionnaires().stream()
                        .anyMatch(member -> member.equals(questionnaire)))
                .findFirst();
    }

    public int findMaxVersionInGroup(QuestionnaireVersionGroup questionnaireVersionGroup) {
        return questionnaireVersionGroup.getQuestionnaires().stream()
                .map(Questionnaire::getVersion)
                .max(Integer::compareTo)
                .orElse(1);
    }

    public Set<Long> getAllGroupIds() {
        return questionnaireVersionGroupDao.getAllElements().stream()
                .map(QuestionnaireVersionGroup::getId)
                .collect(Collectors.toSet());
    }

    public List<QuestionnaireVersionGroup> getQuestionnaireGroups(Set<Long> groupIds) {
        return groupIds.stream()
                .map(this::getQuestionnaireGroupById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all unique group IDs.
     *
     * @return a set of all unique group IDs
     */
    public Set<Long> getAllUniqueGroupIds() {
        return questionnaireVersionGroupDao.getAllElements().stream()
                .map(QuestionnaireVersionGroup::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves all QuestionnaireGroupDTOs.
     *
     * @return a list of all QuestionnaireGroupDTOs
     */
    public List<QuestionnaireVersionGroupDTO> getAllQuestionnaireGroupDTOs() {
        return questionnaireVersionGroupDao.getAllElements().stream()
                .map(questionnaireGroupDTOMapper)
                .sorted(Comparator.comparing(QuestionnaireVersionGroupDTO::getGroupName))
                .toList();
    }

    /**
     * Validates that the provided questionnaires are not null.
     *
     * @param questionnaires the questionnaires to validate
     */
    private void validateQuestionnaires(Questionnaire... questionnaires) {
        for (Questionnaire questionnaire : questionnaires) {
            if (questionnaire == null) {
                throw new IllegalArgumentException("Questionnaires must not be null");
            }
        }
    }

    public List<QuestionnaireVersionGroup> getAllQuestionnaireGroups() {
        return questionnaireVersionGroupDao.getAllElements();
    }
}