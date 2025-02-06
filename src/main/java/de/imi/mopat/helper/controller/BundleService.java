package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserRole;
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
    @Autowired
    private AuthService authService;
    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private AclClassDao aclClassDao;
    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;

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

    public void prepareBundleForEdit(BundleDTO bundleDTO) {
        cleanUpTextFields(bundleDTO);

        if (!authService.hasExactRole(UserRole.ROLE_ADMIN)) {
            bundleDTO.setIsPublished(false);
        }

        removeUnassignedBundleQuestionnaires(bundleDTO);
    }

    public void cleanUpTextFields(BundleDTO bundleDTO) {
        bundleDTO.setLocalizedWelcomeText(cleanUpLocalizedText(bundleDTO.getLocalizedWelcomeText()));
        bundleDTO.setLocalizedFinalText(cleanUpLocalizedText(bundleDTO.getLocalizedFinalText()));
    }

    private SortedMap<String, String> cleanUpLocalizedText(SortedMap<String, String> textMap) {
        textMap.replaceAll((key, value) -> ("<p><br></p>".equals(value) || "<br>".equals(value)) ? "" : value);
        return textMap;
    }

    private void removeUnassignedBundleQuestionnaires(BundleDTO bundleDTO) {
        bundleDTO.getBundleQuestionnaireDTOs().removeIf(
                bq -> bq.getQuestionnaireDTO() == null || bq.getQuestionnaireDTO().getId() == null
        );
    }

    public void syncAssignedAndAvailableQuestionnaires(List<BundleQuestionnaireDTO> bundleQuestionnaireDTOS, List<QuestionnaireDTO> availableQuestionnaireDTOs) {
        // IDs der bereits zugewiesenen Fragebögen sammeln
        Set<Long> assignedIds = bundleQuestionnaireDTOS.stream()
                .map(BundleQuestionnaireDTO::getQuestionnaireDTO)
                .filter(Objects::nonNull)
                .map(QuestionnaireDTO::getId)
                .collect(Collectors.toSet());

        List<QuestionnaireDTO> assignedQuestionnaires = availableQuestionnaireDTOs.stream()
                .filter(q -> assignedIds.contains(q.getId()))
                .toList();

        availableQuestionnaireDTOs.removeAll(assignedQuestionnaires);

        updateMissingQuestionnaireData(bundleQuestionnaireDTOS, assignedQuestionnaires);

        bundleQuestionnaireDTOS.sort(Comparator.comparing(BundleQuestionnaireDTO::getPosition));
    }

    private void updateMissingQuestionnaireData(List<BundleQuestionnaireDTO> assignedBundleQuestionnaires, List<QuestionnaireDTO> assignedQuestionnaires) {
        Map<Long, QuestionnaireDTO> assignedQuestionnaireMap = assignedQuestionnaires.stream()
                .collect(Collectors.toMap(QuestionnaireDTO::getId, Function.identity()));

        assignedBundleQuestionnaires.forEach(abq -> {
            QuestionnaireDTO assignedQuestionnaire = assignedQuestionnaireMap.get(abq.getQuestionnaireDTO().getId());
            if (assignedQuestionnaire != null) {
                abq.getQuestionnaireDTO().setExportTemplates(assignedQuestionnaire.getExportTemplates());
                abq.getQuestionnaireDTO().setQuestionnaireGroupDTO(assignedQuestionnaire.getQuestionnaireGroupDTO());
                abq.getQuestionnaireDTO().setHasScores(assignedQuestionnaire.getHasScores());
            }
        });
    }

    public void saveOrUpdateBundle(BundleDTO bundleDTO) {
        if (!authService.hasExactRole(UserRole.ROLE_ADMIN)){
            bundleDTO.setIsPublished(false);
        }
        // Set property of the Bundle to current user
        User currentUser = authService.getAuthenticatedUser();

        Bundle bundle = (bundleDTO.getId() != null)
                ? bundleDao.getElementById(bundleDTO.getId())
                : new Bundle();

        updateBundleProperties(bundle, bundleDTO, currentUser);

        if (bundle.getId() == null) {
            bundleDao.merge(bundle);
            createAclEntry(bundle, currentUser);
        } else {
            cleanupRemovedBundleQuestionnaires(bundle);
        }
        // Save the bundle questionnaire relationships
        if (bundleDTO.getBundleQuestionnaireDTOs() != null
                && !bundleDTO.getBundleQuestionnaireDTOs().isEmpty()) {
            for (BundleQuestionnaireDTO bundleQuestionnaireDTO : bundleDTO.getBundleQuestionnaireDTOs()) {
                if (bundleQuestionnaireDTO.getQuestionnaireDTO() == null
                        || bundleQuestionnaireDTO.getQuestionnaireDTO().getId() == null) {
                    continue;
                }
                Questionnaire questionnaire = questionnaireDao.getElementById(
                        bundleQuestionnaireDTO.getQuestionnaireDTO().getId());

                if (bundleQuestionnaireDTO.getIsEnabled() == null) {
                    bundleQuestionnaireDTO.setIsEnabled(false);
                }

                if (bundleQuestionnaireDTO.getShowScores() == null) {
                    bundleQuestionnaireDTO.setShowScores(false);
                }

                BundleQuestionnaire bundleQuestionnaire = new BundleQuestionnaire(bundle,
                        questionnaire, bundleQuestionnaireDTO.getPosition().intValue(),
                        bundleQuestionnaireDTO.getIsEnabled(), bundleQuestionnaireDTO.getShowScores());
                for (Long id : bundleQuestionnaireDTO.getExportTemplates()) {
                    ExportTemplate exportTemplate = exportTemplateDao.getElementById(id);
                    if (exportTemplate != null) {
                        bundleQuestionnaire.addExportTemplate(exportTemplate);
                        exportTemplateDao.merge(exportTemplate);
                    }
                }
                bundle.addBundleQuestionnaire(bundleQuestionnaire);
                questionnaire.addBundleQuestionnaire(bundleQuestionnaire);
                questionnaireDao.merge(questionnaire);
            }
        }
        // If the bundle has no questionnaire change isPublished to false
        if (Boolean.TRUE.equals(bundle.getIsPublished()) && bundle.getBundleQuestionnaires()
                .isEmpty()) {
            bundle.setIsPublished(Boolean.FALSE);
        }
        bundleDao.merge(bundle);
    }

    private void updateBundleProperties(Bundle bundle, BundleDTO bundleDTO, User principal) {
        bundle.setName(bundleDTO.getName());
        bundle.setChangedBy(principal.getId());
        bundle.setDescription(bundleDTO.getDescription());
        bundle.setShowProgressPerBundle(bundleDTO.getShowProgressPerBundle());
        bundle.setDeactivateProgressAndNameDuringSurvey(bundleDTO.getdeactivateProgressAndNameDuringSurvey());
        bundle.setLocalizedWelcomeText(bundleDTO.getLocalizedWelcomeText());
        bundle.setLocalizedFinalText(bundleDTO.getLocalizedFinalText());
        bundle.setIsPublished(bundleDTO.getIsPublished());
    }

    private void createAclEntry(Bundle bundle, User currentUser) {
        AclObjectIdentity bundleObjectIdentity = new AclObjectIdentity(
                bundle.getId(),
                Boolean.TRUE, 
                aclClassDao.getElementByClass(Bundle.class.getName()), 
                currentUser,
                null
        );
        aclObjectIdentityDao.persist(bundleObjectIdentity);
    }

    private void cleanupRemovedBundleQuestionnaires(Bundle bundle) {
        for (BundleQuestionnaire toDelete : bundle.getBundleQuestionnaires()) {
            HashSet<ExportTemplate> exportTemplates = new HashSet<>(toDelete.getExportTemplates());
            toDelete.removeExportTemplates();
            for (ExportTemplate exportTemplate : exportTemplates) {
                exportTemplateDao.merge(exportTemplate);
            }
            Questionnaire questionnaire = toDelete.getQuestionnaire();
            questionnaire.removeBundleQuestionnaire(toDelete);
            questionnaireDao.merge(questionnaire);
        }
        bundle.removeAllBundleQuestionnaires();
        bundleDao.merge(bundle);
    }
}
