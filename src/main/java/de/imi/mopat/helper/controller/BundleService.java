package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.QuestionnaireGroupMember;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.imi.mopat.model.dto.QuestionnaireGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BundleService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(BundleService.class);

    @Autowired
    private QuestionnaireDTOMapper questionnaireDTOMapper;

    @Autowired
    private BundleQuestionnaireDao bundleQuestionnaireDao;

    @Autowired
    private BundleDao bundleDao;

    @Autowired
    private ScoreDao scoreDao;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionnaireGroupDao questionnaireGroupDao;

    @Autowired
    QuestionnaireGroupService questionnaireGroupService;

    @Autowired
    QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper;

    /**
     * Converts this {@link Bundle} object to an {@link BundleDTO} object.
     *
     * @param fullVersion Indicates whether the returned {@link BundleDTO}
     *                    object should include all data from the
     *                    {@link Bundle} object or not.
     * @return An {@link BundleDTO} object based on this {@link Bundle} object.
     */
    public BundleDTO toBundleDTO(final Boolean fullVersion, Bundle bundle) {
        BundleDTO bundleDTO = new BundleDTO();
        bundleDTO.setId(bundle.getId());
        bundleDTO.setName(bundle.getName());
        bundleDTO.setAvailableLanguages(bundle.getAvailableLanguages());
        if (fullVersion) {
            bundleDTO.setIsPublished(bundle.getIsPublished());
            bundleDTO.setIsModifiable(bundle.isModifiable());
            bundleDTO.setDescription(bundle.getDescription());
            bundleDTO.setLocalizedWelcomeText(new TreeMap<>(bundle.getLocalizedWelcomeText()));
            bundleDTO.setLocalizedFinalText(new TreeMap<>(bundle.getLocalizedFinalText()));
            bundleDTO.setdeactivateProgressAndNameDuringSurvey(bundle.getDeactivateProgressAndNameDuringSurvey());
            bundleDTO.setShowProgressPerBundle(bundle.getShowProgressPerBundle());
            // this.getBundleQuestionnaires() can never be null because it is
            // initialized with an empty HashMap when a Bundle is created
            List<BundleQuestionnaireDTO> bundleQuestionnaireDTOs = bundle.getBundleQuestionnaires().stream()
                    .filter(bundleQuestionnaire -> bundleQuestionnaire.getQuestionnaire().getId() != null)
                    .map(bundleQuestionnaire -> {
                        BundleQuestionnaireDTO bundleQuestionnaireDTO = bundleQuestionnaire.toBundleQuestionnaireDTO();
                        bundleQuestionnaireDTO.setQuestionnaireDTO(questionnaireDTOMapper.apply(bundleQuestionnaire.getQuestionnaire()));
                        bundleQuestionnaireDTO.setBundleId(bundle.getId());
                        return bundleQuestionnaireDTO;
                    })
                    .toList();

            bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaireDTOs);
        }
        return bundleDTO;
    }


    public List<QuestionnaireGroupDTO> getNonAssignedQuestionnaireGroups(final Long bundleId) {


        // Step 1: Get all non-assigned questionnaires
        List<Questionnaire> nonAssignedQuestionnaires = getNonAssignedQuestionnaires(bundleId);

        // Step 2: Create a set of non-assigned questionnaire IDs
        Set<Long> nonAssignedQuestionnaireIds = nonAssignedQuestionnaires.stream()
                .map(Questionnaire::getId)
                .collect(Collectors.toSet());

        // Step 3: Get all questionnaire groups and filter them
        List<QuestionnaireGroupDTO> allQuestionnaireGroupDTOs = questionnaireGroupService.getAllQuestionnaireGroups().stream()
                .map(questionnaireGroupDTOMapper)
                .filter(groupDTO -> groupDTO.getQuestionnaireDTOS().stream()
                        .map(QuestionnaireDTO::getId)
                        .anyMatch(nonAssignedQuestionnaireIds::contains))
                .collect(Collectors.toList());

        // Step 4: Add non-assigned questionnaires that are not part of any group
        nonAssignedQuestionnaires.stream()
                .filter(questionnaire -> !isInQuestionnaireGroupList(questionnaire, allQuestionnaireGroupDTOs))
                .forEach(questionnaire -> {
                    QuestionnaireGroupDTO groupDTO = new QuestionnaireGroupDTO();
                    groupDTO.setGroupName(questionnaire.getName());
                    groupDTO.setQuestionnaireDTOS(List.of(questionnaireDTOMapper.apply(questionnaire)));
                    allQuestionnaireGroupDTOs.add(groupDTO);
                });

        // Step 5: Sort the final list by group name
        return allQuestionnaireGroupDTOs.stream()
                .sorted(Comparator.comparing(QuestionnaireGroupDTO::getGroupName))
                .collect(Collectors.toList());
    }

    private boolean isInQuestionnaireGroupList(Questionnaire questionnaire, List<QuestionnaireGroupDTO> groupDTOs) {
        return groupDTOs.stream()
                .flatMap(groupDTO -> groupDTO.getQuestionnaireDTOS().stream())
                .anyMatch(dto -> dto.getId().equals(questionnaire.getId()));
    }

    private List<Questionnaire> getNonAssignedQuestionnaires(final Long bundleId) {
        return questionnaireDao.getAllElements().stream()
                .filter(questionnaire -> !isQuestionnairePartOfBundle(questionnaire.getId(), bundleId))
                .collect(Collectors.toList());
    }

    private boolean isQuestionnairePartOfBundle(Long questionnaireId, Long bundleId) {
        return findByQuestionnaireId(questionnaireId).stream()
                .anyMatch(bundleQuestionnaire -> bundleQuestionnaire.getBundle().getId().equals(bundleId));
    }

    public List<BundleQuestionnaire> findByQuestionnaireId(Long questionnaireID) {
        return bundleQuestionnaireDao.findByQuestionnaire(questionnaireID);
    }
}
