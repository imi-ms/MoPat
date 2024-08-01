package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.QuestionnaireGroupMember;
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

    /**
     * Ensures that group information is correctly assigned to a new questionnaire
     * based on an existing questionnaire.
     *
     * @param newQuestionnaire the new questionnaire to be grouped
     * @param existingQuestionnaire the existing questionnaire to base the grouping on
     */
    public void saveGroupInformation(Questionnaire newQuestionnaire, Questionnaire existingQuestionnaire) {
        validateQuestionnaires(newQuestionnaire, existingQuestionnaire);
        if (existingQuestionnaire.isOriginal()) {
            handleOriginalQuestionnaire(newQuestionnaire, existingQuestionnaire);
        } else {
            handleDuplicateQuestionnaire(newQuestionnaire, existingQuestionnaire);
        }
    }

    /**
     * Checks if a questionnaire is already part of a group.
     *
     * @param questionnaire the questionnaire to check
     * @return true if the questionnaire is in a group, false otherwise
     */
    public boolean isQuestionnaireInGroup(Questionnaire questionnaire) {
        validateQuestionnaires(questionnaire);
        List<QuestionnaireGroup> allElements = questionnaireGroupDao.getAllElements();
        return allElements.stream()
                .flatMap(group -> group.getQuestionnaireGroupMembers().stream())
                .anyMatch(member -> member.getQuestionnaire().equals(questionnaire));
    }

    /**
     * Retrieves the group for a specified questionnaire if it exists.
     *
     * @param questionnaire the questionnaire to check
     * @return an Optional containing the group if it exists, or an empty Optional
     */
    public Optional<QuestionnaireGroup> findGroupForQuestionnaire(Questionnaire questionnaire) {
        return questionnaireGroupDao.getAllElements().stream()
                .filter(group -> group.getQuestionnaireGroupMembers().stream()
                        .anyMatch(member -> member.getQuestionnaire().equals(questionnaire)))
                .findFirst();
    }

    /**
     * Finds the maximum version number among the questionnaires in the specified group.
     *
     * @param questionnaireGroup the group for which to find the maximum version number
     * @return the maximum version number in the group. If the group is empty, returns 1.
     */
    public int findMaxVersionInGroup(QuestionnaireGroup questionnaireGroup) {
        return questionnaireGroup.getQuestionnaireGroupMembers().stream()
                .map(QuestionnaireGroupMember::getQuestionnaire)
                .map(Questionnaire::getVersion)
                .max(Integer::compareTo)
                .orElse(1);
    }

    /**
     * Retrieves the group by its ID.
     *
     * @param groupId the ID of the group to retrieve
     * @return an Optional containing the group if it exists, or an empty Optional
     */
    public Optional<QuestionnaireGroup> getGroupById(Long groupId){
        return questionnaireGroupDao.getAllElements().stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst();
    }

    /**
     * Retrieves all QuestionnaireGroupMembers.
     *
     * @return a list of all QuestionnaireGroupMembers
     */
    private List<QuestionnaireGroupMember> getAllQuestionnaireGroupMembers() {
        return questionnaireGroupDao.getAllElements().stream()
                .flatMap(group -> group.getQuestionnaireGroupMembers().stream())
                .toList();
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
     * Handles grouping when the existing questionnaire is an original.
     *
     * @param newQuestionnaire the new questionnaire to be grouped
     * @param originalQuestionnaire the original questionnaire
     */
    private void handleOriginalQuestionnaire(Questionnaire newQuestionnaire, Questionnaire originalQuestionnaire) {
        Optional<QuestionnaireGroup> groupForQuestionnaire = findGroupForQuestionnaire(originalQuestionnaire);
        if (groupForQuestionnaire.isPresent()) {
            saveQuestionnaireToGroup(groupForQuestionnaire.get(), newQuestionnaire);
        }else{
            QuestionnaireGroup newQuestionnaireGroup = new QuestionnaireGroup();
            newQuestionnaireGroup.setName(originalQuestionnaire.getName());
            saveQuestionnaireToGroup(newQuestionnaireGroup, originalQuestionnaire);
            saveQuestionnaireToGroup(newQuestionnaireGroup, newQuestionnaire);
        }
    }

    /**
     * Handles grouping when the existing questionnaire is a duplicate.
     *
     * @param newQuestionnaire the new questionnaire to be grouped
     * @param existingQuestionnaire the existing duplicate questionnaire
     */
    private void handleDuplicateQuestionnaire(Questionnaire newQuestionnaire, Questionnaire existingQuestionnaire) {
        QuestionnaireGroup group = findGroupForQuestionnaire(existingQuestionnaire)
                .orElseGet(() -> {
                    QuestionnaireGroup newGroup = new QuestionnaireGroup();
                    newGroup.setName(existingQuestionnaire.getName());
                    return newGroup;
                });
        if (group.getQuestionnaireGroupMembers().isEmpty()) {
            saveQuestionnaireToGroup(group, existingQuestionnaire);
        }
        saveQuestionnaireToGroup(group, newQuestionnaire);
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

    /**
     * Saves a questionnaire to a specified group.
     *
     * @param questionnaireGroup the group to save the questionnaire to
     * @param questionnaire the questionnaire to be saved
     */
    private void saveQuestionnaireToGroup(QuestionnaireGroup questionnaireGroup, Questionnaire questionnaire) {
        QuestionnaireGroupMember newMember = createQuestionnaireGroupMember(questionnaireGroup, questionnaire);
        questionnaireGroup.addMember(newMember);
        questionnaireGroupDao.merge(questionnaireGroup);
    }

    /**
     * Creates a new QuestionnaireGroupMember.
     *
     * @param questionnaireGroup the group the member belongs to
     * @param questionnaire the questionnaire of the member
     * @return the created QuestionnaireGroupMember
     */
    private QuestionnaireGroupMember createQuestionnaireGroupMember(QuestionnaireGroup questionnaireGroup, Questionnaire questionnaire) {
        QuestionnaireGroupMember newMember = new QuestionnaireGroupMember();
        newMember.setQuestionnaireGroup(questionnaireGroup);
        newMember.setQuestionnaire(questionnaire);
        return newMember;
    }

    public List<QuestionnaireGroup> getAllQuestionnaireGroups() {
        return questionnaireGroupDao.getAllElements();
    }
}