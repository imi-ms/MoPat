package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BundleService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(BundleService.class);

    @Autowired
    private QuestionnaireDTOMapper questionnaireDTOMapper;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private QuestionnaireGroupService questionnaireGroupService;
    @Autowired
    private BundleQuestionnaireDao bundleQuestionnaireDao;
    @Autowired
    private QuestionnaireService questionnaireService;

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

    /**
     * Retrieves available questionnaires for a specific bundle, sorted by name
     * If the bundle is not found, returns all questionnaires
     *
     * @param bundleId The ID of the bundle to retrieve available questionnaires for
     * @return A list of {@link QuestionnaireDTO} objects representing the available questionnaires
     */
    public List<QuestionnaireDTO> getAvailableQuestionnaires(final Long bundleId) {
        Optional<Bundle> bundle = findBundleById(bundleId);

        if (bundle.isEmpty()) {
            return questionnaireService.getAllQuestionnaireDTOs();
        }

        Set<Long> unassignedGroupIds = getUnassignedGroupIds(bundle.get());
        List<QuestionnaireGroup> unassignedQuestionnaireGroups = questionnaireGroupService.getQuestionnaireGroups(unassignedGroupIds);

        // Flatten the sorted QuestionnaireGroups into a list of QuestionnaireDTOs
        return unassignedQuestionnaireGroups.stream()
                .map(group -> group.getQuestionnaires().stream()
                        .min(Comparator.comparingInt(Questionnaire::getVersion)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toQuestionnaireDTO)
                .sorted(Comparator.comparing(QuestionnaireDTO::getName))
                .collect(Collectors.toList());
    }

    /**
     * Finds a {@link Bundle} by its ID
     *
     * @param bundleId The ID of the bundle to find
     * @return An {@link Optional} containing the found bundle, or empty if not found.
     */
    private Optional<Bundle> findBundleById(Long bundleId) {
        return bundleDao.getAllElements().stream()
                .filter(b1 -> b1.getId().equals(bundleId))
                .findFirst();
    }

    /**
     * Retrieves the set of group IDs that are not assigned to any questionnaire in the given bundle
     *
     * @param bundle The bundle to check for assigned group IDs
     * @return A set of unassigned group IDs
     */
    private Set<Long> getUnassignedGroupIds(Bundle bundle) {
        Set<Long> assignedGroupIds = bundle.getBundleQuestionnaires().stream()
                .map(bq -> bq.getQuestionnaire().getGroup().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> allGroupIds = questionnaireGroupService.getAllGroupIds();
        allGroupIds.removeAll(assignedGroupIds);

        return allGroupIds;
    }

    /**
     * Converts a {@link Questionnaire} to a {@link QuestionnaireDTO}.
     *
     * @param questionnaire The questionnaire to convert.
     * @return A {@link QuestionnaireDTO} object.
     */
    private QuestionnaireDTO toQuestionnaireDTO(Questionnaire questionnaire) {
        QuestionnaireDTO questionnaireDTO = questionnaireDTOMapper.apply(questionnaire);
        questionnaireDTO.setHasScores(scoreDao.hasScore(questionnaire));
        return questionnaireDTO;
    }

    /** Finds {@link BundleQuestionnaire} objects by the ID of the questionnaire.
     *
     * @param questionnaireID The ID of the questionnaire to find bundle questionnaires for.
     * @return A list of {@link BundleQuestionnaire} objects.
     */
    public List<BundleQuestionnaire> findByQuestionnaireId(Long questionnaireID) {
        return bundleQuestionnaireDao.findByQuestionnaire(questionnaireID);
    }
}
