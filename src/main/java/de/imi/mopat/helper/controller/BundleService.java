package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private BundleQuestionnaireDao bundleQuestionnaireDao;
    @Autowired
    private QuestionnaireService questionnaireService;

    /**
     * Retrieves all available {@link QuestionnaireDTO} objects that are not currently assigned
     * to the {@link Bundle} with the given ID. This method filters out any questionnaires that
     * have no associated questions and sorts the results first by group name, then by group ID,
     * and finally by version.
     *
     * @param bundleId The ID of the {@link Bundle} from which unassigned questionnaires should be retrieved.
     * @return A sorted list of {@link QuestionnaireDTO} objects that are not assigned to the specified bundle
     *         and contain at least one question. If the bundle ID is null or not found, all available questionnaires
     *         are returned.
     */
    public List<QuestionnaireDTO> getAvailableQuestionnaires(final Long bundleId) {
        Optional<Bundle> bundle = findBundleById(bundleId);

        List<Questionnaire> unassignedQuestionnaires;
        unassignedQuestionnaires = bundle
                .map(this::getUnassignedQuestionnaires)
                .orElseGet(() -> questionnaireService.getAllQuestionnaires());

        return unassignedQuestionnaires.stream()
                .filter(questionnaire -> !questionnaire.getQuestions().isEmpty())
                .map(questionnaire -> {
                    QuestionnaireDTO questionnaireDTO = questionnaireDTOMapper.apply(questionnaire);
                    questionnaireDTO.setHasScores(scoreDao.hasScore(questionnaire));
                    return questionnaireDTO;
                })
                .sorted(
                        Comparator.comparing(QuestionnaireDTO::getQuestionnaireVersionGroupName, String::compareToIgnoreCase)
                                .thenComparing(QuestionnaireDTO::getQuestionnaireVersionGroupId)
                                .thenComparing(QuestionnaireDTO::getVersion))
                .toList();
    }

    /**
     * Finds a {@link Bundle} by its ID
     *
     * @param bundleId The ID of the bundle to find
     * @return An {@link Optional} containing the found bundle, or empty if not found.
     */
    private Optional<Bundle> findBundleById(Long bundleId) {
        return bundleDao.getAllElements().stream()
                .filter(bundle -> bundle.getId().equals(bundleId))
                .findFirst();
    }

    /**
     * Retrieves the set of questionnaires that are not assigned to the given bundle
     *
     * @param bundle The bundle to check for assigned group IDs
     * @return A List of unassigned questionnaires
     */
    private List<Questionnaire> getUnassignedQuestionnaires(Bundle bundle) {
        Set<Questionnaire> assignedQuestionnaires = bundle.getBundleQuestionnaires().stream()
                .map(BundleQuestionnaire::getQuestionnaire)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Questionnaire> allQuestionnaires = questionnaireService.getAllQuestionnaires();
        allQuestionnaires.removeAll(assignedQuestionnaires);

        return allQuestionnaires;
    }

    /** Finds {@link BundleQuestionnaire} objects by the ID of the questionnaire.
     *
     * @param questionnaireID The ID of the questionnaire to find bundle questionnaires for.
     * @return A list of {@link BundleQuestionnaire} objects.
     */
    public List<BundleQuestionnaire> findByQuestionnaireId(Long questionnaireID) {
        return bundleQuestionnaireDao.findByQuestionnaire(questionnaireID);
    }

    /**
     * Returns a set of unique {@link Bundle}-IDs for a list of {@link Bundle}
     * instances
     * @param bundles to get ids for
     * @return {@link Set} with Ids
     */
    public Set<Long> getUniqueQuestionnaireIds(List<Bundle> bundles) {
        Set<Long> resultSet = new HashSet<>();
        for (Bundle bundle: bundles) {
            resultSet.add(bundle.getId());
        }
        return resultSet;
    }

    public boolean isBundleModifiable(BundleDTO bundleDTO) {
        return bundleDTO.getId() == null || bundleDao.getElementById(bundleDTO.getId()).isModifiable();
    }
}
