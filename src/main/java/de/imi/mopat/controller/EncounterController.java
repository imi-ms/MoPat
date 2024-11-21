package de.imi.mopat.controller;

import de.imi.mopat.cron.EncounterScheduledExecutor;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.model.BundleDTOMapper;
import de.imi.mopat.helper.model.EncounterScheduledDTOMapper;
import de.imi.mopat.helper.model.EncounterDTOMapper;
import de.imi.mopat.io.EncounterExporter;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleClinic;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;
import de.imi.mopat.model.user.User;
import de.imi.mopat.validator.EncounterScheduledDTOValidator;
import jakarta.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 */
@Controller
public class EncounterController {

    @Autowired
    private ApplicationMailer applicationMailer;
    @Autowired
    private AuditEntryDao auditEntryDao;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired(required = false)
    private EncounterExporter encounterExporter;
    @Autowired
    private EncounterScheduledDao encounterScheduledDao;
    @Autowired
    private EncounterScheduledDTOValidator encounterScheduledDTOValidator;
    @Autowired
    private EncounterScheduledExecutor encounterScheduledExecutor;
    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private BundleDTOMapper bundleDTOMapper;
    @Autowired
    private EncounterScheduledDTOMapper encounterScheduledDTOMapper;
    @Autowired
    private EncounterDTOMapper encounterDTOMapper;

    /**
     * Collects all emails to set for the encounterScheduledDTOs replyMails.
     *
     * @return The map containing the bundles id and the emails appropriate to the bundle
     */
    public Map<Long, Set<String>> getReplyMails() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
            .getPrincipal();

        List<Bundle> bundles = bundleDao.getAllElements();
        // Initialize list with available email addresses
        // to provide those ones for reply mail for scheduled encounter
        // no address or the current user's address should be available
        Map<Long, Set<String>> replyMailMap = new LinkedHashMap<>();

