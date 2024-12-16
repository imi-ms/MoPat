package de.imi.mopat.controller;

import de.imi.mopat.dao.*;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.controller.*;
import de.imi.mopat.auth.PinAuthorizationService;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ResponseDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.dao.user.PinAuthorizationDao;
import de.imi.mopat.helper.model.BundleDTOMapper;
import de.imi.mopat.helper.model.ClinicDTOMapper;
import de.imi.mopat.helper.model.EncounterDTOMapper;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.PatientDataRetriever;
import de.imi.mopat.io.EncounterExporter;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.ClinicDTO;
import de.imi.mopat.model.enumeration.*;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.PointOnImageDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.ResponseDTO;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.user.User;
import de.imi.mopat.validator.MoPatValidator;

import java.sql.Timestamp;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes({"encounterDTO", "hideProfile"})
public class SurveyController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SurveyController.class);
    @Autowired
    private ApplicationContext appContext;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private ConditionDao conditionDao;
    @Autowired
    private ResponseDao responseDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired(required = false)
    private EncounterExporter encounterExporter;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private AuditEntryDao auditEntryDao;
    @Autowired
    private BundleDTOMapper bundleDTOMapper;
    @Autowired
    private AclEntryDao aclEntryDao;
    @Autowired
    private ClinicConfigurationMappingDao clinicConfigurationMappingDao;
    @Autowired
    private ClinicConfigurationMappingService clinicConfigurationMappingService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private EncounterDTOMapper encounterDTOMapper;
    @Autowired
    private PinAuthorizationDao pinAuthorizationDao;
    @Autowired
    private PinAuthorizationService pinAuthorizationService;
    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private ClinicService clinicService;
    @Autowired
    private ClinicDTOMapper clinicDTOMapper;
    @Autowired
    private Validator validator;

    // Initialize every needed configuration information as a final string
    private final String className = this.getClass().getName();
    private final String caseNumberTypeProperty = "caseNumberType";

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/mobile/survey/index</i>. Shows the page for checking the case number.
     *
     * @param model The model, which holds the information for the view
     * @return The <i>mobile/survey/check</i> website.
     */
    @RequestMapping(value = "/mobile/survey/index", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String showCheckCaseNumberFirstTime(final Model model) {
        model.addAttribute(
            "encounterDTO",
            encounterDTOMapper.apply(true, new Encounter())
        );
        model.addAttribute("hideProfile", Boolean.FALSE);

        addClinicInfoToModel(model);

        if (model.getAttribute("activeClinicDTO") == null) {
            return "redirect:/error/clinicNotFound";
        }

        return showCheckCaseNumber(model, null);
    }


    /**
     * Controls the HTTP GET requests for the URL
     * <i>/mobile/survey/clinicSelect</i>. Shows the page for selecting clinic.
     *
     * @param model The model, which holds the information for the view
     * @return The <i>mobile/survey/check</i> website.
     */
    @RequestMapping(value = "/mobile/survey/clinicSelect", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String showSelectClinic(final Model model) {
        return showCheckCaseNumberFirstTime(model);
    }


    /**
     * Controls the HTTP POST requests for the URL
     * <i>/mobile/survey/clinicSelect</i>. Checks selected clinic id and assigns clinic to the model
     * <p>
     * In case the passed action is <code>showBundles</code>, it redirects to the bundle overview page.
     *
     * @param activeClinicDTO (<i>required</i>) dto of the selected clinic
     * @param model           The model, which holds the information for the view
     * @return Redirect to the <i>mobile/survey/check</i> website In that case, redirect to
     * <i>survey/bundles</i>.
     */
    @RequestMapping(value = "/mobile/survey/clinicSelect", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String SelectClinic(
        @ModelAttribute(value = "activeClinicDTO") final ClinicDTO activeClinicDTO,
        final Model model
    ) {
        User user = getCurrentUser();
        user.setLastSelectedClinicId(activeClinicDTO.getId());
        userDao.merge(user);

        return showCheckCaseNumberFirstTime(model);

    }

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/mobile/survey/check</i>. Displays the page for checking the case
     * number.
     *
     * @param model The model, which holds the information for the view
     * @return Redirect to the <i>mobile/survey/check</i> website
     */
    @RequestMapping(value = "/mobile/survey/check", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String showCheckCaseNumber(final Model model, final BindingResult result) {
        if (!model.containsAttribute("encounterDTO")) {
            model.addAttribute(
                "encounterDTO",
                encounterDTOMapper.apply(true, new Encounter()));
        }

        addClinicInfoToModel(model);

        ClinicDTO activeClinicDTO = (ClinicDTO) model.getAttribute("activeClinicDTO");

        if (activeClinicDTO != null && clinicConfigurationMappingService.clinicHasConfig(activeClinicDTO.getId())) {
            if (!model.containsAttribute("patientDataService")) {
                if (clinicConfigurationMappingDao.isRegistryOfPatientActivated(activeClinicDTO.getId())) {
                    model.addAttribute("patientDataService", "register");
                } else if (clinicConfigurationMappingDao.isUsePatientDataLookupActivated(activeClinicDTO.getId())) {
                    model.addAttribute("patientDataService", "searchHIS");
                } else if (clinicConfigurationMappingDao.isPseudonymizationServiceActivated(activeClinicDTO.getId())) {
                    model.addAttribute("patientDataService", "pseudonym");
                } else {
                    model.addAttribute("patientDataService", "inactive");
                }
            }

            model.addAttribute("register",
                clinicConfigurationMappingDao.isRegistryOfPatientActivated(activeClinicDTO.getId()));
            model.addAttribute("pseudonym",
                clinicConfigurationMappingDao.isPseudonymizationServiceActivated(activeClinicDTO.getId()));

            boolean isHISActivated =
                clinicConfigurationMappingDao.isUsePatientDataLookupActivated(activeClinicDTO.getId());
            model.addAttribute("searchHIS", isHISActivated);
            
            model.addAttribute("searchHISType", "CASE_NUMBER");
            if (isHISActivated) {
                PatientDataRetriever patientDataRetriever = getPatientRetriever(activeClinicDTO.getId());
                if (patientDataRetriever.getClass() == HL7v22PatientInformationRetrieverByPID.class) {
                    model.addAttribute("searchHISType", "PID");
                }
            }

        }

        model.addAttribute("hideProfile", Boolean.FALSE);
        String caseNumberType = getCaseNumberType();
        if (!caseNumberType.matches("text|number")) {
            LOGGER.info("[WARNING] The value '{}' of the property " + this.getClass().getName()
                + ".caseNumberType is invalid. Defaulting to 'text'.", caseNumberType);
            caseNumberType = "text";
        }
        model.addAttribute("caseNumberType", caseNumberType);
        return "mobile/survey/check";
    }

    /**
     * Controls the HTTP POST requests for the URL
     * <i>/mobile/survey/check</i>. Checks a case number and looks it up in the
     * HIS, if desired. The patient data is then added to the {@link Encounter Encounter} object stored in the session.
     * <p>
     * In case the passed action is <code>showBundles</code>, it redirects to the bundle overview page.
     *
     * @param caseNumber         (<i>required</i>) Case number for the patient
     * @param patientDataService The registration service for the casenumber and patient Id
     * @param encounterDTO       The current {@link EncounterDTO}
     * @param result             The result for validation of the {@link EncounterDTO}
     * @param model              The model, which holds the information for the view
     * @param session            The current session
     * @return Redirect to the <i>mobile/survey/check</i> website, unless
     * <i>action</i> is <code>showBundles</code>. In that case, redirect to
     * <i>survey/bundles</i>.
     */
    @RequestMapping(value = "/mobile/survey/check", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String checkCaseNumber(
        @RequestParam(value = "caseNumber", required = true) final String caseNumber,
        @RequestParam(value = "patientDataService", required = true) final String patientDataService,
        @ModelAttribute(value = "encounterDTO") final EncounterDTO encounterDTO,
        @ModelAttribute(value = "activeClinicId") final Long activeClinicId,
        final BindingResult result, final Model model, final HttpSession session
    ) {

        if (!caseNumber.isEmpty() && caseNumber.trim().isEmpty()) {
            result.rejectValue("caseNumber", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("encounter.error" + ".caseNumberIsEmpty", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        validator.validate(encounterDTO, result);

        if (result.hasErrors()) {
            return showCheckCaseNumber(model, result);
        }
        encounterDTO.removeDemographics();
        encounterDTO.setCaseNumber(caseNumber);

        Clinic activeClinic = clinicDao.getElementById(activeClinicId);

        //Checkout which service to save or get patient data has been chosen
        if (patientDataService.equalsIgnoreCase("searchHIS")) {
            PatientDataRetriever patientDataRetriever = getPatientRetriever(activeClinicId);
            if (patientDataRetriever != null) {
                EncounterDTO retrievedEncounter = patientDataRetriever.retrievePatientData(
                    activeClinic,
                    caseNumber
                );

                if (retrievedEncounter != null) {
                    if (retrievedEncounter.getBirthdate() != null
                        && retrievedEncounter.getBirthdate().before(new java.util.Date())) {
                        encounterDTO.setBirthdate(retrievedEncounter.getBirthdate());
                    }
                    if (retrievedEncounter.getFirstname() != null
                        && !retrievedEncounter.getFirstname().trim().isEmpty()) {
                        encounterDTO.setFirstname(retrievedEncounter.getFirstname());
                    }
                    if (retrievedEncounter.getLastname() != null
                        && !retrievedEncounter.getLastname().trim().isEmpty()) {
                        encounterDTO.setLastname(retrievedEncounter.getLastname());
                    }
                    encounterDTO.setGender(retrievedEncounter.getGender());
                    if (retrievedEncounter.getPatientID() != null
                        && retrievedEncounter.getPatientID() > 0) {
                        encounterDTO.setPatientID(retrievedEncounter.getPatientID());
                    }
                } else {
                    result.reject("not.found",
                        messageSource.getMessage("survey.error" + ".noSuchPatient", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }
            }
            model.addAttribute("patientDataService", "searchHIS");
        } else {
            encounterDTO.setPatientID(null);
            model.addAttribute("registerSuccess", true);
            if (patientDataService.equalsIgnoreCase("register")) {
                model.addAttribute("patientDataService", "register");
            } else {
                model.addAttribute("patientDataService", "pseudonym");
            }
        }

        addClinicInfoToModelForKnownId(model, activeClinicId);

        model.addAttribute("hideProfile", Boolean.FALSE);
        return showCheckCaseNumber(model, result);
    }

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/mobile/survey/bundles</i>. Displays the available bundles, of which
     * one can be selected in order to start a questionnaire.
     *
     * @param encounterDTO       The current {@link EncounterDTO}
     * @param patientDataService The selected service to retrieve patient data
     * @param result             The result for validation of the {@link EncounterDTO}
     * @param model              The model, which holds the information for the view
     * @return Redirect to the <i>mobile/survey/bundles</i> website
     */
    @RequestMapping(value = "/mobile/survey/bundles", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String showBundles(
        @RequestParam(value = "patientDataService", required = false) final String patientDataService,
        @RequestParam(value = "activeClinicId", required = true) final Long activeClinicId,
        @ModelAttribute(value = "encounterDTO") final EncounterDTO encounterDTO,
        final BindingResult result, final Model model) {

        addClinicInfoToModelForKnownId(model, activeClinicId);

        if (patientDataService != null && patientDataService.equalsIgnoreCase("searchHIS")) {
            PatientDataRetriever patientDataRetriever = getPatientRetriever(activeClinicId);
            if (patientDataRetriever != null) {
                Clinic activeClinic = clinicDao.getElementById(activeClinicId);
                EncounterDTO retrievedEncounter = patientDataRetriever.retrievePatientData(
                    activeClinic,
                    encounterDTO.getCaseNumber()
                );

                if (retrievedEncounter == null) {
                    result.reject("not.found",
                        messageSource.getMessage("survey.error" + ".noSuchPatient", new Object[]{},
                            LocaleContextHolder.getLocale())
                    );
                    return showCheckCaseNumber(model, result);
                }
            }
        }

        // Create a map with bundleId's as Key and a map with locale codes as
        // key and a lists of encounters as value.
        // This map is easy to handle in the corresponding jsp file
        SortedMap<BundleDTO, Map<String, List<EncounterDTO>>> bundleLanguageEncounterMap = new TreeMap<>(
            (BundleDTO o1, BundleDTO o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        // Iterate over all available bundles and save the BundleDTO in the Map
        for (Bundle bundle : bundleDao.getAllElements()) {
            // The bundle needs to be published and has to have at least one
            // clinic attached
            if (bundle.getIsPublished() && !bundle.getBundleClinics()
                .isEmpty()) {
                bundleLanguageEncounterMap.put(
                    bundleDTOMapper.apply(false, bundle),
                    new HashMap<>());
            }
        }

        // Get all incomplete encounters for the given caseNumber
        List<Encounter> incompleteEncounters = encounterDao.getIncompleteEncounters(
            encounterDTO.getCaseNumber());

        // Loop through all incomplete encounter
        for (Encounter incompleteEncounter : incompleteEncounters) {
            // Save the temporarily used BundleDTO to save some computation time
            BundleDTO tempBundleDTO = bundleDTOMapper.apply(false, incompleteEncounter.getBundle());
            // If the bundle is already in the map the user has the rights to
            // see it
            if (bundleLanguageEncounterMap.containsKey(tempBundleDTO)) {
                Map<String, List<EncounterDTO>> localeCodeEncounterMap = bundleLanguageEncounterMap.get(
                    tempBundleDTO);
                // Check if the map for the current bundle already contains
                // the current locale code
                if (!localeCodeEncounterMap.containsKey(incompleteEncounter.getBundleLanguage())) {
                    // If not, add the language code and the current
                    // encounter to the map of the current bundle
                    List<EncounterDTO> encounterList = new ArrayList<>();
                    encounterList.add(encounterDTOMapper.apply(true, incompleteEncounter));
                    localeCodeEncounterMap.put(
                        incompleteEncounter.getBundleLanguage(),
                        encounterList);
                    bundleLanguageEncounterMap.put(
                        tempBundleDTO,
                        localeCodeEncounterMap);
                } else {
                    // Otherwise the map for the current bundle contains the
                    // current locale code.
                    // Add the incomplete encounter to the list for the
                    // bundle combined with the language code
                    bundleLanguageEncounterMap.get(tempBundleDTO)
                        .get(incompleteEncounter.getBundleLanguage())
                        .add(encounterDTOMapper.apply(
                            true,
                            incompleteEncounter));
                }
            }
        }

        // Add the map to the model
        model.addAttribute("bundleLanguageEncounterMap", bundleLanguageEncounterMap);
        model.addAttribute("hideProfile", Boolean.FALSE);
        return "mobile/survey/bundles";
    }

    /**
     * Controls the HTTP POST requests for the URL
     * <i>/mobile/survey/select</i>. Starts the questionnaire or returns to the
     * case number check page.<br> <br>
     *
     * @param bundleId                Id of the selected bundle
     * @param bundleLanguage          The language of the bundle
     * @param action                  (<i>required</i>)
     *                                <code>gotoCheck</code> Redirects to check
     *                                case number page. <br>
     *                                <code>startQuestionnaires</code> Starts
     *                                the questionnaire. <br>
     * @param guiLanguage             The language from the gui
     * @param incompleteEncounterUUID Chosen UUID of an incomplete {@link Encounter}
     * @param encounterDTO            The current {@link EncounterDTO}
     * @param result                  The result for validation of the {@link EncounterDTO}
     * @param session                 The current session object
     * @param model                   The model, which holds the information for the view
     * @return Redirect to the <i>mobile/survey/bundles</i> website
     */
    @RequestMapping(value = "/mobile/survey/select", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String selectBundle(
        @RequestParam(value = "bundleId", required = false) final Long bundleId,
        @RequestParam(value = "bundleLanguage", required = false) final String bundleLanguage,
        @RequestParam(value = "action", required = true) final String action,
        @RequestParam(value = "guiLanguage", required = true) final String guiLanguage,
        @RequestParam(value = "incompleteEncounterId", required = false) final String incompleteEncounterUUID,
        @ModelAttribute(value = "activeClinicId") final Long activeClinicId,
        @ModelAttribute(value = "encounterDTO") EncounterDTO encounterDTO,
        final BindingResult result, final HttpSession session, final Model model) {

        addClinicInfoToModel(model);

        // Recheck case number
        if (action.equalsIgnoreCase("gotoCheck")) {
            return showCheckCaseNumber(model, result);
        }
        if (action.equalsIgnoreCase("gotoClinicSelect")) {
            return "redirect:/mobile/survey/clinicSelect";
        }

        // Start the survey
        if (action.equalsIgnoreCase("startSurvey")) {
            // If the bunlde language is null or empty, stay on the current site
            if (bundleLanguage == null || bundleLanguage.isEmpty()) {
                return showBundles(null, null, encounterDTO, result, model);
            }
            // If an incomplete encounter is selected, load it from the db
            // and add it
            // to the model to override the default (new) encounter
            if (incompleteEncounterUUID != null
                && !incompleteEncounterUUID.isEmpty()) {
                EncounterDTO incompleteEncounterDTO = encounterDTOMapper.apply(true,
                    encounterDao.getElementByUUID(incompleteEncounterUUID));
                model.addAttribute(
                    "encounterDTO",
                    incompleteEncounterDTO);
                // Otherwise get the bundle and set it to the default (new)
                // encounter
            } else {
                Bundle bundle = bundleDao.getElementById(bundleId);

                // Make a bundleDTO from the bundle and add it to the
                // encounterDTO
                encounterDTO.setBundleDTO(bundleDTOMapper.apply(true, bundle));

                Encounter encounter = new Encounter();

                encounter.setStartTime(encounterDTO.getStartTime());
                encounter.setPatientID(encounterDTO.getPatientID());
                encounter.setCaseNumber(encounterDTO.getCaseNumber());
                encounter.setBundleLanguage(bundleLanguage);
                encounter.setClinic(clinicDao.getElementById(activeClinicId));
                encounterDao.merge(encounter);
                encounter.setBundle(bundle);
                // Merge the encounter object twice to merge the before updated
                // (bundle has just been set) encounter object,
                // otherwise the bundle object would not be merged
                encounterDao.merge(encounter);
                encounterDTO = encounterDTOMapper.apply(true, encounter);
                model.addAttribute(
                    "encounterDTO",
                    encounterDTO);
            }
            // If the selected bundle language is available for the gui, then
            // use this language
            if (!guiLanguage.isEmpty()) {
                return "redirect:/mobile/survey/questionnaire?lang=" + guiLanguage;
                // Otherwise use the selected bundle language for the bundle
                // and the current language for the user interface
            } else {
                Locale locale = LocaleContextHolder.getLocale();
                return "redirect:/mobile/survey/questionnaire?lang=" + locale;
            }

        }
        model.addAttribute("hideProfile", Boolean.FALSE);
        return showBundles(null, null, encounterDTO, result, model);
    }

    /**
     * If a user is logged in, he/she will be forwarded to the questionnaire
     *
     * @param model   The model, which holds the information for the view
     * @param session The current session object
     * @return Show the <i>mobile/survey/questionnaire</i> website
     */
    @RequestMapping(value = "/mobile/survey/questionnaire", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String showQuestionnaire(final Model model, final HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String username = user.getUsername();

        EncounterDTO encounterDTO = (EncounterDTO) session.getAttribute("encounterDTO");
        encounterDTO.removeDemographics();
        model.addAttribute("encounterDTO", encounterDTO);
        // Check all questionnaireDTOs for conditions and set the boolean
        for (BundleQuestionnaireDTO bundleQuestionnaireDTO : encounterDTO.getBundleDTO()
            .getBundleQuestionnaireDTOs()) {
            // Get the QuestionnaireDTO
            QuestionnaireDTO questionnaireDTO = bundleQuestionnaireDTO.getQuestionnaireDTO();
            // Get the boolean if this questionnaire has any conditions
            boolean hasConditionsAsTarget = conditionDao.isConditionTarget(
                questionnaireDao.getElementById(questionnaireDTO.getId()));
            // Set the boolean in the QuestionnaireDTO
            questionnaireDTO.setHasConditionsAsTarget(hasConditionsAsTarget);
        }

        if (configurationDao.isGlobalPinAuthEnabled()) {
            pinAuthorizationService.resetPinAuthForUser(user);
        } else {
            // invalidate the current session
            SecurityContextHolder.getContext().setAuthentication(null);
            session.invalidate();
        }

        return "mobile/survey/questionnaire";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/mobile/survey/test</i> Displays the {@link Bundle}, which should
     * be tested.
     *
     * @param bundleId           The id of the current {@link Bundle}.
     * @param model              The model, which holds the information for the view.
     * @param request            The request, which was sent from the client's browser.
     * @param redirectAttributes Stores the information for a redirect scenario.
     * @return The <i>bundle/fill</i> website.
     */
    @RequestMapping(value = "/mobile/survey/test", method = RequestMethod.GET)
    public String testBundle(@RequestParam(value = "id", required = false) final Long bundleId,
        final Model model, final HttpServletRequest request,
        final RedirectAttributes redirectAttributes) {

        Bundle bundle = bundleDao.getElementById(bundleId);
        if (bundle == null || bundle.getIsPublished()) {
            return "redirect:error/accessdenied";

        } else {
            EncounterDTO encounterDTO = new EncounterDTO(
                true,
                "test");
            encounterDTO.setBundleDTO(bundleDTOMapper.apply(true, bundle));
            model.addAttribute(
                "hideProfile",
                "false");
            model.addAttribute(
                "bundle",
                bundle);
            model.addAttribute(
                "encounterDTO",
                encounterDTO);

            // Check all questionnaireDTOs for conditions and set the boolean
            for (BundleQuestionnaireDTO bundleQuestionnaireDTO : encounterDTO.getBundleDTO()
                .getBundleQuestionnaireDTOs()) {
                // Get the QuestionnaireDTO
                QuestionnaireDTO questionnaireDTO = bundleQuestionnaireDTO.getQuestionnaireDTO();
                // Get the boolean if this questionnaire has any conditions
                boolean hasConditionsAsTarget = conditionDao.isConditionTarget(
                    questionnaireDao.getElementById(questionnaireDTO.getId()));
                // Set the boolean in the QuestionnaireDTO
                questionnaireDTO.setHasConditionsAsTarget(hasConditionsAsTarget);
            }

            if (encounterDTO.getBundleDTO().getBundleQuestionnaireDTOs().get(0) != null
                && !encounterDTO.getBundleDTO().getBundleQuestionnaireDTOs().isEmpty()
                && !encounterDTO.getBundleDTO().getBundleQuestionnaireDTOs().get(0)
                .getIsEnabled()) {
                model.addAttribute("noActiveQuestionnaire",
                    messageSource.getMessage("bundle.error" + ".noActiveQuestionnaires",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }

            return "mobile/survey/bundleTest";
        }
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/mobile/survey/test</i>. Starts the {@link Bundle}, which should
     * be tested.
     *
     * @param encounterDTO   The current {@link EncounterDTO}.
     * @param model          The model, which holds the information for the view.
     * @param guiLanguage    The language from the gui
     * @param bundleLanguage The language of the {@link Bundle}.
     * @return The <i>bundle/fill</i> website.
     */
    @RequestMapping(value = "/mobile/survey/test", method = RequestMethod.POST)
    public String testBundle(
        @ModelAttribute(value = "encounterDTO") @Valid final EncounterDTO encounterDTO,
        @RequestParam(value = "bundleLanguage", required = false) final String bundleLanguage,
        @RequestParam(value = "guiLanguage", required = true) final String guiLanguage,
        final Model model) {
        Bundle bundle = bundleDao.getElementById(encounterDTO.getBundleDTO().getId());

        if (bundle.getIsPublished()) {
            return "redirect:error/accessdenied";
        }

        encounterDTO.setBundleLanguage(bundleLanguage);
        model.addAttribute("encounterDTO", encounterDTO);
        // If the selected bundle language is available for the gui, then use
        // this language
        if (!guiLanguage.isEmpty()) {
            return "redirect:/mobile/survey/questionnairetest?lang=" + guiLanguage;
        } else {
            // Otherwise use the selected bundle language for the bundle and
            // the current language for the user interface
            Locale locale = LocaleContextHolder.getLocale();
            return "redirect:/mobile/survey/questionnairetest?lang=" + locale;
        }
    }

    /**
     * If a user is not logged in and want to test a bundle, he/she will be forwarded to the questionnaire.
     *
     * @param model   The model, which holds the information for the view
     * @param session The current session object
     * @return Show the <i>mobile/survey/questionnairetest</i> website
     */
    @RequestMapping(value = "/mobile/survey/questionnairetest", method = RequestMethod.GET)
    public String showQuestionaireTest(final Model model, final HttpSession session) {
        if (session.getAttribute("encounterDTO") == null) {
            return "redirect:error/accessdenied";
        }

        EncounterDTO encounterDTO = (EncounterDTO) session.getAttribute("encounterDTO");
        model.addAttribute("encounterDTO", encounterDTO);
        session.invalidate();
        return "mobile/survey/questionnaire";
    }

    /**
     * Controls the HTTP GET Request for the URL
     * <i>/mobile/survey/encounter</i>. Displays the given {@link Encounter},
     * which should be answered.
     *
     * @param uuid  Uuid of the encounter to create or to resume.
     * @param model The model which holds information for the view.
     * @return The <i>/mobile/survey/bundleScheduled</i> website.
     */
    @RequestMapping(value = "/mobile/survey/encounter", method = RequestMethod.GET)
    public String selectEncounterLanguage(
        @RequestParam(value = "hash", required = true) final String uuid, final Model model) {

        if (uuid == null || uuid.isEmpty() || encounterDao.getElementByUUID(uuid) == null
            || encounterDao.getElementByUUID(uuid).getEndTime() != null
            || encounterDao.getElementByUUID(uuid).getEncounterScheduled() == null) {
            return "encounter/completed";
        }

        EncounterDTO encounterDTO = encounterDTOMapper.apply(true, encounterDao.getElementByUUID(uuid));

        if (encounterDTO.getEndTime() == null && encounterDTO.getLastSeenQuestionId() == null) {
            //Set the startTime if the encounter started for the first time
            encounterDTO.setStartTime(new Timestamp(new Date().getTime()));
            Encounter encounter = encounterDao.getElementById(encounterDTO.getId());
            encounter.setStartTime(encounterDTO.getStartTime());
            encounterDao.merge(encounter);
        }

        model.addAttribute("hideProfile", "false");
        model.addAttribute("encounterDTO", encounterDTO);

        // If the encounter is resumed or there is only one selectable language
        if (encounterDTO.getLastSeenQuestionId() != null
            || encounterDTO.getBundleDTO().getAvailableLanguages().size() == 1) {
            if (encounterDTO.getBundleLanguage() == null) {
                String language = encounterDTO.getBundleDTO().getAvailableLanguages().get(0);
                for (String locale : LocaleHelper.getLocalesUsedInSurvey()) {
                    if (locale.contains(language) || locale.contains(language.substring(0, 2))) {
                        encounterDTO.setBundleLanguage(locale);
                        break;
                    }
                }

                if (encounterDTO.getBundleLanguage() == null) {
                    encounterDTO.setBundleLanguage(LocaleContextHolder.getLocale().toString());
                }
            }

            // Skip the language selection page
            return "redirect:/mobile/survey/questionnaireScheduled?lang="
                + encounterDTO.getBundleLanguage();
        }

        return "mobile/survey/bundleScheduled";
    }

    /**
     * Controls the HTTP POST Request for the URL
     * <i>/mobile/survey/schedule</i>. Starts or resumes the given
     * {@link Encounter}.
     *
     * @param bundleLanguage          The selected language the survey should run with.
     * @param guiLanguage             The language of the GUI.
     * @param incompleteEncounterUUID The uuid of a incompleted encounter, could be null if encounter hasn't started
     *                                before.
     * @param encounterDTO            The {@link EncounterDTO} object which holds information for the survey.
     * @param model                   The model which holds information for the view.
     * @return The <i>/mobile/survey/questionnaireScheduled/</i> website.
     */
    @RequestMapping(value = "/mobile/survey/schedule", method = RequestMethod.POST)
    public String startScheduledEncounter(
        @RequestParam(value = "bundleLanguage", required = false) final String bundleLanguage,
        @RequestParam(value = "guiLanguage", required = true) final String guiLanguage,
        @RequestParam(value = "incompleteEncounterId", required = false) final String incompleteEncounterUUID,
        @ModelAttribute(value = "encounterDTO") final EncounterDTO encounterDTO,
        final Model model) {

        // If the bundle language is null or empty, stay on the current site
        if (bundleLanguage == null || bundleLanguage.isEmpty()) {
            return "mobile/survey/encounter";
        }
        // If an incomplete encounter is selected, load it from the database
        // and add it
        // to the model to override the default (new) encounter
        if (incompleteEncounterUUID != null && !incompleteEncounterUUID.isEmpty()) {
            EncounterDTO incompleteEncounterDTO = encounterDTOMapper.apply(true,
                encounterDao.getElementByUUID(incompleteEncounterUUID));
            model.addAttribute(
                "encounterDTO",
                incompleteEncounterDTO);
            // Otherwise set the bundleLanguage and startTime to the
            // encounter and merge it
        } else {
            Encounter encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());
            encounter.setStartTime(new Timestamp(new Date().getTime()));
            encounter.setBundleLanguage(bundleLanguage);
            encounterDao.merge(encounter);
            model.addAttribute(
                "encounterDTO",
                encounterDTOMapper.apply(true, encounter));
        }

        // If the selected bundle language is available for the gui, then use
        // this language
        if (!guiLanguage.isEmpty()) {
            return "redirect:/mobile/survey/questionnaireScheduled?lang=" + guiLanguage;
            // Otherwise use the selected bundle language for the bundle and
            // the current language for the user interface
        } else {
            Locale locale = LocaleContextHolder.getLocale();
            return "redirect:/mobile/survey/questionnaireScheduled?lang=" + locale;
        }
    }

    /**
     * If a user is not logged in and want to answer an encounter, he/she will be forwarded to the questionnaire.
     *
     * @param encounterDTO The {@link EncounterDTO} object which holds information of the survey.
     * @param model        The model which holds information for the view.
     * @return The <i>mobile/survey/questionnaire</i> website.
     */
    @RequestMapping(value = "mobile/survey/questionnaireScheduled", method = RequestMethod.GET)
    public String showQuestionnaireScheduled(
        @ModelAttribute(value = "encounterDTO") final EncounterDTO encounterDTO,
        final Model model) {
        encounterDTO.removeDemographics();
        model.addAttribute("encounterDTO", encounterDTO);
        // Check all questionnaireDTOs for conditions and set the boolean
        for (BundleQuestionnaireDTO bundleQuestionnaireDTO : encounterDTO.getBundleDTO()
            .getBundleQuestionnaireDTOs()) {
            // Get the QuestionnaireDTO
            QuestionnaireDTO questionnaireDTO = bundleQuestionnaireDTO.getQuestionnaireDTO();
            // Get the boolean if this questionnaire has any conditions
            boolean hasConditionsAsTarget = conditionDao.isConditionTarget(
                questionnaireDao.getElementById(questionnaireDTO.getId()));
            // Set the boolean in the QuestionnaireDTO
            questionnaireDTO.setHasConditionsAsTarget(hasConditionsAsTarget);
        }
        return "mobile/survey/questionnaire";
    }

    /**
     * Controls the HTTP POST requests for the URL
     * <i>/mobile/survey/encounter</i>. Provides the ability to store/update an
     * {@link Encounter Encounter} object.
     *
     * @param encounterDTO The data transfer object containing the responses of the encounter.
     * @return Returns an empty String.
     */
    @RequestMapping(value = "/mobile/survey/encounter", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public @ResponseBody String updateEncounter(@RequestBody final EncounterDTO encounterDTO) {
        // If the encounter was sent from the client as isTest or
        // the given encounter Id is null
        if (encounterDTO.getIsTest() || encounterDTO.getId() == null) {
            // Do nothing
            return "";
        } else {
            Encounter encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());
            // If the attached bundle is not in test mode write the changes
            if (encounter != null && encounter.getBundle().getIsPublished()) {
                // Set the last seen question and the isCompleted flag from
                // the dto to the model
                encounter.setLastSeenQuestionId(encounterDTO.getLastSeenQuestionId());
                // Set active questionnaires
                encounter.setActiveQuestionnaires(encounterDTO.getActiveQuestionnaireIds());

                // Get all already existing responses
                Set<Response> existingResponses = new HashSet<>(encounter.getResponses());

                //Get a list with all existing answer IDs
                Set<Long> existingAnswerIds = new HashSet<>();
                for (Response response : existingResponses) {
                    existingAnswerIds.add(response.getAnswer().getId());
                }

                // Get a list with all new given answer IDs and fill it in
                // every step of the loop
                Set<Long> givenAnswerIds = new HashSet<>();

                // If there are any changes in the responses
                if (!existingResponses.equals(encounterDTO.getResponses())) {
                    for (ResponseDTO responseDTO : encounterDTO.getResponses()) {
                        // Add the AnswerId to the list of all given AnswerIds
                        givenAnswerIds.add(responseDTO.getAnswerId());

                        // Set the current answer
                        Answer currentAnswer = answerDao.getElementById(responseDTO.getAnswerId());

                        // If response is not enabled
                        if (!responseDTO.isEnabled()) {
                            // And response exists
                            if (existingAnswerIds.contains(responseDTO.getAnswerId())) {
                                // Delete it from answer and existing
                                // responses list
                                Response responseToDelete = responseDao.getResponseByAnswerInEncounter(
                                    responseDTO.getAnswerId(), encounter.getId());
                                currentAnswer.removeResponse(responseToDelete);
                                existingResponses.remove(responseToDelete);
                            }
                            questionnaireDao.merge(currentAnswer.getQuestion().getQuestionnaire());
                            continue;
                        }

                        // If the response already exists it must be updated
                        if (existingAnswerIds.contains(responseDTO.getAnswerId())) {
                            // Get current response from the existing responses
                            Response currentResponse = null;
                            for (Response response : existingResponses) {
                                if (response.getAnswer().getId()
                                    .equals(responseDTO.getAnswerId())) {
                                    currentResponse = response;
                                    break;
                                }
                            }
                            // Check to which type of question this response
                            // belongs and update the values if necessary
                            switch (currentAnswer.getQuestion().getQuestionType()) {
                                case SLIDER:
                                case NUMBER_CHECKBOX:
                                case NUMBER_INPUT:
                                    if (responseDTO.getValue() == null) {
                                        givenAnswerIds.remove(responseDTO.getAnswerId());
                                    } else if (currentResponse.getValue() == null
                                        || !currentResponse.getValue()
                                        .equals(responseDTO.getValue())) {
                                        currentResponse.setValue(responseDTO.getValue());
                                    }
                                    break;
                                case NUMBER_CHECKBOX_TEXT:
                                    if (responseDTO.getValue() == null && (
                                        responseDTO.getCustomtext() == null
                                            || responseDTO.getCustomtext().equals(""))) {
                                        givenAnswerIds.remove(responseDTO.getAnswerId());
                                    } else {
                                        // If new response is not null and
                                        // the existing response is null or
                                        // is not equal to the new one, set it
                                        if (responseDTO.getValue() != null && (
                                            currentResponse.getValue() == null
                                                || !currentResponse.getValue()
                                                .equals(responseDTO.getValue()))) {
                                            currentResponse.setValue(responseDTO.getValue());
                                            // If the new repsonse is null,
                                            // set the existing to null
                                        } else if (responseDTO.getValue() == null) {
                                            currentResponse.setValue(null);
                                        }
                                        // If the new customtext is not null
                                        // or empty and the existing
                                        // customtext is null or not equal to
                                        // the new one, set the new one
                                        if ((responseDTO.getCustomtext() != null
                                            && !responseDTO.getCustomtext().equals("")) && (
                                            currentResponse.getCustomtext() == null
                                                || currentResponse.getCustomtext().equals("")
                                                || !currentResponse.getCustomtext()
                                                .equals(responseDTO.getCustomtext()))) {
                                            currentResponse.setCustomtext(
                                                responseDTO.getCustomtext());
                                            // If the new custom text is null
                                            // or empty, set the existing
                                            // custom text to null
                                        } else if (responseDTO.getCustomtext() == null
                                            || responseDTO.getCustomtext().equals("")) {
                                            currentResponse.setCustomtext("");
                                        }
                                    }
                                    break;
                                case FREE_TEXT:
                                    if (responseDTO.getCustomtext() == null
                                        || responseDTO.getCustomtext().equals("")) {
                                        givenAnswerIds.remove(responseDTO.getAnswerId());
                                    } else if (currentResponse.getCustomtext() == null
                                        || !currentResponse.getCustomtext()
                                        .equals(responseDTO.getCustomtext())) {
                                        currentResponse.setCustomtext(responseDTO.getCustomtext());
                                    }
                                    break;
                                case DATE:
                                    if (responseDTO.getDate() == null) {
                                        givenAnswerIds.remove(responseDTO.getAnswerId());
                                    } else if (currentResponse.getDate() == null
                                        || !currentResponse.getDate()
                                        .equals(responseDTO.getDate())) {
                                        currentResponse.setDate(responseDTO.getDate());
                                    }
                                    break;
                                case IMAGE:
                                    if (responseDTO.getPointsOnImage() == null
                                        || responseDTO.getPointsOnImage().isEmpty()) {
                                        givenAnswerIds.remove(responseDTO.getAnswerId());
                                    } else {
                                        List<PointOnImage> pointsOnImage = new ArrayList<>();
                                        for (PointOnImageDTO currentPointOnImageDTO : responseDTO.getPointsOnImage()) {
                                            PointOnImage pointOnImage = currentPointOnImageDTO.toPointOnImage();
                                            pointOnImage.setResponse(currentResponse);
                                            pointsOnImage.add(pointOnImage);
                                        }
                                        currentResponse.setPointsOnImage(pointsOnImage);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            // If the response does not exist create a new one
                            Response response = new Response(currentAnswer, encounter);

                            if (responseDTO.getCustomtext() != null) {
                                response.setCustomtext(responseDTO.getCustomtext());
                            }

                            if (responseDTO.getValue() != null) {
                                response.setValue(responseDTO.getValue());
                            }

                            if (responseDTO.getDate() != null) {
                                response.setDate(responseDTO.getDate());
                            }

                            if (responseDTO.getPointsOnImage() != null) {

                                List<PointOnImage> pointsOnImage = new ArrayList<>();
                                for (PointOnImageDTO currentPointOnImageDTO : responseDTO.getPointsOnImage()) {
                                    PointOnImage pointOnImage = currentPointOnImageDTO.toPointOnImage();
                                    pointOnImage.setResponse(response);
                                    pointsOnImage.add(pointOnImage);
                                }
                                response.setPointsOnImage(pointsOnImage);
                            }
                            existingResponses.add(response);
                        }
                        questionnaireDao.merge(currentAnswer.getQuestion().getQuestionnaire());
                    }

                    // Get all existing responses that were not in the DTO
                    existingAnswerIds.removeAll(givenAnswerIds);
                    // And remove them from the answer and the list of
                    // existing responses
                    for (Long id : existingAnswerIds) {
                        Answer answer = answerDao.getElementById(id);
                        Response responseToDelete = responseDao.getResponseByAnswerInEncounter(id,
                            encounter.getId());
                        answer.removeResponse(responseToDelete);
                        existingResponses.remove(responseToDelete);
                        questionnaireDao.merge(answer.getQuestion().getQuestionnaire());
                    }

                    // Update the response list of the encounter and merge it
                    encounter.setResponses(existingResponses);
                    encounterDao.merge(encounter);
                }

                // If the encounter is finished
                if (encounterDTO.getIsCompleted()) {
                    // Wait initially 5 seconds for a possibly runnig export
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException ex) {
                        LOGGER.debug(
                            "The waiting of the exporting thread " + "was" + " interrupted");
                    }

                    // Get all export templates that should be done
                    Set<ExportTemplate> remainingExportTemplates = new HashSet<>();
                    for (BundleQuestionnaire bundleQuestionnaire : encounter.getBundle()
                        .getBundleQuestionnaires()) {
                        remainingExportTemplates.addAll(bundleQuestionnaire.getExportTemplates());
                    }
                    int loopCounter = 0;

                    // If the loop counter reaches 15 the exports has waited
                    // the maximum time of 30 seconds
                    while (loopCounter <= 15) {
                        // Renew the encounter from database to get the newly
                        // exported templates
                        encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());
                        // Remove all (successfully) exported templates from
                        // remaining templates
                        for (EncounterExportTemplate encounterExportTemplate : encounter.getEncounterExportTemplates()) {
                            if (!(
                                encounterExportTemplate.getExportTemplate().getExportTemplateType()
                                    == ExportTemplateType.ODM) || !(
                                encounterExportTemplate.getExportStatus()
                                    == ExportStatus.FAILURE)) {
                                remainingExportTemplates.remove(
                                    encounterExportTemplate.getExportTemplate());
                            }
                        }
                        // If there are remaining export templates wait 2
                        // seconds (maximal 15 times) for a possibly running
                        // export (connection timeout is 30 seconds)
                        if (!remainingExportTemplates.isEmpty()) {
                            try {
                                Thread.sleep(2000L);
                                loopCounter++;
                            } catch (InterruptedException ex) {
                                LOGGER.debug(
                                    "The waiting of the exporting " + "thread was interrupted");
                            }
                        } else {
                            break;
                        }
                    }

                    // Update the encounter from database
                    encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());
                    encounter.setEndTime(new Timestamp(System.currentTimeMillis()));
                    // Remove all (successfully) exported templates from
                    // remaining templates
                    for (EncounterExportTemplate encounterExportTemplate : encounter.getEncounterExportTemplates()) {
                        if (!(encounterExportTemplate.getExportTemplate().getExportTemplateType()
                            == ExportTemplateType.ODM) || !(
                            encounterExportTemplate.getExportStatus() == ExportStatus.FAILURE)) {
                            remainingExportTemplates.remove(
                                encounterExportTemplate.getExportTemplate());
                        }
                    }

                    // And export the templates which were not yet exported
                    for (ExportTemplate exportTemplate : remainingExportTemplates) {
                        if (encounter.getActiveQuestionnaires()
                            .contains(exportTemplate.getQuestionnaire().getId())) {
                            encounterExporter.export(encounter, exportTemplate);
                        }
                    }
                    encounterDao.merge(encounter);
                }

                Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
                if (encounter.getPatientID() != null) {
                    patientAttributes.add(AuditPatientAttribute.PATIENT_ID);
                }
                if (encounter.getResponses() != null && !encounter.getResponses().isEmpty()) {
                    patientAttributes.add(AuditPatientAttribute.TREATMENT_DATA);
                }
                auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
                    "updateEncounter(EncounterDTO)", encounter.getCaseNumber(), patientAttributes,
                    AuditEntryActionType.WRITE);
            }
            return "";
        }
    }

    /**
     * Controls the HTTP POST requests for the URL
     * <i>/mobile/survey/encounter</i>. Provides the ability to get the
     * evaluated scores of an {@link Encounter Encounter} object.
     *
     * @param encounterUuid The UUID of the encounter
     * @return The evaluated scores of an encounter.
     */
    @RequestMapping(value = "/mobile/survey/scores", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> getActivatedScores(
        @RequestParam(value = "encounterUuid", required = true) final String encounterUuid) {
        Map<String, String> activatedScores = new LinkedHashMap<>();
        // If the given encounter Uuid is null
        if (encounterUuid == null || encounterUuid.equals("")) {
            // Do nothing
            return activatedScores;
        }
        Encounter encounter = encounterDao.getElementByUUID(encounterUuid);
        for (BundleQuestionnaire bundleQuestionnaire : encounter.getBundle()
            .getBundleQuestionnaires()) {
            if (bundleQuestionnaire.getShowScores()) {
                activatedScores.put(bundleQuestionnaire.getQuestionnaire().getLocalizedDisplayName()
                    .getOrDefault(LocaleContextHolder.getLocale().toString(),
                        bundleQuestionnaire.getQuestionnaire().getName()), null);
                List<Score> sortedScores = new ArrayList(
                    bundleQuestionnaire.getQuestionnaire().getScores());
                Collections.sort(sortedScores,
                    (Score o1, Score o2) -> o1.getName().compareTo(o2.getName()));
                for (Score score : sortedScores) {
                    if (score.evaluate(encounter) != null) {
                        activatedScores.put(score.getName(), score.evaluate(encounter).toString());
                    } else {
                        activatedScores.put(score.getName(),
                            messageSource.getMessage("encounter.export" + ".scoreNotCalculable",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    }
                }
            }
        }
        return activatedScores;
    }

    /**
     * Controls the HTTP POST requests for the URL
     * <i>/mobile/survey/finishQuestionnaire</i>. Stores/Updates the given
     * {@link Encounter} and exports the {@link Questionnaire} identified by the given questionnaireId
     *
     * @param encounterDTO    The data transfer object containing the responses of the encounter.
     * @param questionnaireId the id of the questionnaire that has been finished and can be exported.
     */
    @RequestMapping(value = "/mobile/survey/finishQuestionnaire", method = RequestMethod.POST)
    @ResponseBody
    public void finishQuestionnaire(
        @RequestParam(value = "questionnaireId", required = true) final Long questionnaireId,
        @RequestBody final EncounterDTO encounterDTO) {
        if (!encounterDTO.getIsTest() && encounterDTO.getId() != null) {
            Encounter encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());
            // If the attached bundle is not in test mode write the changes
            if (encounter != null && encounter.getBundle().getIsPublished()) {
                // Store the responses to the database
                updateEncounter(encounterDTO);
                // Refresh encounter from database after storing of responses
                encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());
                Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireId);
                if (encounter.getActiveQuestionnaires().contains(questionnaire.getId())) {
                    encounterExporter.export(encounter, questionnaire);
                }
            }
        }
    }

    /**
     * @param pseudonym The generated pseudonym.
     * @param session   The current session object.
     * @return {@link ResponseEntity} object containing the current HttpStatus code.
     */
    @RequestMapping(value = "/mobile/survey/pseudonym", method = RequestMethod.POST)
    public ResponseEntity<String> registerPseuodnym(
        @RequestParam(value = "pseudonym", required = true) final String pseudonym,
        final HttpSession session) {

        EncounterDTO encounterDTO = (EncounterDTO) session.getAttribute("encounterDTO");

        encounterDTO.removeDemographics();
        encounterDTO.setCaseNumber(pseudonym);
        encounterDTO.setPatientID(null);

        session.setAttribute("encounterDTO", encounterDTO);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    /**
     * Returns the case number type from the {@link ConfigurationDao} by using the name of this class and the
     * appropriate attribute name.
     *
     * @return The configured case number type string.
     */
    private String getCaseNumberType() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            caseNumberTypeProperty, className);
        return configuration.getValue();
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication()
            .getPrincipal();
    }

    private void addClinicInfoToModel(Model model) {
        User currentUser = getCurrentUser();
        List<Clinic> assignedClinics = clinicService.getAssignedClinics(currentUser);

        List<ClinicDTO> clinicDTOs = clinicService.transformClinicsToDTOs(true, assignedClinics);
        model.addAttribute("clinicDTOs", clinicDTOs);

        if (!clinicDTOs.isEmpty()) {
            if (model.getAttribute("activeClinicDTO") == null && !assignedClinics.isEmpty()) {

                Long lastSelectedClinicId = currentUser.getLastSelectedClinicId();
                Clinic assignedClinic = clinicService.getClinicByIdFromList(assignedClinics, lastSelectedClinicId);

                ClinicDTO clinicDTO;
                if (assignedClinic != null) {
                    clinicDTO = clinicDTOMapper.apply(assignedClinic);
                } else {
                    clinicDTO = clinicDTOMapper.apply(assignedClinics.get(0));
                }
                model.addAttribute("activeClinicDTO", clinicDTO);
            } else {
                ClinicDTO activeClinicDTO = (ClinicDTO) model.getAttribute("activeClinicDTO");
                final Long activeClinicId = activeClinicDTO.getId();
                //Restore all activeClinicDTO information
                activeClinicDTO = clinicDTOs.stream().filter(clinicDTO -> clinicDTO.getId().equals(activeClinicId))
                    .findFirst().get();
                model.addAttribute("activeClinicDTO", activeClinicDTO);
            }
        }
    }

    private void addClinicInfoToModelForKnownId(Model model, Long clinicId) {
        User currentUser = getCurrentUser();
        List<Clinic> assignedClinics = clinicService.getAssignedClinics(currentUser);

        List<ClinicDTO> clinicDTOs = clinicService.transformClinicsToDTOs(true, assignedClinics);
        ClinicDTO activeClinicDTO = clinicDTOs.stream().filter(
            clinicDTO -> clinicDTO.getId().equals(clinicId)
        ).findFirst().get();
        model.addAttribute("activeClinicDTO", activeClinicDTO);
        model.addAttribute("clinicDTOs", clinicDTOs);
    }


    private PatientDataRetriever getPatientRetriever(Long clinicId) {
        PatientDataRetriever patientDataRetriever;
        patientDataRetriever = (PatientDataRetriever) appContext.getBean(
            "clinicPatientDataRetriever", clinicId
        );
        return patientDataRetriever;
    }
}
