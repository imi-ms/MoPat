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
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.validator.BundleDTOValidator;

import java.util.List;
import java.util.Set;
import jakarta.validation.Valid;
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
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private BundleDTOValidator bundleDTOValidator;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private BundleService bundleService;
    @Autowired
    private ClinicService clinicService;

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
        BundleDTO bundleDTO = bundleService.getBundleDTO(bundleId);
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

        if (!bundleService.isBundleModifiable(bundleDTO)) {
            return "redirect:/bundle/fill?id=" + bundleDTO.getId();
        }

        bundleService.prepareBundleForEdit(bundleDTO);

        List<QuestionnaireDTO> availableQuestionnaireDTOs = bundleService.getAvailableQuestionnaires(null);
        bundleService.syncAssignedAndAvailableQuestionnaires(bundleDTO.getBundleQuestionnaireDTOs(), availableQuestionnaireDTOs);

        // Validate the bundle object
        bundleDTOValidator.validate(bundleDTO, result);

        if (result.hasErrors()) {
            model.addAttribute("bundleDTO", bundleDTO);
            model.addAttribute("availableQuestionnaireDTOs", availableQuestionnaireDTOs);
            model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
            return "bundle/edit";
        }

        bundleService.saveOrUpdateBundle(bundleDTO);

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
