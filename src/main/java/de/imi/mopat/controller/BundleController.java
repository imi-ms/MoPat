package de.imi.mopat.controller;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.helper.controller.AuthService;
import de.imi.mopat.helper.controller.BundleService;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.UserService;
import de.imi.mopat.helper.controller.ClinicService;
import de.imi.mopat.helper.model.BundleDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleClinic;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.user.User;
import de.imi.mopat.validator.BundleDTOValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 *
 */
@Controller
public class BundleController {

    @Autowired
    private AclClassDao aclClassDao;
    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private ConditionDao conditionDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private BundleDTOValidator bundleDTOValidator;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private BundleService bundleService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClinicService clinicService;
    @Autowired
    private QuestionnaireDTOMapper questionnaireDTOMapper;
    @Autowired
    private BundleDTOMapper bundleDTOMapper;
    @Autowired
    private AuthService authService;

    /**
     * @param id for bundle
     * @return resulting bundle for id
     */
    public BundleDTO getBundleDTO(final Long id) {
        BundleDTO result;
        BundleDTO bundleDTO;
        if (id == null || id <= 0) {
            result = new BundleDTO();
        } else {
            Bundle bundle = bundleDao.getElementById(id);
            if (bundle == null) {
                result = new BundleDTO();
            } else {
                bundleDTO = bundleDTOMapper.apply(true,bundle);
                for (BundleQuestionnaireDTO bundleQuestionnaireDTO
                        : bundleDTO.getBundleQuestionnaireDTOs()) {
                    bundleQuestionnaireDTO.getQuestionnaireDTO()
                                          .setHasScores(scoreDao.hasScore(questionnaireDao.getElementById(bundleQuestionnaireDTO.getQuestionnaireDTO()
                                                                                                                                .getId())));
                }
                result = bundleDTO;
            }
        }
        return result;
    }

