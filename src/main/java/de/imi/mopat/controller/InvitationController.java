package de.imi.mopat.controller;

import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.user.ForgotPasswordTokenDao;
import de.imi.mopat.dao.user.InvitationDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.controller.ClinicService;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.ClinicDTO;
import de.imi.mopat.model.dto.InvitationDTO;
import de.imi.mopat.model.dto.InvitationUserDTO;
import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.user.Invitation;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserRole;
import de.imi.mopat.validator.InvitationDTOValidator;
import de.imi.mopat.validator.UserValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 *
 */
@Controller
public class InvitationController {

    @Autowired
    private AclEntryDao aclEntryDao;
    @Autowired
    private AclClassDao aclClassDao;
    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;
    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private InvitationDao invitationDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ForgotPasswordTokenDao forgotPasswordDao;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private InvitationDTOValidator invitationDTOValidator;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ApplicationMailer applicationMailer;
    @Autowired
    private ClinicService clinicService;

    /**
     * Returns a list of {@link ClinicDTO clinicDTOs}, which are not assigned to the given
     * {@link InvitationDTO invitationDTO}.
     *
     * @param invitationDTO The {@link InvitationDTO invitationDTO} with already assigned
     *                      {@link ClinicDTO clinicDTOs}.
     * @return Returns all {@link ClinicDTO clinicDTO} objects that are not assigned to the
     * {@link InvitationDTO}.
     */
    public List<ClinicDTO> getAvailableClinicDTOs(final InvitationDTO invitationDTO) {

        List<ClinicDTO> availableClinicDTOs = new ArrayList<>();

        // Get availableClinics by removing assignedClinics from all clinics
        for (Clinic clinic : clinicDao.getAllElements()) {
            availableClinicDTOs.add(clinicService.toClinicDTO(clinic));
        }
        availableClinicDTOs.removeAll(invitationDTO.getAssignedClinics());

        Collections.sort(availableClinicDTOs, new Comparator<ClinicDTO>() {
            @Override
            public int compare(final ClinicDTO o1, final ClinicDTO o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return availableClinicDTOs;
    }

    /**
     * Controls the HTTP GET request for the URL <i>/invitation/list</i>. Shows the list of
     * invitations.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>/invitation/list</i> website.
     */
    @RequestMapping(value = "invitation/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showInvitations(final Model model) {
        model.addAttribute("invitedUsers", invitationDao.getAllElements());
        return "invitation/list";
    }

    /**
     * Controls the HTTP GET request for the URL <i>invitation/edit</i>. Shows the form to create a
     * new {@link Invitation invitation}.
     *
     * @param model        The model, which holds information for the view.
     * @param request      The current request.
     * @param invitationId Id of the current invitation.
     * @return The <i>invitation/edit</i> website.
     */
    @RequestMapping(value = {"invitation/edit"}, method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String edit(@RequestParam(value = "id", required = false) final Long invitationId,
        final Model model, final HttpServletRequest request) {

        InvitationDTO invitationDTO = null;

        if (invitationId == null || invitationId <= 0) {
            invitationDTO = new InvitationDTO();
        } else {
            Invitation invitation = invitationDao.getElementById(invitationId);
            invitationDTO = invitation.toInvitationDTO();

            List<ClinicDTO> assignedClinicDTOs = new ArrayList<>();

            // Add assignedClinics to invitation
            for (AclObjectIdentity aclObjectIdentity
                : invitation.getAssignedClinics()) {
                assignedClinicDTOs.add(clinicService.toClinicDTO(clinicDao.getElementById(aclObjectIdentity.getObjectIdIdentity())));
            }
            invitationDTO.setAssignedClinics(assignedClinicDTOs);
        }

        model.addAttribute("availableClinics", getAvailableClinicDTOs(invitationDTO));
        model.addAttribute("roleList", new ArrayList<>(Arrays.asList(UserRole.values())));
        model.addAttribute("languageList", request.getLocales());
        model.addAttribute("invitationDTO", invitationDTO);

        return "invitation/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/invitation/edit</i>. Provides the ability to
     * create a new {@link Invitation invitation} or edit an old one.
     *
     * @param invitationDTO The {@link InvitationDTO InvitationDTO} object from the view.
     * @param action        The name of the submit button which has been clicked.
     * @param result        The result for validation of the invitation object.
     * @param model         The model, which holds the information for the view.
     * @param request       The current request.
     * @return The <i>invitation/edit</i>, <i> invitation/list</i> or
     * <i>user/list</i> website.
     */
    @RequestMapping(value = {"/invitation/edit"}, method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional("MoPat_User")
    public String invite(@RequestParam final String action,
        @ModelAttribute("invitationDTO") @Valid final InvitationDTO invitationDTO,
        final BindingResult result, final Model model, final HttpServletRequest request) {

        if (action.equalsIgnoreCase("cancel")) {
            return "redirect:list";
        }

        // Remove empty Invitation Users
        if (invitationDTO.getInvitationUsers().size() > 1) {
            invitationDTO.getInvitationUsers().removeIf(
                (InvitationUserDTO t) -> t.getFirstName() == null && t.getLastName() == null
                    && t.getEmail() == null);
        }

        // Get base URL for preview and mailing
        String contextPath = request.getContextPath();
        String baseUrl = null;
        if (contextPath.equalsIgnoreCase("/")) {
            baseUrl = (String) request.getRequestURL()
                .subSequence(0, request.getRequestURL().lastIndexOf("/invitation"));
        } else {
            baseUrl = request.getRequestURL()
                .subSequence(0, request.getRequestURL().lastIndexOf(contextPath + "/invitation"))
                + contextPath;
        }

        // Validate inputs
        invitationDTOValidator.validate(invitationDTO, result);

        // If the edited invitationDTO has errors or is chosen for preview
        if (result.hasErrors() || action.equalsIgnoreCase("preview")) {
            // Delete empty assignedClinics
            for (Iterator<ClinicDTO> iterator = invitationDTO.getAssignedClinics().iterator();
                iterator.hasNext(); ) {
                ClinicDTO assignedClinicDTO = iterator.next();
                if (assignedClinicDTO == null || assignedClinicDTO.getId() == null) {
                    iterator.remove();
                }
            }
            model.addAttribute("roleList", new ArrayList<>(Arrays.asList(UserRole.values())));
            model.addAttribute("languageList", request.getLocales());
            model.addAttribute("availableClinics", getAvailableClinicDTOs(invitationDTO));

            // If the preview is chosen for the edited invitationDTO
            if (!result.hasErrors() && action.equalsIgnoreCase("preview")) {
                Locale locale = LocaleHelper.getLocaleFromString(invitationDTO.getLocale());
                // Create the invitation texts needed for the preview
                String personalMessage = ".";
                String personalText = invitationDTO.getPersonalText();
                if (personalText != null && !personalText.trim().isEmpty()) {
                    personalMessage = " " + messageSource.getMessage("mail.invitation.personal",
                        new Object[]{personalText}, locale);
                }
                String subject = messageSource.getMessage("mail.invitation.subject", new Object[]{},
                    locale);
                String footerEmail = applicationMailer.getMailFooterEMail();
                String footerPhone = applicationMailer.getMailFooterPhone();
                String footer = messageSource.getMessage("mail.invitation.footer",
                    new Object[]{footerEmail, footerPhone}, locale);

                // Add the preview information to the model
                String content = messageSource.getMessage("mail.invitation.content",
                    new Object[]{personalMessage, "LINK"}, locale);
                model.addAttribute("preview", subject + "\n\n" + content + footer);
                model.addAttribute("invitationDTO", invitationDTO);
            }
            return "invitation/edit";
        }

        // Set properties of invitation
        //Get assignedClinics by its related aclObjectIdentity
        AclClass elementClass = aclClassDao.getElementByClass(Clinic.class.getName());
        Set<AclObjectIdentity> aclObjectIdentitys = new HashSet<>();
        for (ClinicDTO clinicDTO : invitationDTO.getAssignedClinics()) {
            if (clinicDTO.getId() != null) {
                aclObjectIdentitys.add(
                    this.aclObjectIdentityDao.getElementByClassAndObjectId(elementClass,
                        clinicDTO.getId()));
            }
        }

        // Get the invitation object(s) based on the given invitationDTO
        if (invitationDTO.getId() == null) {
            User currentUser = null;
            if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                .equals("anonymousUser")) {
                User contextUser = (User) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
                currentUser = userDao.getElementById(contextUser.getId());
                model.addAttribute("currentUser", currentUser);
            }
            for (InvitationUserDTO invitationUserDTO : invitationDTO.getInvitationUsers()) {
                Invitation invitation = new Invitation(invitationUserDTO.getEmail(), currentUser);
                invitation.setFirstName(invitationUserDTO.getFirstName());
                invitation.setLastName(invitationUserDTO.getLastName());
                invitation.setLocale(invitationDTO.getLocale());
                invitation.setPersonalText(invitationDTO.getPersonalText());
                invitation.setRole(invitationDTO.getRole());
                invitation.setAssignedClinics(aclObjectIdentitys);
                invitation.refreshExpirationDate();
                invitationDao.merge(invitation);

                invitation.sendMail(applicationMailer, messageSource, baseUrl);
            }
        } else {
            Invitation invitation = invitationDao.getElementById(invitationDTO.getId());
            invitation.setFirstName(invitationDTO.getInvitationUsers().get(0).getFirstName());
            invitation.setLastName(invitationDTO.getInvitationUsers().get(0).getLastName());
            invitation.setLocale(invitationDTO.getLocale());
            invitation.setPersonalText(invitationDTO.getPersonalText());
            invitation.setRole(invitationDTO.getRole());
            invitation.setAssignedClinics(aclObjectIdentitys);
            invitation.refreshExpirationDate();
            invitationDao.merge(invitation);

            invitation.sendMail(applicationMailer, messageSource, baseUrl);
        }

        return "redirect:list";
    }

    /**
     * Controls the HTTP GET request for the URL <i>invitation/refresh</i>. Refreshes the expiration
     * date of an {@link Invitation} object and re-sends the invitation E-mail.
     *
     * @param model        The model, which holds informations for the view.
     * @param invitationId Id of the current invitation.
     * @param request      The current request.
     * @return The <i>invitation/list</i> website.
     */
    @RequestMapping(value = "invitation/refresh", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String refreshExpirationDate(final Model model,
        @RequestParam(value = "id", required = false) final Long invitationId,
        final HttpServletRequest request) {
        Invitation invitation = invitationDao.getElementById(invitationId);
        invitation.refreshExpirationDate();
        invitationDao.merge(invitation);

        String contextPath = request.getContextPath();
        String baseUrl = null;
        if (contextPath.equalsIgnoreCase("/")) {
            baseUrl = (String) request.getRequestURL()
                .subSequence(0, request.getRequestURL().lastIndexOf("/invitation"));
        } else {
            baseUrl = request.getRequestURL()
                .subSequence(0, request.getRequestURL().lastIndexOf(contextPath + "/invitation"))
                + contextPath;
        }

        invitation.sendMail(applicationMailer, messageSource, baseUrl);
        return "redirect:list";
    }

    /**
     * Controls the HTTP GET request for the URL <i>invitation/remove</i>. Removes
     * {@link Invitation Invitation} object from the database.
     *
     * @param model        The model, which holds informations for the view.
     * @param invitationId Id of the current invitation.
     * @return The <i>invitation/list</i> website.
     */
    @GetMapping(value = "invitation/remove")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String remove(final Model model,
        @RequestParam(value = "id", required = false) final Long invitationId) {
        invitationDao.remove(invitationDao.getElementById(invitationId));
        return "redirect:list";
    }
}