        for (Bundle bundle : bundles) {
            Set<String> emails = new HashSet<>();
            emails.add(currentUser.getEmail());
            // Add only those bundles which are published and
            // assigned to at least one clinic
            if (bundle.getIsPublished() && bundle.usedInClinics()) {

                // Provide those mail addresses that belongs to clinics the
                // current user has access to
                for (BundleClinic bundleClinic : bundle.getBundleClinics()) {
                    if (!(bundleClinic.getClinic().getEmail() == null || bundleClinic.getClinic()
                        .getEmail().isEmpty())) {
                        emails.add(bundleClinic.getClinic().getEmail());
                    }
                }
                replyMailMap.put(bundle.getId(), emails);
            }
        }
        return replyMailMap;
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/encounter/list</i>. Shows the list of
     * encounter.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>encounter/list</i> website.
     */
    @GetMapping(value = "/encounter/list")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String listEncounter(final Model model) {
        // Initialize containers
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>(
            Arrays.asList(
                AuditPatientAttribute.CASE_NUMBER,
                AuditPatientAttribute.EMAIL_ADDRESS
            )
        );

        Set<String> caseNumbers = new HashSet<>();
        List<EncounterDTO> encounterDTOs = new ArrayList<>();
        List<EncounterScheduledDTO> encounterScheduledDTOs = new ArrayList<>();
        Set<String> encounterScheduledJSONSet = new HashSet<>();

        // Fetch all required elements at once
        List<Bundle> bundles = bundleDao.getAllElements();
        List<EncounterScheduled> allEncounterScheduled = encounterScheduledDao.getAllElements();

        // Group EncounterScheduled by Bundle ID for quick access
        Map<Long, List<EncounterScheduled>> encounterScheduledByBundle = allEncounterScheduled.stream()
            .collect(Collectors.groupingBy(e -> e.getBundle().getId()));

        // Process each bundle
        bundles.forEach(bundle -> {
            // Add encounters to DTOs list if they have no scheduled encounters, collect case numbers
            bundle.getEncounters().forEach(encounter -> {
                caseNumbers.add(encounter.getCaseNumber());
                if (encounter.getEncounterScheduled() == null) {
                    encounterDTOs.add(encounterDTOMapper.apply(false, encounter));
                }
            });

            // Add EncounterScheduled to DTOs based on already grouped data by bundle
            encounterScheduledByBundle.getOrDefault(bundle.getId(), Collections.emptyList()).stream()
                .map(encounterScheduledDTOMapper)
                .forEach(dto -> {
                    encounterScheduledDTOs.add(dto);
                    encounterScheduledJSONSet.add(dto.getJSON());
                });
        });

        // Sort the encounters and scheduled encounters by start date using stream sorted method
        encounterDTOs.sort(Comparator.comparing(EncounterDTO::getStartTime));
        encounterScheduledDTOs.sort(Comparator.comparing(EncounterScheduledDTO::getStartDate));

        model.addAttribute("allEncounters", encounterDTOs);
        model.addAttribute("allEncounterScheduled", encounterScheduledDTOs);
        model.addAttribute("encounterScheduledDTOs", encounterScheduledJSONSet);
        auditEntryDao.writeAuditEntries(this.getClass().getSimpleName(), "listEncounter(Model)",
            caseNumbers, patientAttributes, AuditEntryActionType.READ);

        return "encounter/list";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/encounter/show</i>. Used to show a specific
     * encounter with inherent questionnaires and associated export templates
     *
     * @param encounterId The id from the specific encounter.
     * @param model       The model, which holds the information for the view.
     * @return The <i>encounter/show</i> website.
     */
    @GetMapping(value = "/encounter/show")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String showEncounter(@RequestParam(required = true, value = "id") final Long encounterId,
        final Model model) {
        Encounter encounter = encounterDao.getElementById(encounterId);

        if (encounter == null) {
            return "redirect:/encounter/list";
        }

        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
        patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
        auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
            "showEncounter(" + encounterId + ", model)", encounter.getCaseNumber(),
            patientAttributes, AuditEntryActionType.READ);
        model.addAttribute("encounter", encounter);
        return "encounter/show";
    }

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/encounter/exporttemplate</i>. Used for manual export. Export the
     * chosen export template and encounter.
     *
     * @param encounterId The id from the specific encounter.
     * @param templateId  The id from the template to be exported.
     * @param model       The model, which holds the information for the view.
     * @return The <i>encounter/show</i> website.
     */
    @GetMapping(value = "/encounter/exporttemplate")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String exportEncounterTemplate(
        @RequestParam(required = true, value = "id") final Long encounterId,
        @RequestParam(required = true, value = "templateid") final Long templateId,
        final Model model) {
        Encounter encounter = encounterDao.getElementById(encounterId);
        ExportTemplate exportTemplate = exportTemplateDao.getElementById(templateId);
        encounterExporter.export(encounter, exportTemplate, true);
        model.addAttribute("encounter", encounter);
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
        patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
        patientAttributes.add(AuditPatientAttribute.TREATMENT_DATA);
        auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
            "exportEncounterTemplate(" + encounterId + "templateId, model)",
            encounter.getCaseNumber(), patientAttributes, AuditEntryActionType.WRITE);
        return "redirect:/encounter/show?id=" + encounterId;
    }

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/encounter/schedule</i>. It is used to schedule encounter series.
     *
     * @param id        The id from the specific {@link EncounterScheduled}.
     * @param pseudonym The casenumber/pseudonym of the new {@link EncounterScheduled}.
     * @param email     The contact email address of the new {@link EncounterScheduled}.
     * @param model     The model, which holds the information for the view.
     * @return The <i>encounter/show</i> website.
     */
    @GetMapping(value = "/encounter/schedule")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String scheduleEncounter(@RequestParam(value = "id", required = false) final Long id,
        @RequestParam(value = "pseudonym", required = false) final String pseudonym,
        @RequestParam(value = "email", required = false) final String email, final Model model) {

        EncounterScheduledDTO encounterScheduledDTO = new EncounterScheduledDTO();
        if (id != null && id > 0) {
            EncounterScheduled encounterScheduled = encounterScheduledDao.getElementById(id);
            if (encounterScheduled != null) {
                encounterScheduledDTO = encounterScheduledDTOMapper.apply(
                    encounterScheduled);
                Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
                patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
                patientAttributes.add(AuditPatientAttribute.EMAIL_ADDRESS);
                patientAttributes.add(AuditPatientAttribute.FIRST_NAME);
                patientAttributes.add(AuditPatientAttribute.LAST_NAME);
                patientAttributes.add(AuditPatientAttribute.DATE_OF_BIRTH);
                auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
                    "scheduleEncounter(id, " + "pseudonym, email, " + "model)",
                    encounterScheduled.getCaseNumber(), patientAttributes,
                    AuditEntryActionType.READ);
            }
        } else {
            if (pseudonym != null && !pseudonym.isEmpty()) {
                encounterScheduledDTO.setCaseNumber(pseudonym);
            }
            if (email != null && !email.isEmpty()) {
                encounterScheduledDTO.setEmail(email);
            }
        }

        List<Bundle> bundles = bundleDao.getAllElements();
        List<BundleDTO> bundleDTOs = new ArrayList<>();

        for (Bundle bundle : bundles) {
            BundleDTO bundleDTO = bundleDTOMapper.apply(false, bundle);
            // Add only those bundles which are published
            // and assigned to at least one clinic
            if (bundle.getIsPublished() && bundle.usedInClinics()) {
                bundleDTOs.add(bundleDTO);
            }
        }
        encounterScheduledDTO.setReplyMails(getReplyMails());

        model.addAttribute("encounterScheduledDTO", encounterScheduledDTO);
        model.addAttribute("bundleDTOs", bundleDTOs);
        model.addAttribute("encounterScheduledSerialTypeList",
            new ArrayList<>(Arrays.asList(EncounterScheduledSerialType.values())));
        model.addAttribute("pseudonymizationIsActive",
            configurationDao.isPseudonymizationServiceActivated());
        return "encounter/schedule";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/encounter/schedule</i>. Creates a new
     * {@link EncounterScheduled} object or edits an existing one. Validates and persists the given
     * object.
     *
     * @param encounterScheduledDTO The {@link EncounterScheduledDTO} object from the view.
     * @param result                :The result for validation of the {@link EncounterScheduledDTO}
     *                              object.
     * @param action                :The name of the submit button which has been clicked.
     * @param model                 :The model, which holds the information for the view.
     * @param redirectAttributes    :Stores the information for a redirect scenario.
     * @return Redirect to the <i>encounter/list</i> website.
     */
    @PostMapping(value = "/encounter/schedule")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String saveScheduledEncounter(@RequestParam final String action,
        @ModelAttribute("encounterScheduledDTO") @Valid final EncounterScheduledDTO encounterScheduledDTO,
        final BindingResult result, final Model model,
        final RedirectAttributes redirectAttributes) {

        if (action.equalsIgnoreCase("cancel")) {
            return "redirect:/encounter/list?series=true";
        }

        switch (encounterScheduledDTO.getEncounterScheduledSerialType()) {
            case UNIQUELY:
                encounterScheduledDTO.setRepeatPeriod(null);
                encounterScheduledDTO.setEndDate(null);
                break;
            case WEEKLY:
                encounterScheduledDTO.setRepeatPeriod(7);
                break;
            case MONTHLY:
                encounterScheduledDTO.setRepeatPeriod(30);
                break;
            default:
                break;
        }

        encounterScheduledDTO.setReplyMails(getReplyMails());

        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);

        if (result.hasErrors()) {
            List<Bundle> bundles = bundleDao.getAllElements();
            List<BundleDTO> bundleDTOs = new ArrayList<>();
            for (Bundle bundle : bundles) {
                bundleDTOs.add(bundleDTOMapper.apply(true, bundle));
            }
            model.addAttribute("bundleDTOs", bundleDTOs);
            model.addAttribute("encounterScheduledSerialTypeList",
                new ArrayList<>(Arrays.asList(EncounterScheduledSerialType.values())));
            model.addAttribute("pseudonymizationIsActive",
                configurationDao.isPseudonymizationServiceActivated());
            return "encounter/schedule";
        }

        EncounterScheduled encounterScheduled = null;
        Bundle bundle = bundleDao.getElementById(encounterScheduledDTO.getBundleDTO().getId());
        if (encounterScheduledDTO.getId() == null) {
            encounterScheduled = new EncounterScheduled(encounterScheduledDTO.getCaseNumber(),
                bundle, encounterScheduledDTO.getStartDate(),
                encounterScheduledDTO.getEncounterScheduledSerialType(),
                encounterScheduledDTO.getEndDate(), encounterScheduledDTO.getRepeatPeriod(),
                encounterScheduledDTO.getEmail(), encounterScheduledDTO.getLocale().toString(),
                encounterScheduledDTO.getPersonalText(), encounterScheduledDTO.getReplyMail());
        } else {
            encounterScheduled = encounterScheduledDao.getElementById(
                encounterScheduledDTO.getId());
            encounterScheduled.setCaseNumber(encounterScheduledDTO.getCaseNumber());
            encounterScheduled.setBundle(bundle);
            encounterScheduled.setEmail(encounterScheduledDTO.getEmail());
            encounterScheduled.setEncounterScheduledSerialType(
                encounterScheduledDTO.getEncounterScheduledSerialType());
            encounterScheduled.setStartDate(encounterScheduledDTO.getStartDate());
            encounterScheduled.setEndDate(encounterScheduledDTO.getEndDate());
            encounterScheduled.setRepeatPeriod(encounterScheduledDTO.getRepeatPeriod());
            encounterScheduled.setLocale(encounterScheduledDTO.getLocale().toString());
            encounterScheduled.setPersonalText(encounterScheduledDTO.getPersonalText());
            if (encounterScheduledDTO.getReplyMail().equalsIgnoreCase("empty")) {
                encounterScheduled.setReplyMail(null);
            } else {
                encounterScheduled.setReplyMail(encounterScheduledDTO.getReplyMail());
            }
        }

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        Calendar startDay = Calendar.getInstance();
        startDay.setTime(encounterScheduledDTO.getStartDate());

        // If the scheduled encounter is scheduled for today,
        // possibly send the notification mail immediately
        if (startDay.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
            Calendar lastExecutionTime = Calendar.getInstance();

            if (encounterScheduledExecutor.getLastExecutionTime() != null) {
                lastExecutionTime.setTime(encounterScheduledExecutor.getLastExecutionTime());
            }

            Calendar nextExecutionTime = Calendar.getInstance();
            nextExecutionTime.setTime(encounterScheduledExecutor.getNextExecutionTime());

            // When the last execution time of the encounterScheduledExecutor
            // is not known
            // and the next run will be tomorrow or
            // if the last execution time was today,
            // we have to send the notification email immediately.
            if ((encounterScheduledExecutor.getLastExecutionTime() == null
                && now.get(Calendar.DAY_OF_MONTH) != nextExecutionTime.get(Calendar.DAY_OF_MONTH))
                || (encounterScheduledExecutor.getLastExecutionTime() != null
                && now.get(Calendar.DAY_OF_MONTH) == lastExecutionTime.get(
                Calendar.DAY_OF_MONTH))) {
                // Get date today at midnight to set the encounter's time
                now.set(Calendar.MILLISECOND, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.HOUR_OF_DAY, 0);
                Date today = now.getTime();

                Encounter encounter = new Encounter();
                encounter.setEncounterScheduled(encounterScheduled);
                encounter.setBundle(bundle);
                bundle.addEncounter(encounter);
                encounter.setCaseNumber(encounterScheduled.getCaseNumber());
                encounter.setStartTime(new Timestamp(today.getTime()));
                if (encounter.sendMail(applicationMailer, messageSource,
                    configurationDao.getBaseURL())) {
                    redirectAttributes.addFlashAttribute("success",
                        messageSource.getMessage("encounterScheduled.mail.success", new Object[]{},
                            LocaleContextHolder.getLocale()));
                } else {
                    String failMessage = messageSource.getMessage("encounterScheduled.mail.fail",
                        new Object[]{}, LocaleContextHolder.getLocale());
                    if (encounter.getEncounterScheduled().getMailStatus() != null
                        && encounter.getEncounterScheduled().getMailStatus()
                        .equals(EncounterScheduledMailStatus.ADDRESS_REJECTED)) {
                        encounterScheduled.setMailStatus(
                            EncounterScheduledMailStatus.ADDRESS_REJECTED);
                        failMessage = messageSource.getMessage(
                            "encounterScheduled.mail.invalidMail",
                            new Object[]{encounterScheduled.getEmail()},
                            LocaleContextHolder.getLocale());
                    }
                    redirectAttributes.addFlashAttribute("failure", failMessage);
                }
            }
        }
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
        patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
        patientAttributes.add(AuditPatientAttribute.EMAIL_ADDRESS);
        patientAttributes.add(AuditPatientAttribute.FIRST_NAME);
        patientAttributes.add(AuditPatientAttribute.LAST_NAME);
        patientAttributes.add(AuditPatientAttribute.DATE_OF_BIRTH);
        auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
            "saveScheduledEncounter(encounterScheduledDTO, result, model," + " redirectAttributes)",
            encounterScheduled.getCaseNumber(), patientAttributes, AuditEntryActionType.WRITE);
        encounterScheduledDao.merge(encounterScheduled);
        bundleDao.merge(bundle);

        return "redirect:/encounter/list";
    }

    /**
     * Controls the HTTP GET Request for the URL <i>/encounter/sendEmail</i>. Sends a remind mail
     * for a given {@link Encounter} object to the patient.
     *
     * @param encounterId        :The id of the {@link Encounter} object.
     * @param model              :The model, which holds the information for the view.
     * @param redirectAttributes Stores the information for a redirect scenario.
     * @return The <i>encounter/list</i> website.
     */
    @RequestMapping(value = "/encounter/sendEmail")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String sendEMail(@RequestParam(required = true, value = "id") final Long encounterId,
        final Model model, final RedirectAttributes redirectAttributes) {
        Encounter encounter = encounterDao.getElementById(encounterId);

        if (encounter != null && encounter.sendMail(applicationMailer, messageSource,
            configurationDao.getBaseURL())) {
            redirectAttributes.addFlashAttribute("success",
                messageSource.getMessage("encounterScheduled.mail.success", new Object[]{},
                    LocaleContextHolder.getLocale()));
        } else {
            String failMessage = messageSource.getMessage("encounterScheduled.mail.fail",
                new Object[]{}, LocaleContextHolder.getLocale());
            if (encounter != null && encounter.getEncounterScheduled() != null
                && encounter.getEncounterScheduled().getMailStatus() != null
                && encounter.getEncounterScheduled().getMailStatus()
                .equals(EncounterScheduledMailStatus.ADDRESS_REJECTED)) {
                failMessage = messageSource.getMessage("encounterScheduled.mail.invalidMail",
                    new Object[]{encounter.getEncounterScheduled().getEmail()},
                    LocaleContextHolder.getLocale());
            }
            redirectAttributes.addFlashAttribute("failure", failMessage);
        }

        if (encounter != null) {
            Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
            patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
            patientAttributes.add(AuditPatientAttribute.EMAIL_ADDRESS);
            auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
                "sendEMail(" + encounterId + "model, redirectAttributes)",
                encounter.getCaseNumber(), patientAttributes, AuditEntryActionType.SENT);
            encounterDao.merge(encounter);
            encounterScheduledDao.merge(encounter.getEncounterScheduled());
        }

        return "redirect:/encounter/list";
    }

    /**
     * Controls the HTTP GET Request for the URL
     * <i>/encounter/toggleMailStatus</i>. Enables a registered user to cancel
     * the mail notification for a given {@link EncounterScheduled} scheduled encounter.
     *
     * @param encounterScheduledId The id of the {@link EncounterScheduled} object whose mail
     *                             reminder has to be toggled.
     * @param redirectAttributes   :Stores the information for a redirect scenario.
     * @return Redirect to the <i>/encounter/list</i> website.
     */
    @RequestMapping(value = "/encounter/toggleMailStatus")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String toggleMailStatus(
        @RequestParam(required = true, value = "id") final Long encounterScheduledId,
        final RedirectAttributes redirectAttributes) {
        EncounterScheduled encounterScheduled = encounterScheduledDao.getElementById(
            encounterScheduledId);

        if (encounterScheduled != null) {
            switch (encounterScheduled.getMailStatus()) {
                case ACTIVE:
                    encounterScheduled.setMailStatus(
                        EncounterScheduledMailStatus.DEACTIVATED_ENCOUNTER_MANAGER);
                    break;
                case DEACTIVATED_ENCOUNTER_MANAGER:
                    encounterScheduled.setMailStatus(EncounterScheduledMailStatus.ACTIVE);
                    break;
                case DEACTIVATED_PATIENT:
                    if (encounterScheduled.sendReactivationMail(applicationMailer, messageSource,
                        configurationDao.getBaseURL())) {
                        encounterScheduled.setMailStatus(
                            EncounterScheduledMailStatus.CONSENT_PENDING);
                        redirectAttributes.addFlashAttribute("success",
                            messageSource.getMessage("encounterScheduled.mail.success",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    } else {
                        redirectAttributes.addFlashAttribute("failure",
                            messageSource.getMessage("encounterScheduled.mail.fail", new Object[]{},
                                LocaleContextHolder.getLocale()));
                    }
                default:
                    break;
            }
            Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
            patientAttributes.add(AuditPatientAttribute.MAIL_STATUS);
            auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
                "toggleMailStatus(" + encounterScheduledId + ", " + "redirectAttributes)",
                encounterScheduled.getCaseNumber(), patientAttributes, AuditEntryActionType.CHANGE);
            encounterScheduledDao.merge(encounterScheduled);
        }

        return "redirect:/encounter/list";
    }

    /**
     * Controls the HTTP GET Request for the URL
     * <i>/encounter/toggleMailStatusByPatient</i>. Enables a patient to
     * activate the mail notification for a given {@link EncounterScheduled}.
     *
     * @param uuid The uuid of the given {@link EncounterScheduled} object whose mail reminder has
     *             to be activated.
     * @return Depending on the mailStatus of the {@link EncounterScheduled} object redirect to the
     * <i>/error/pagenotfound</i>,
     * <i>/encounter/mailReactivated</i>
     * or the <i>/encounter/cancel</i> website.
     */
    @RequestMapping(value = "/encounter/activateMailStatusByPatient")
    public String activateMailStatusByPatient(
        @RequestParam(required = true, value = "hash") final String uuid) {
        EncounterScheduled encounterScheduled = encounterScheduledDao.getElementByUUID(uuid);

        if (encounterScheduled == null) {
            return "error/pagenotfound";
        }
        if (encounterScheduled.getMailStatus().equals(EncounterScheduledMailStatus.INTERRUPTED)) {
            return "encounter/completed";
        }
        if (encounterScheduled.getMailStatus()
            .equals(EncounterScheduledMailStatus.CONSENT_PENDING)) {
            encounterScheduled.setMailStatus(EncounterScheduledMailStatus.ACTIVE);
            encounterScheduledDao.merge(encounterScheduled);
        }
        return "encounter/mailReactivated";
    }

    /**
     * Controls the HTTP GET Request for the URL
     * <i>/encounter/deactivateMailStatusByPatient</i>. Enables a patient to
     * deactivate the mail notification for a given {@link EncounterScheduled}.
     *
     * @param uuid The uuid of the given {@link EncounterScheduled} object whose mail reminder has
     *             to be deactivated.
     * @return Depending on the mailStatus of the {@link EncounterScheduled} object to the
     * <i>/error/pagenotfound</i>, <i>/encounter/reactivated</i> or the <i>/encounter/cancel</i>
     * website.
     */
    @RequestMapping(value = "/encounter/deactivateMailStatusByPatient")
    public String deactivateMailStatusByPatient(
        @RequestParam(required = true, value = "hash") final String uuid) {
        EncounterScheduled encounterScheduled = encounterScheduledDao.getElementByUUID(uuid);

        if (encounterScheduled == null) {
            return "error/pagenotfound";
        }
        if (encounterScheduled.getMailStatus().equals(EncounterScheduledMailStatus.INTERRUPTED)) {
            return "encounter/completed";
        }
        if (encounterScheduled.getMailStatus().equals(EncounterScheduledMailStatus.ACTIVE)
            || encounterScheduled.getMailStatus()
            .equals(EncounterScheduledMailStatus.DEACTIVATED_ENCOUNTER_MANAGER)) {
            encounterScheduled.setMailStatus(EncounterScheduledMailStatus.DEACTIVATED_PATIENT);
            encounterScheduledDao.merge(encounterScheduled);
        }
        return "encounter/mailCanceled";
    }

    /**
     * Controls the HTTP GET Request for the URL <i>/encounter/interrupt</i>. Interrupts the
     * {@link EncounterScheduled} object by turning the mail reminder off and setting its enddate to
     * now. No more {@link Encounter} objects will be created and no more remind mails will be sent
     * for the {@link EncounterScheduled} object.
     *
     * @param encounterScheduledId The id of the {@link EncounterScheduled} object which should be
     *                             interrupted.
     * @return Redirect to the <i>/encounter/list</i> website.
     */
    @RequestMapping(value = "/encounter/interrupt")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String interruptEncounter(
        @RequestParam(required = true, value = "encounterScheduledId") final Long encounterScheduledId) {
        EncounterScheduled encounterScheduled = encounterScheduledDao.getElementById(
            encounterScheduledId);

        if (encounterScheduled != null) {
            Date now = new Date();
            encounterScheduled.setEndDate(now);
            for (Encounter encounter : encounterScheduled.getEncounters()) {
                if (encounter.getEndTime() == null) {
                    encounter.setEndTime(new Timestamp(now.getTime()));
                }
            }
            encounterScheduled.setMailStatus(EncounterScheduledMailStatus.INTERRUPTED);
            Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
            patientAttributes.add(AuditPatientAttribute.MAIL_STATUS);
            auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
                "interruptEncounter(" + encounterScheduledId + ")",
                encounterScheduled.getCaseNumber(), patientAttributes, AuditEntryActionType.CHANGE);
            encounterScheduledDao.merge(encounterScheduled);
        }

        return "redirect:/encounter/list";
    }

    /**
     * Controls the HTTP POST request for the URL <i>/encounter/remove</i>. Used to remove an
     * existing {@link EncounterScheduled EncounterScheduled} object without any adherent encounter
     * objects.
     *
     * @param encounterScheduledId The id of the encounterScheduled object to remove.
     * @param model                :The model, which holds the information for the view.
     * @param redirectAttributes   :Stores the information for a redirect scenario.
     * @return The <i>encounter/list</i> website.
     */
    @RequestMapping(value = "/encounter/remove")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String removeEncounterScheduled(
        @RequestParam(value = "encounterScheduledId", required = true) final Long encounterScheduledId,
        final Model model, final RedirectAttributes redirectAttributes) {
        EncounterScheduled encounterScheduled = encounterScheduledDao.getElementById(
            encounterScheduledId);

        if (encounterScheduled != null && (encounterScheduled.getEncounters() == null
            || encounterScheduled.getEncounters().isEmpty())) {
            Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
            patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
            patientAttributes.add(AuditPatientAttribute.EMAIL_ADDRESS);
            auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
                "removeEncounterScheduled(encounterScheduledId, model, " + "redirectAttributes)",
                encounterScheduled.getCaseNumber(), patientAttributes, AuditEntryActionType.DELETE);
            encounterScheduledDao.remove(encounterScheduled);
            redirectAttributes.addFlashAttribute("success",
                messageSource.getMessage("encounterScheduled.succes.remove", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        return "redirect:/encounter/list";
    }

    /**
     * Controls the HTTP Request for the URL <i>/encounter/editEmail</i>. Sets the email address of
     * the given {@link EncounterScheduled} object.
     *
     * @param encounterScheduledId The id of the encounterScheduled object.
     * @param email                :The new email to set.
     * @param model                :The model, which holds the information for the view.
     * @param redirectAttributes   :Stores the information for a redirect scenario.
     * @return The <i>encounter/list</i> website.
     */
    @RequestMapping(value = "/encounter/editEmail")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String editEmail(
        @RequestParam(value = "encounterScheduledId", required = true) final Long encounterScheduledId,
        @RequestParam(value = "email") final String email, final Model model,
        final RedirectAttributes redirectAttributes) {
        EncounterScheduled encounterScheduled = encounterScheduledDao.getElementById(
            encounterScheduledId);
        if (encounterScheduled != null && email != null && !email.isEmpty()
            && !encounterScheduled.getMailStatus()
            .equals(EncounterScheduledMailStatus.DEACTIVATED_PATIENT)
            && !encounterScheduled.getMailStatus()
            .equals(EncounterScheduledMailStatus.INTERRUPTED)) {
            encounterScheduled.setEmail(email);
            if (encounterScheduled.getMailStatus()
                .equals(EncounterScheduledMailStatus.ADDRESS_REJECTED)) {
                encounterScheduled.setMailStatus(EncounterScheduledMailStatus.ACTIVE);
            }

            Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
            patientAttributes.add(AuditPatientAttribute.EMAIL_ADDRESS);
            auditEntryDao.writeAuditEntry(this.getClass().getSimpleName(),
                "editEmail(" + encounterScheduled.getId() + ", email, " + "model)",
                encounterScheduled.getCaseNumber(), patientAttributes, AuditEntryActionType.CHANGE);
            encounterScheduledDao.merge(encounterScheduled);
            redirectAttributes.addFlashAttribute("success",
                messageSource.getMessage("encounterScheduled.succes.editMail", new Object[]{},
                    LocaleContextHolder.getLocale()));
        } else {
            redirectAttributes.addFlashAttribute("failure",
                messageSource.getMessage("encounterScheduled.error.editMail", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        return "redirect:/encounter/list";
    }
}
