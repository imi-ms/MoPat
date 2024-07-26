package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuestionnaireGroupService {

    private QuestionnaireGroupDao questionnaireGroupDao;

    @Autowired
    public void setQuestionnaireGroupDao(QuestionnaireGroupDao questionnaireGroupDao) {
        this.questionnaireGroupDao = questionnaireGroupDao;
    }

    /**
     * Ensures that group information is correctly assigned to a new questionnaire
     * based on an existing questionnaire.
     *
     * @param newQuestionnaire the new questionnaire to be grouped
     * @param existingQuestionnaire the existing questionnaire to base the grouping on
     */
    public void saveGroupInformation(Questionnaire newQuestionnaire, Questionnaire existingQuestionnaire) {
        if (newQuestionnaire == null || existingQuestionnaire == null) {
            throw new IllegalArgumentException("Questionnaires must not be null");
        }
        if (existingQuestionnaire.isOriginal()) {
            handleOriginalQuestionnaire(newQuestionnaire, existingQuestionnaire);
        } else {
            handleDuplicateQuestionnaire(newQuestionnaire, existingQuestionnaire);
        }
    }

    /**
     * Handles grouping when the existing questionnaire is an original.
     *
     * @param newQuestionnaire the new questionnaire to be grouped
     * @param originalQuestionnaire the original questionnaire
     */
    private void handleOriginalQuestionnaire(Questionnaire newQuestionnaire, Questionnaire originalQuestionnaire) {
        Optional<Long> groupIdForQuestionnaire = getGroupIdForQuestionnaire(originalQuestionnaire);
        if (groupIdForQuestionnaire.isEmpty()) {
            Long newGroupId = questionnaireGroupDao.getNextGroupId();
            saveQuestionnaireGroup(newGroupId, originalQuestionnaire);
            saveQuestionnaireGroup(newGroupId, newQuestionnaire);
        }else{
            Long existingGroupId = groupIdForQuestionnaire.get();
            saveQuestionnaireGroup(existingGroupId, newQuestionnaire);
        }
    }

    /**
     * Handles grouping when the existing questionnaire is a duplicate.
     *
     * @param newQuestionnaire the new questionnaire to be grouped
     * @param existingQuestionnaire the existing duplicate questionnaire
     */
    private void handleDuplicateQuestionnaire(Questionnaire newQuestionnaire, Questionnaire existingQuestionnaire) {
        Optional<Long> groupIdOpt = getGroupIdForQuestionnaire(existingQuestionnaire);
        Long groupId = groupIdOpt.orElseGet(questionnaireGroupDao::getNextGroupId);
        if (groupIdOpt.isEmpty()) {
            saveQuestionnaireGroup(groupId, existingQuestionnaire);
        }
        saveQuestionnaireGroup(groupId, newQuestionnaire);
    }

    /**
     * Checks if a questionnaire is already part of a group.
     *
     * @param questionnaire the questionnaire to check
     * @return true if the questionnaire is in a group, false otherwise
     */
    public boolean isQuestionnaireInGroup(Questionnaire questionnaire) {
        if (questionnaire == null) {
            throw new IllegalArgumentException("Questionnaires must not be null");
        }
        return questionnaireGroupDao.getAllElements().stream()
                .anyMatch(group -> group.getQuestionnaire().equals(questionnaire));
    }

    /**
     * Saves the group information for a questionnaire.
     *
     * @param groupId the group ID to assign
     * @param questionnaire the questionnaire to be grouped
     */
    private void saveQuestionnaireGroup(Long groupId, Questionnaire questionnaire) {
        QuestionnaireGroup questionnaireGroup = new QuestionnaireGroup();
        questionnaireGroup.setGroupId(groupId);
        questionnaireGroup.setQuestionnaire(questionnaire);
        questionnaireGroupDao.merge(questionnaireGroup);
    }

    /**
     * Retrieves the group ID for a questionnaire if it exists.
     *
     * @param questionnaire the questionnaire to check
     * @return an Optional containing the group ID if it exists, or an empty Optional
     */
    public Optional<Long> getGroupIdForQuestionnaire(Questionnaire questionnaire) {
        return questionnaireGroupDao.getAllElements().stream()
                .filter(group -> group.getQuestionnaire().equals(questionnaire))
                .map(QuestionnaireGroup::getGroupId)
                .findFirst();
    }

    /**
     * Finds the maximum version number among the questionnaires in the specified group.
     *
     * @param groupId The ID of the group for which to find the maximum version number.
     * @return The maximum version number in the group. If the group is empty, returns 0.
     */
    public int findMaxVersionInGroup(Long groupId) {
        return questionnaireGroupDao.getAllElements().stream()
                .filter(group -> group.getGroupId().equals(groupId))
                .map(group -> group.getQuestionnaire().getVersion())
                .max(Integer::compare)
                .orElse(0);
    }
}

