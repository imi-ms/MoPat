package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.model.BundleDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;

import java.util.*;
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
    @Autowired
    private BundleDTOMapper bundleDTOMapper;

    /**
     * Retrieves a BundleDTO for the given bundle ID.
     *
     * @param id The ID of the bundle.
     * @return The corresponding BundleDTO or a new instance if the bundle is not found.
     */
    public BundleDTO getBundleDTO(final Long id) {
        if (id == null || id <= 0) {
            return new BundleDTO();
        }

        Bundle bundle = bundleDao.getElementById(id);
        if (bundle == null) {
            return new BundleDTO();
        }

        BundleDTO bundleDTO = bundleDTOMapper.apply(true, bundle);

        bundleDTO.getBundleQuestionnaireDTOs().forEach(bundleQuestionnaireDTO -> {
            QuestionnaireDTO questionnaireDTO = bundleQuestionnaireDTO.getQuestionnaireDTO();
            if (questionnaireDTO != null && questionnaireDTO.getId() != null) {
                questionnaireDTO.setHasScores(scoreDao.hasScore(questionnaireDao.getElementById(questionnaireDTO.getId())));
            }
        });

        return bundleDTO;
    }


    /**
     * Retrieves all questionnaires that are not assigned to the given bundle.
     * The list is sorted by group name, group ID, and version.
     *
     * @param bundleId The ID of the bundle.
     * @return A sorted list of unassigned questionnaires.
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

    /**
     * Prepares a bundle for editing by cleaning text fields and ensuring
     * that non-admin users cannot publish the bundle.
     *
     * @param bundleDTO The bundle to prepare.
     */
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

    /**
     * Synchronizes assigned and available questionnaires.
     * Ensures that assigned questionnaires are removed from the available list
     * and updates missing data (ExportTemplates, QuestionnaireGroup, Scores) for assigned questionnaires.
     *
     * @param bundleQuestionnaireDTOS The list of assigned questionnaires.
     * @param availableQuestionnaireDTOs The list of available questionnaires.
     */
    public void syncAssignedAndAvailableQuestionnaires(List<BundleQuestionnaireDTO> bundleQuestionnaireDTOS, List<QuestionnaireDTO> availableQuestionnaireDTOs) {
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

    /**
     * Saves or updates the given bundle.
     * If the bundle does not exist, it is created. Otherwise, it is updated.
     *
     * @param bundleDTO The bundle data.
     */
    public void saveOrUpdateBundle(BundleDTO bundleDTO) {
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

        persistBundleQuestionnaires(bundleDTO, bundle);

        boolean isNotAdmin = !authService.hasExactRole(UserRole.ROLE_ADMIN);
        boolean hasNoQuestionnaires = bundle.getBundleQuestionnaires().isEmpty();
        if (isNotAdmin || hasNoQuestionnaires) {
            bundleDTO.setIsPublished(false);
        }

        bundleDao.merge(bundle);
    }

    private void persistBundleQuestionnaires(BundleDTO bundleDTO, Bundle bundle) {
        if (bundleDTO.getBundleQuestionnaireDTOs() == null || bundleDTO.getBundleQuestionnaireDTOs().isEmpty()) {
            return;
        }

        for (BundleQuestionnaireDTO bundleQuestionnaireDTO : bundleDTO.getBundleQuestionnaireDTOs()) {
            if (bundleQuestionnaireDTO.getQuestionnaireDTO() == null || bundleQuestionnaireDTO.getQuestionnaireDTO().getId() == null) {
                continue;
            }

            Questionnaire questionnaire = questionnaireDao.getElementById(bundleQuestionnaireDTO.getQuestionnaireDTO().getId());
            BundleQuestionnaire bundleQuestionnaire = new BundleQuestionnaire(
                    bundle,
                    questionnaire,
                    bundleQuestionnaireDTO.getPosition().intValue(),
                    Optional.ofNullable(bundleQuestionnaireDTO.getIsEnabled()).orElse(false),
                    Optional.ofNullable(bundleQuestionnaireDTO.getShowScores()).orElse(false)
            );

            bundleQuestionnaireDTO.getExportTemplates().stream()
                    .map(exportTemplateDao::getElementById)
                    .filter(Objects::nonNull)
                    .forEach(exportTemplate -> {
                        bundleQuestionnaire.addExportTemplate(exportTemplate);
                        exportTemplateDao.merge(exportTemplate);
                    });
            
            bundle.addBundleQuestionnaire(bundleQuestionnaire);
            questionnaire.addBundleQuestionnaire(bundleQuestionnaire);
            questionnaireDao.merge(questionnaire);
        }
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
    
    /**
     * Returns the list of bundles sorted by their name property (ascending).
     *
     * @param bundles to sort
     * @return sorted List<Bundle>
     */
    public List<Bundle> sortBundlesByNameAsc(List<Bundle> bundles) {
        bundles.sort(Comparator.comparing(Bundle::getName));
        return bundles;
    }
}
