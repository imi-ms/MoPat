package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireDao;
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
     * @param name the name of the questionnaire, which will also be the name of the group
     * @return the newly created QuestionnaireVersionGroup
     */
    public QuestionnaireVersionGroup createQuestionnaireGroup(String name) {
        QuestionnaireVersionGroup questionnaireVersionGroup = new QuestionnaireVersionGroup();
        questionnaireVersionGroup.setName(name);
        questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
        return questionnaireVersionGroup;
    }

    /**
     * Creates a new questionnaire group or finds an existing one based on the QuestionnaireDTO.
     * If the DTO contains a valid group ID, the corresponding group is retrieved. If not,
     * a new group is created with the name provided in the DTO.
     *
     * @param questionnaireDTO the DTO containing questionnaire group details
     * @return the existing or newly created QuestionnaireVersionGroup
     */
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

    public void add(QuestionnaireVersionGroup questionnaireVersionGroup) {
        questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
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

        // Remove the questionnaire from the group if it exists
        Set<Questionnaire> questionnairesInGroup = questionnaireVersionGroup.getQuestionnaires();
        if (!questionnairesInGroup.remove(questionnaire)) {
            return;
        }

        if (questionnairesInGroup.isEmpty()){
            //If there would be no questionnaire left, delete the group
            questionnaireVersionGroupDao.remove(questionnaireVersionGroup);
        } else {
            //Relationship between version group and questionnaire is controlled
            //by the questionnaire, so it has to be removed there
            questionnaire.setQuestionnaireVersionGroup(null);
            questionnaireDao.merge(questionnaire);
        }
    }
}