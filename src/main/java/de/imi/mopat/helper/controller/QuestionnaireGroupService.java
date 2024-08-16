package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionnaireGroupService {

    private final QuestionnaireGroupDao questionnaireGroupDao;

    private final QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper;

    @Autowired
    public QuestionnaireGroupService(QuestionnaireGroupDao questionnaireGroupDao, QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper) {
        this.questionnaireGroupDao = questionnaireGroupDao;
        this.questionnaireGroupDTOMapper = questionnaireGroupDTOMapper;
    }

    public QuestionnaireGroup createQuestionnaireGroup(String name) {
        QuestionnaireGroup questionnaireGroup = new QuestionnaireGroup();
        questionnaireGroup.setName(name);
        questionnaireGroupDao.merge(questionnaireGroup);
        return questionnaireGroup;
    }

    public QuestionnaireGroup createOrFindQuestionnaireGroup(QuestionnaireDTO questionnaireDTO) {
        if (questionnaireDTO.getGroupId() != null) {
            return getQuestionnaireGroupById(questionnaireDTO.getGroupId()).orElseGet(() -> {
                QuestionnaireGroup newGroup = new QuestionnaireGroup();
                newGroup.setName(questionnaireDTO.getName());
                questionnaireGroupDao.merge(newGroup);
                return newGroup;
            });
        } else {
            QuestionnaireGroup newGroup = new QuestionnaireGroup();
            newGroup.setName(questionnaireDTO.getName());
            questionnaireGroupDao.merge(newGroup);
            return newGroup;
        }
    }

    /**
     * Retrieves the group by its ID.
     *
     * @param groupId the ID of the group to retrieve
     * @return an Optional containing the group if it exists, or an empty Optional
     */
    public Optional<QuestionnaireGroup> getQuestionnaireGroupById(Long groupId) {
        return questionnaireGroupDao.getAllElements().stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst();
    }

    public Optional<QuestionnaireGroup> findGroupForQuestionnaire(Questionnaire questionnaire) {
        validateQuestionnaires(questionnaire);
        return questionnaireGroupDao.getAllElements().stream()
                .filter(group -> group.getQuestionnaires().stream()
                        .anyMatch(member -> member.equals(questionnaire)))
                .findFirst();
    }

    public int findMaxVersionInGroup(QuestionnaireGroup questionnaireGroup) {
        return questionnaireGroup.getQuestionnaires().stream()
                .map(Questionnaire::getVersion)
                .max(Integer::compareTo)
                .orElse(1);
    }

    public Set<Long> getAllGroupIds() {
        return questionnaireGroupDao.getAllElements().stream()
                .map(QuestionnaireGroup::getId)
                .collect(Collectors.toSet());
    }

    public List<QuestionnaireGroup> getQuestionnaireGroups(Set<Long> groupIds) {
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
        return questionnaireGroupDao.getAllElements().stream()
                .map(QuestionnaireGroup::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves all QuestionnaireGroupDTOs.
     *
     * @return a list of all QuestionnaireGroupDTOs
     */
    public List<QuestionnaireGroupDTO> getAllQuestionnaireGroupDTOs() {
        return questionnaireGroupDao.getAllElements().stream()
                .map(questionnaireGroupDTOMapper)
                .sorted(Comparator.comparing(QuestionnaireGroupDTO::getGroupName))
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

    public List<QuestionnaireGroup> getAllQuestionnaireGroups() {
        return questionnaireGroupDao.getAllElements();
    }
}