    /**
     * @param id is id of the {@link Bundle Bundle} object
     * @return Returns all {@link Questionnaire Questionnaire} objects that are not already present
     * in the bundle with the given id.
     */
    private List<QuestionnaireDTO> getAvailableQuestionnaires(final Long bundleId) {
        // Fetch questionnaires not linked with the given bundle
        List<Questionnaire> availableQuestionnaires = bundleDao.getAvailableQuestionnairesForBundle(bundleId);

        // Collect IDs for fetching scores
        Set<Long> questionnaireIds = availableQuestionnaires.stream().map(Questionnaire::getId).collect(Collectors.toSet());
        Set<Long> questionnairesWithScores = scoreDao.findQuestionnairesWithScores(new ArrayList<>(questionnaireIds));

        // Map to DTO and sort
        return availableQuestionnaires.stream()
            .filter(q -> !q.getQuestions().isEmpty())
            .map(q -> {
                QuestionnaireDTO dto = questionnaireDTOMapper.apply(q);
                dto.setHasScores(questionnairesWithScores.contains(q.getId()));
                return dto;
            })
            .sorted(Comparator.comparing(QuestionnaireDTO::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/bundle/list</i>. Shows the list of bundles.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>bundle/list</i> website.
     */
    @RequestMapping(value = "/bundle/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String listBundles(final Model model) {
        List<Bundle> bundles = bundleDao.getAllElements();
        Set<Long> bundleIds = bundleService.getUniqueQuestionnaireIds(bundles);
        Set<Long> targetBundles = conditionDao.findConditionTargetIds(
            bundleIds.stream().toList(),
            "Bundle"
        );

        for (Bundle bundle : bundles) {
            bundle.setHasConditions(targetBundles.contains(bundle.getId()));
        }

        model.addAttribute("allBundles", bundles);
        return "bundle/list";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/bundle/fill</i>. Shows the page containing the
     * form fields for a new {@link Bundle Bundle} object.
     *
     * @param bundleId The id of the current bundle.
     * @param model    The model, which holds the information for the view.
     * @return The <i>bundle/fill</i> website.
     */
    @RequestMapping(value = "/bundle/fill", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String fillBundle(@RequestParam(value = "id", required = false) final Long bundleId,
        final Model model) {
        BundleDTO bundleDTO = getBundleDTO(bundleId);
        model.addAttribute("bundleDTO", bundleDTO);
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        model.addAttribute("availableQuestionnaireDTOs", bundleService.getAvailableQuestionnaires(bundleId));
        return "bundle/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/bundle/edit</i>. Provides the ability to
     * create a new {@link Bundle Bundle} object.
     *
     * @param bundleDTO The {@link BundleDTO BundleDTO} object from the view.
     * @param action    The name of the submit button which has been clicked.
     * @param result    The result for validation of the bundle object.
     * @param model     The model, which holds the information for the view.
     * @return Redirect to the <i>bundle/list</i> website.
     */
    @RequestMapping(value = "/bundle/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @Transactional("MoPat_User")
    public String editBundle(@RequestParam final String action,
        @ModelAttribute("bundleDTO") @Valid final BundleDTO bundleDTO, final BindingResult result,
        final Model model) {

        if (action.equalsIgnoreCase("cancel")) {
            return "redirect:/bundle/list";
        }

        // If the bundle has any incomplete encounters, it may not be edited
        if (bundleDTO.getId() != null && !bundleDao.getElementById(bundleDTO.getId())
            .isModifiable()) {
            return "redirect:/bundle/fill?id=" + bundleDTO.getId();
        }

        // Check if one of the welcome texts has only one newline and
        // change it to an empty string
        SortedMap<String, String> tempLocalizedWelcomeText = bundleDTO.getLocalizedWelcomeText();
        for (SortedMap.Entry entry : tempLocalizedWelcomeText.entrySet()) {
            if (entry.getValue().equals("<p><br></p>") || entry.getValue().equals("<br>")) {
                entry.setValue("");
            }
        }
        bundleDTO.setLocalizedWelcomeText(tempLocalizedWelcomeText);

        // Check if one of the final texts has only one newline and
        // change it to an empty string
        SortedMap<String, String> tempLocalizedFinalText = bundleDTO.getLocalizedFinalText();
        for (SortedMap.Entry entry : tempLocalizedFinalText.entrySet()) {
            if (entry.getValue().equals("<p><br></p>") || entry.getValue().equals("<br>")) {
                entry.setValue("");
            }
        }
        bundleDTO.setLocalizedFinalText(tempLocalizedFinalText);

        List<QuestionnaireDTO> availableQuestionnaireDTOs = getAvailableQuestionnaires(null);
        List<BundleQuestionnaireDTO> assignedBundleQuestionnaireDTOs = new ArrayList<>(
            bundleDTO.getBundleQuestionnaireDTOs());
        List<QuestionnaireDTO> questionnaireDTOsToDelete = new ArrayList<>();

        //make sure the changes in the bundleQuestionnaireDTO list of
        // bundleDTO are detained
        for (QuestionnaireDTO questionnaireDTO : new ArrayList<>(availableQuestionnaireDTOs)) {
            for (BundleQuestionnaireDTO bundleQuestionnaireDTO : assignedBundleQuestionnaireDTOs) {
                if (bundleQuestionnaireDTO.getQuestionnaireDTO() == null
                    || bundleQuestionnaireDTO.getQuestionnaireDTO().getId() == null) {
                    // Delete empty entries
                    bundleDTO.getBundleQuestionnaireDTOs().remove(bundleQuestionnaireDTO);
                    // If this available questionnaire is in the assigned
                    // questionnaire list as well,
                    // remove it from the available list
                } else if (questionnaireDTO.getId().longValue()
                    == bundleQuestionnaireDTO.getQuestionnaireDTO().getId().longValue()) {
                    // Remove this questionnaire from the list with available
                    // questionnaires
                    questionnaireDTOsToDelete.add(questionnaireDTO);
                    // Set the export templates to the assigned questionnaire
                    bundleQuestionnaireDTO.getQuestionnaireDTO()
                        .setExportTemplates(questionnaireDTO.getExportTemplates());
                }
            }
        }
        availableQuestionnaireDTOs.removeAll(questionnaireDTOsToDelete);

        // Sort assigned bundle questionnaires by position
        Collections.sort(bundleDTO.getBundleQuestionnaireDTOs(),
            (BundleQuestionnaireDTO o1, BundleQuestionnaireDTO o2) -> o1.getPosition()
                .compareTo(o2.getPosition()));

        // Validate the bundle object
        bundleDTOValidator.validate(bundleDTO, result);

        if (result.hasErrors()) {
            model.addAttribute("bundleDTO", bundleDTO);
            model.addAttribute("availableQuestionnaireDTOs", availableQuestionnaireDTOs);
            model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
            return "bundle/edit";
        }

        // Set property of the Bundle to current user
        User principal = authService.getAuthenticatedUser();

        Bundle bundle;
        if (bundleDTO.getId() != null) {
            bundle = bundleDao.getElementById(bundleDTO.getId());
            bundle.setName(bundleDTO.getName());
            bundle.setChangedBy(principal.getId());
            bundle.setDescription(bundleDTO.getDescription());
            bundle.setIsPublished(bundleDTO.getIsPublished());
            bundle.setShowProgressPerBundle(bundleDTO.getShowProgressPerBundle());
            bundle.setDeactivateProgressAndNameDuringSurvey(
                bundleDTO.getdeactivateProgressAndNameDuringSurvey());
        } else {
            bundle = new Bundle(bundleDTO.getName(), bundleDTO.getDescription(), principal.getId(),
                bundleDTO.getIsPublished(), bundleDTO.getShowProgressPerBundle(),
                bundleDTO.getdeactivateProgressAndNameDuringSurvey());
        }
        bundle.setLocalizedWelcomeText(bundleDTO.getLocalizedWelcomeText());
        bundle.setLocalizedFinalText(bundleDTO.getLocalizedFinalText());

        if (bundle.getId() == null) { // the bundle is completely new, thus
            // no removal of BundleQuestionnaire objects necessary and in the
            // end persist (not merge)
            bundleDao.merge(bundle);
            // Get the current user, which is the owner of the bundle
            User currentUser = authService.getAuthenticatedUser();
            // Create a new ACLObjectIdentity for the bundle and save it
            AclObjectIdentity bundleObjectIdentity = new AclObjectIdentity(bundle.getId(),
                Boolean.TRUE, aclClassDao.getElementByClass(Bundle.class.getName()), currentUser,
                null);
            aclObjectIdentityDao.persist(bundleObjectIdentity);
        } else {
            // the bundle is not new,
            // thus BundleQuestionnaire objects might have been removed or
            // repositioned.
            // To avoid complex code, we remove all BundleQuestionnaire
            // objects from the bundle and (re-)add them.
            // In the end: merge (not persist) (since the bundle already has
            // an ID)
            for (BundleQuestionnaire toDelete : bundle.getBundleQuestionnaires()) {
                HashSet<ExportTemplate> exportTemplates = new HashSet<>(
                    toDelete.getExportTemplates());
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

        return "redirect:/bundle/list";
    }

    /**
     * Controls the HTTP requests for the URL <i>bundle/remove</i>. Removes a {@link Bundle Bundle}
     * object by a given id and redirects to the list of bundles.
     *
     * @param id    is id of the {@link Bundle Bundle} object, which should be removed.
     * @param model The model, which holds the information for the view.
     * @return Redirect to the <i>bundle/list</i> website.
     */
    @RequestMapping(value = "/bundle/remove")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @Transactional("MoPat_User")
    public String removeBundle(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {
        Bundle bundle = bundleDao.getElementById(id);

        if (bundle != null) {
            if (bundle.isDeletable()) {
                // Delete connection to the clinics
                for (BundleClinic bundleClinic : bundle.getBundleClinics()) {
                    Clinic clinic = bundleClinic.getClinic();
                    clinic.removeBundleClinic(bundleClinic);
                    clinicService.merge(clinic);
                }

                // Delete connection to the questionnaires
                for (BundleQuestionnaire bundleQuestionnaire : bundle.getBundleQuestionnaires()) {
                    Questionnaire questionnaire = bundleQuestionnaire.getQuestionnaire();
                    questionnaire.removeBundleQuestionnaire(bundleQuestionnaire);
                    questionnaireDao.merge(questionnaire);
                }

                // Delete the corresponding conditions
                for (Condition condition : conditionDao.getConditionsByTarget(bundle)) {
                    if (condition instanceof SelectAnswerCondition
                        || condition instanceof SliderAnswerThresholdCondition) {
                        // Refresh the trigger so that multiple conditions of
                        // the same trigger will be correctly deleted
                        ConditionTrigger conditionTrigger = answerDao.getElementById(
                            condition.getTrigger().getId());
                        conditionTrigger.removeCondition(condition);
                        answerDao.merge((Answer) conditionTrigger);
                    }
                    conditionDao.remove(condition);
                }

                // Delete the corresponding ACL object for the removed bundle
                aclObjectIdentityDao.remove(aclObjectIdentityDao.getElementByClassAndObjectId(
                    aclClassDao.getElementByClass(Bundle.class.getName()), id));
                // Delete the bundle
                bundle.removeAllBundleClinics();
                bundle.removeAllBundleQuestionnaires();
                bundleDao.remove(bundle);
                model.addAttribute("messageSuccess",
                    messageSource.getMessage("bundle.error.deletePossible",
                        new Object[]{bundle.getName()}, LocaleContextHolder.getLocale()));
            } else {
                model.addAttribute("messageFail",
                    messageSource.getMessage("bundle.error.deleteNotPossible",
                        new Object[]{bundle.getName()}, LocaleContextHolder.getLocale()));
            }
        }
        return listBundles(model);
    }

    /**
     * Controls the HTTP request for the URL <i>/bundle/publish</i>. Toggles the publishing state of
     * the {@link Bundle bundle} object by the given id.
     *
     * @param id    is id of the {@link Bundle bundle} object to be published.
     * @param model The model, which holds the information for the view.
     * @return Redirect to the <i>bundle/list</i> website.
     */
    @RequestMapping(value = "/bundle/togglepublish")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String togglePublish(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {
        Bundle bundle = bundleDao.getElementById(id);
        // Only change the publishing state,
        // if the bundle has at least one questionnaire
        if (bundle != null && !bundle.getBundleQuestionnaires().isEmpty()
            && bundle.isModifiable()) {
            bundle.setIsPublished(!bundle.getIsPublished());
            bundleDao.merge(bundle);
        }
        return "redirect:/bundle/list";
    }
}
