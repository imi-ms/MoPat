package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireVersionGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class QuestionnaireVersionGroupService {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(QuestionnaireVersionGroupService.class);

    @Autowired
    private QuestionnaireVersionGroupDao questionnaireVersionGroupDao;
    
    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper;

    /**
     * Creates a new questionnaire version group where the name of the group is always
     * the same as the name of the questionnaire for which the group is being created.
     *
     * @param questionnaire the questionnaire for which a new group will be created
     * @return the newly created QuestionnaireVersionGroup
     */
    public QuestionnaireVersionGroup getOrCreateQuestionnaireGroup(Questionnaire questionnaire) {
        if (questionnaire.getQuestionnaireVersionGroupId() != null){
            return questionnaire.getQuestionnaireVersionGroup();
        }
        QuestionnaireVersionGroup questionnaireVersionGroup = new QuestionnaireVersionGroup();
        questionnaireVersionGroup.setName(questionnaire.getName());
        questionnaireVersionGroup.addQuestionnaire(questionnaire);
        questionnaireVersionGroup.setMainQuestionnaire(questionnaire);
        questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
        return questionnaireVersionGroup;
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

    /**
     * Finds the questionnaire version group to which the specified questionnaire belongs.
     *
     * @param questionnaire the questionnaire to search for within the groups
     * @return an Optional containing the group if found, or empty if not found
     */
    public Optional<QuestionnaireVersionGroup> findGroupForQuestionnaire(Questionnaire questionnaire) {
        validateQuestionnaires(questionnaire);
        return questionnaireVersionGroupDao.getAllElements().stream()
                .filter(group -> group.getQuestionnaires().stream()
                        .anyMatch(member -> member.equals(questionnaire)))
                .findFirst();
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
     * Adds a new questionnaire to the specified questionnaire version group and updates the group.
     *
     * @param questionnaireVersionGroup the group to which the questionnaire will be added
     * @param newQuestionnaire the new questionnaire to add to the group
     */
    public void addQuestionnaireToGroup(QuestionnaireVersionGroup questionnaireVersionGroup, Questionnaire newQuestionnaire) {
        questionnaireVersionGroup.addQuestionnaire(newQuestionnaire);
        newQuestionnaire.setQuestionnaireVersionGroup(questionnaireVersionGroup);
        questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
    }

    /**
     * Removes the specified questionnaire from the questionnaire version group identified by the given ID.
     * If the questionnaire is the last one in the group, the group is removed. If the group is not empty
     * after removal, it is updated in the database.
     *
     * @param questionnaireVersionGroupId the ID of the group from which the questionnaire will be removed
     * @param questionnaire the questionnaire to be removed from the group
     */
    public void removeQuestionnaire(Long questionnaireVersionGroupId, Questionnaire questionnaire) {
        // Fetch the group by ID, return early if not found
        Optional<QuestionnaireVersionGroup> optionalGroup = getQuestionnaireGroupById(questionnaireVersionGroupId);
        if (optionalGroup.isEmpty()){
            return;
        }

        QuestionnaireVersionGroup questionnaireVersionGroup = optionalGroup.get();

        if (questionnaireVersionGroup.isMainQuestionnaire(questionnaire)) {
            questionnaireVersionGroup.setMainQuestionnaire(
                    questionnaireVersionGroup.determineNewMainQuestionnaire().orElse(null)
            );
        }

        // Remove the questionnaire from the group if it exists
        Set<Questionnaire> questionnairesInGroup = questionnaireVersionGroup.getQuestionnaires();
        if (!questionnairesInGroup.remove(questionnaire)) {
            return;
        }

        // Unlink the questionnaire from the group
        questionnaire.setQuestionnaireVersionGroup(null);
        questionnaireDao.merge(questionnaire);

        if (questionnairesInGroup.isEmpty()){
            //If there would be no questionnaire left, delete the group
            questionnaireVersionGroupDao.remove(questionnaireVersionGroup);
        } else {
            questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
        }
    }

    public void setMainVersionForGroup(Questionnaire questionnaire) {
        Optional<QuestionnaireVersionGroup> groupForQuestionnaire = findGroupForQuestionnaire(questionnaire);
        if (groupForQuestionnaire.isPresent()) {
            QuestionnaireVersionGroup questionnaireVersionGroup = groupForQuestionnaire.get();
            questionnaireVersionGroup.setMainQuestionnaire(questionnaire);
            questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
        }
    }
}