package de.imi.mopat.controller;

import de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.user.ForgotPasswordTokenDao;
import de.imi.mopat.dao.user.InvitationDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.user.AclEntry;
import de.imi.mopat.model.user.Authority;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.user.ForgotPasswordToken;
import de.imi.mopat.model.user.Invitation;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserRole;
import de.imi.mopat.validator.UserValidator;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;

import java.util.List;
import java.util.Locale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * The Controller servlet communicates with the front end of the model and loads the
 * HttpServletRequest or HttpSession with appropriate data, before forwarding the HttpServletRequest
 * and Response to the JSP using a RequestDispatcher. This controller handles all requests related
 * to user.
 */
@Controller
@SessionAttributes("hideProfile")
public class UserController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(UserController.class);
    public static final String LAST_USERNAME_KEY = "LAST_USERNAME";

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
    private MessageSource messageSource;
    @Autowired
    private ApplicationMailer applicationMailer;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private MoPatActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider;

    /**
     * @param id (<i>optional</i>) Id of the {@link User User} object
     * @return Returns a new {@link User User} object to the attribute
     * <i>user</i> in the model, if no id is given. Otherwise the
     * {@link User User} object associated with the id is returned.
     */
    @ModelAttribute("user")
    public User getUser(final Long id) {
        if (id == null || id <= 0) {
            return new User();
        }
        return userDao.getElementById(id);
    }

    /**
     * Before the RequestMapping takes place the method writes the requested or a new user object to
     * the model accessible for all methods and the corresponding views.
     *
     * @return Returns a the current {@link User User} object to the attribute currentUser in the
     * model.
     */
    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            .equals("anonymousUser")) {
            User contextUser = (User) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
            User currentUser = userDao.getElementById(contextUser.getId());
            return currentUser;
        } else {
            return null;
        }
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/user/list</i>. Shows the list of users.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>/user/list</i> website.
     */
    @RequestMapping(value = "/user/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showUsers(final Model model) {
        model.addAttribute("allUsers", userDao.getAllElements());
        return "user/list";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/user/edit</i>. Shows the page containing the
     * form fields for editing a {@link User User} object.
     *
     * @param returnPage The URL for the page to return after processing the user editing.
     * @param request    The request, which was sent from the client's browser.
     * @param model      The model, which holds the information for the view.
     * @return The <i>/user/edit</i> website.
     */
    @RequestMapping(value = {"/user/edit", "/mobile/user/edit"}, method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String edit(
        @RequestParam(value = "returnPage", required = false) final String returnPage,
        final HttpServletRequest request, final Model model) {
        model.addAttribute("returnPage", returnPage);
        model.addAttribute("hideProfile", Boolean.TRUE);
        // Cut of the extension and the first slash from the current url to
        // identify the corresponding jsp location
        return request.getServletPath().replaceAll("", "").substring(1);
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/user/edit</i>. Provides the ability to update
     * a {@link User User} object.
     *
     * @param returnPage         The URL for the page to return after processing the user editing.
     * @param oldPassword        The current password of the {@link User User } object, which should
     *                           be changed.
     * @param newPassword        The new password of the {@link User User} object, which should be
     *                           changed.
     * @param newPasswordApprove The password approval, which should be equal to the new password.
     * @param action             The name of the submit button which has been clicked.
     * @param user               The {@link User User} object which has been edited.
     * @param result             The result for validation of the bundle object.
     * @param model              The model, which holds the information for the view.
     * @return The <i>/user/edit</i> website.
     */
    @RequestMapping(value = {"/user/edit", "/mobile/user/edit"}, method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public String edit(
        @RequestParam(value = "returnPage", required = false) final String returnPage,
        @RequestParam(value = "oldPassword", required = false) final String oldPassword,
        @RequestParam(value = "newPassword", required = false) final String newPassword,
        @RequestParam(value = "newPasswordApprove", required = false) final String newPasswordApprove,
        @RequestParam(value = "action", required = false) final String action,
        @ModelAttribute("currentUser") final User user, final BindingResult result,
        final Model model) {

        if (action == null || action.equalsIgnoreCase("save")) {
            // Set given password for the current user
            if (!user.isLdap()) {
                user.setOldPassword(oldPassword);
                user.setNewPassword(newPassword);
                user.setPasswordCheck(newPasswordApprove);
            }
            userValidator.validate(user, result);
            if (!result.hasErrors()) {
                model.addAttribute("success",
                    messageSource.getMessage("user.success.changed", new Object[]{},
                        LocaleContextHolder.getLocale()));
                // Set given old password
                if (user.getOldPassword() != null && !user.getOldPassword().isEmpty()
                    && !user.isLdap()) {
                    user.setPassword(newPassword);
                    userDao.setPassword(user);
                }
                userDao.merge(user);
            }
        }
        if (returnPage != null && !returnPage.isEmpty()) {
            if (result.hasErrors() && action.equalsIgnoreCase("save")) {
                model.addAttribute("returnPage", returnPage);
                return "mobile/user/edit";
            } else {
                return "redirect:" + returnPage;
            }
        } else {
            return "user/edit";
        }
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/mobile/user/password</i>. Shows the page
     * containing the form fields for requesting a new password for a {@link User User}.
     *
     * @return The <i>/mobile/user/password</i> website.
     */
    @RequestMapping(value = "/mobile/user/password", method = RequestMethod.GET)
    public String forgotPassword(final Model model) {
        return "mobile/user/password";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/mobile/user/password</i>. Provides the
     * ability to request a new password for a {@link User User}.
     *
     * @param username The username whose password reset is requested.
     * @param request  The request, which was sent from the client's browser.
     * @param model    The model, which holds the information for the view.
     * @return The <i>/mobile/user/password</i> website.
     */
    @RequestMapping(value = "/mobile/user/password", method = RequestMethod.POST)
    public String forgotPassword(
        @RequestParam(value = "username", required = false) final String username,
        final HttpServletRequest request, final Model model) {
        User user = userDao.loadUserByUsername(username);
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            String baseUrl = (String) request.getRequestURL()
                .subSequence(0, request.getRequestURL().indexOf("/mobile/user/password"));
            String subject = messageSource.getMessage("mail.forgotPassword.subject", new Object[]{},
                LocaleContextHolder.getLocale());
            String footerEmail = applicationMailer.getMailFooterEMail();
            String footerPhone = applicationMailer.getMailFooterPhone();
            String footer = messageSource.getMessage("mail.forgotPassword.footer",
                new Object[]{footerEmail, footerPhone}, LocaleContextHolder.getLocale());
            String content;
            if (user.isLdap()) {
                String domain = activeDirectoryLdapAuthenticationProvider.getActiveDirectoryLDAPDomain();
                String domainSupportPhone = activeDirectoryLdapAuthenticationProvider.getActiveDirectoryLDAPSupportPhone();
                content = messageSource.getMessage("mail.forgotPasswordLdap.content",
                    new Object[]{baseUrl, domain, domainSupportPhone},
                    LocaleContextHolder.getLocale());
            } else {
                ForgotPasswordToken oldForgotPasswordToken = forgotPasswordDao.getElementByUser(
                    user);
                if (oldForgotPasswordToken != null) {
                    forgotPasswordDao.remove(oldForgotPasswordToken);
                }
                ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user);
                forgotPasswordDao.merge(forgotPasswordToken);
                DateFormat dateformat = DateFormat.getDateInstance(DateFormat.FULL,
                    LocaleContextHolder.getLocale());
                content = messageSource.getMessage("mail.forgotPassword.content",
                    new Object[]{baseUrl, baseUrl + "/mobile/user" + "/passwordreset?token="
                        + forgotPasswordToken.getUuid(),
                        dateformat.format(forgotPasswordToken.getExpirationDate())},
                    LocaleContextHolder.getLocale());
            }
            Set<String> recipientsBCC = null;
            try {
                applicationMailer.sendMail(user.getEmail(), recipientsBCC, subject,
                    content + footer, null);
            } catch (MailException e) {
                LOGGER.debug("It wasn't possible to send email: " + e.getMessage());
            } catch (Exception ex) {
                LOGGER.debug("It wasn't possible to send email: " + ex.getMessage());
            }
        } else {
            model.addAttribute("error",
                messageSource.getMessage("user.error.usernameNotInUse", new Object[]{},
                    LocaleContextHolder.getLocale()));
            return "mobile/user/password";
        }
        request.getSession().setAttribute(LAST_USERNAME_KEY, username);
        return "redirect:/mobile/user/login?message=forgotPasswordSuccess";
    }

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/mobile/user/passwordreset</i>. Shows the page containing the form
     * fields for resetting the password for a {@link User User} with given
     * {@link ForgotPasswordToken}.
     *
     * @param token The identifier for the {@link ForgotPasswordToken}, which should be used for
     *              this password reset process.
     * @param model The model, which holds the information for the view.
     * @return The <i>/mobile/user/passwordreset</i> website.
     */
    @RequestMapping(value = "/mobile/user/passwordreset", method = RequestMethod.GET)
    public String passwordReset(@RequestParam(value = "token", required = false) final String token,
        final Model model) {
        ForgotPasswordToken currentForgotPasswordToken = forgotPasswordDao.getElementByUUID(token);
        if (currentForgotPasswordToken == null || !currentForgotPasswordToken.isActive()) {
            return "redirect:/error/accessdenied";
        } else {
            model.addAttribute("token", token);
            model.addAttribute("user", currentForgotPasswordToken.getUser());
        }
        return "mobile/user/passwordreset";
    }

    /**
     * Controls the HTTP POST requests for the URL
     * <i>/mobile/user/passwordreset</i>. Provides the ability to reset the
     * password for a {@link User User} with given {@link ForgotPasswordToken}.
     *
     * @param token              The identifier for the {@link Invitation Invitation}, which should
     *                           be used for this registration process.
     * @param newPassword        The new password of the {@link User User} object.
     * @param newPasswordApprove The password approval, which should be equal to the new password.
     * @param request            The request, which was sent from the client's browser.
     * @param model              The model, which holds the information for the view.
     * @return The <i>/user/invite</i> website.
     */
    @RequestMapping(value = "/mobile/user/passwordreset", method = RequestMethod.POST)
    public String register(@RequestParam(value = "token", required = false) final String token,
        @RequestParam(value = "newPassword", required = false) final String newPassword,
        @RequestParam(value = "newPasswordApprove", required = false) final String newPasswordApprove,
        final HttpServletRequest request, final Model model) {
        ForgotPasswordToken currentForgotPasswordToken = forgotPasswordDao.getElementByUUID(token);
        if (currentForgotPasswordToken == null || !currentForgotPasswordToken.isActive()) {
            return "redirect:/error/accessdenied";
        } else {
            User user = currentForgotPasswordToken.getUser();
            boolean error = false;
            if (newPassword.length() < Constants.PASSWORD_MINIMUM_SIZE
                || newPassword.length() > Constants.PASSWORD_MAXIMUM_SIZE) {
                model.addAttribute("error", messageSource.getMessage("user.error.passwordSize",
                    new Object[]{Constants.PASSWORD_MINIMUM_SIZE, Constants.PASSWORD_MAXIMUM_SIZE},
                    LocaleContextHolder.getLocale()));
                error = true;
            } else if (!newPassword.equals(newPasswordApprove)) {
                model.addAttribute("error",
                    messageSource.getMessage("user.error.passwordsNotMatching", new Object[]{},
                        LocaleContextHolder.getLocale()));
                error = true;
            }
            if (!error) {
                user.setPassword(newPassword);
                userDao.setPassword(user);
                userDao.merge(user);
                forgotPasswordDao.remove(currentForgotPasswordToken);
                request.getSession().setAttribute(LAST_USERNAME_KEY, user.getUsername());
                model.addAttribute("message",
                    messageSource.getMessage("user.success.changed", new Object[]{},
                        LocaleContextHolder.getLocale()));
                return "mobile/user/login";
            } else {
                model.addAttribute("user", user);
            }
        }
        model.addAttribute("token", token);
        return "mobile/user/passwordreset";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/mobile/user/register</i>. Shows the page
     * containing the form fields for registering as a new {@link User User}.
     *
     * @param hash  The identifier for the {@link Invitation Invitation}, which should be used for
     *              this registration process.
     * @param model The model, which holds the information for the view.
     * @return The <i>/mobile/user/register</i> website.
     */
    @RequestMapping(value = "/mobile/user/register", method = RequestMethod.GET)
    public String register(@RequestParam(value = "hash", required = false) final String hash,
        final Model model) {
        Invitation currentInvitation = invitationDao.getElementByUUID(hash);
        if (currentInvitation == null || !currentInvitation.isActive()) {
            return "redirect:/error/accessdenied";
        } else {
            User user = (User) model.asMap().get("user");
            user.setFirstname(currentInvitation.getFirstName());
            user.setLastname(currentInvitation.getLastName());
            user.setEmail(currentInvitation.getEmail());
            model.addAttribute("hash", hash);
            model.addAttribute("user", user);
            Collection<Clinic> clinics = clinicDao.getClinicsFromAclObjectIdentitys(
                currentInvitation.getAssignedClinics());
            model.addAttribute("clinics", clinics);
        }
        Boolean activateLdap = activeDirectoryLdapAuthenticationProvider.isActiveDirectoryLDAPAuthenticationActivated();
        model.addAttribute("isLdap", true);
        model.addAttribute("activateLdap", activateLdap);
        String domain = activeDirectoryLdapAuthenticationProvider.getActiveDirectoryLDAPDomain();
        model.addAttribute("domain", domain);
        return "mobile/user/register";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/user/register</i>. Provides the ability to
     * register as a new {@link User User}.
     *
     * @param hash               The identifier for the {@link Invitation Invitation}, which should
     *                           be used for this registration process.
     * @param isLdap             Is the user local or an ldap user.
     * @param newPassword        The new password of the {@link User User} object, which should be
     *                           registered.
     * @param newPasswordApprove The password approval, which should be equal to the new password.
     * @param user               The {@link User User} object which should be registered.
     * @param result             The result for validation of the bundle object.
     * @param model              The model, which holds the information for the view.
     * @return The <i>/user/invite</i> website.
     */
    @RequestMapping(value = "/mobile/user/register", method = RequestMethod.POST)
    public String register(@RequestParam(value = "hash", required = false) final String hash,
        @RequestParam(value = "isLdap", required = false) Boolean isLdap,
        @RequestParam(value = "newPassword", required = false) final String newPassword,
        @RequestParam(value = "newPasswordApprove", required = false) final String newPasswordApprove,
        @ModelAttribute("user") final User user, final BindingResult result, final Model model) {
        Invitation currentInvitation = invitationDao.getElementByUUID(hash);
        if (currentInvitation == null || !currentInvitation.isActive()) {
            return "redirect:/error/accessdenied";
        } else {
            if (isLdap == null) {
                isLdap = false;
            }
            user.addAuthority(
                new Authority(user, UserRole.fromString(currentInvitation.getRole())));
            user.setPrincipal(true);
            if (!isLdap) {
                user.setPassword(newPassword);
                user.setPasswordCheck(newPasswordApprove);
            }
            user.setNewPassword(newPassword);
            userValidator.validate(user, result);
            if (!result.hasErrors()) {
                if (!isLdap) {
                    userDao.setPassword(user);
                }
                userDao.merge(user);
                // get newly persisted user
                User persistedUser = userDao.loadUserByUsername(user.getUsername());
                // grant clinic rights
                Collection<Clinic> assignedClinics = clinicDao.getClinicsFromAclObjectIdentitys(
                    currentInvitation.getAssignedClinics());
                for (Clinic clinic : assignedClinics) {
                    AclEntry clinicACLEntry = aclEntryDao.getEntryForObjectUserAndRight(clinic,
                        persistedUser, PermissionType.READ);
                    if (clinicACLEntry == null) {
                        clinicDao.grantRight(clinic, persistedUser, PermissionType.READ,
                            Boolean.TRUE);
                    }
                }
                invitationDao.remove(currentInvitation);
                model.addAttribute("message",
                    messageSource.getMessage("user.success.changed", new Object[]{},
                        LocaleContextHolder.getLocale()));
                return "mobile/user/login";
            }
        }
        Boolean activateLdap = activeDirectoryLdapAuthenticationProvider.isActiveDirectoryLDAPAuthenticationActivated();
        model.addAttribute("activateLdap", activateLdap);
        model.addAttribute("isLdap", isLdap);
        String domain = activeDirectoryLdapAuthenticationProvider.getActiveDirectoryLDAPDomain();
        model.addAttribute("domain", domain);
        model.addAttribute("hash", hash);
        model.addAttribute("user", user);
        Collection<Clinic> assignedClinics = clinicDao.getClinicsFromAclObjectIdentitys(
            currentInvitation.getAssignedClinics());
        model.addAttribute("clinics", assignedClinics);
        return "mobile/user/register";
    }

    /**
     * Controls the HTTP requests for the URL <i>/user/remove</i>. Removes a {@link User User}
     * object by a given id and redirects to the list of users.
     *
     * @param id Id of the {@link User User} object, which should be removed.
     * @return Redirect to the <i>/user/list</i> website.
     */
    @RequestMapping(value = "/user/toggleenabled")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String removeUser(@RequestParam(value = "id", required = true) final Long id) {
        User user = userDao.getElementById(id);
        if (user != null) {
            // enable / disable the user
            user.setIsEnabled(!user.getIsEnabled());
            userDao.merge(user);
        }
        return "redirect:/user/list";
    }

    /**
     * Controls the HTTP requests for the URL <i>/mobile/user/login</i>. Shows the login page.
     *
     * @param message     A message, which was sent by another website redirecting to
     *                    <i>/mobile/user/login</i>
     * @param model       The model, which holds the information for the view.
     * @param httpSession The http session object.
     * @return The website, which was requested.
     */
    @RequestMapping(value = "/mobile/user/login", method = RequestMethod.GET)
    public String login(@RequestParam(value = "message", required = false) final String message,
        final Model model, final HttpSession httpSession) {
        // Logout first!
        SecurityContextHolder.getContext().setAuthentication(null);
        if (message != null && !message.isEmpty()) {
            switch (message) {
                case "InsufficientAuthenticationException":
                case "BadCredentialsException":
                    model.addAttribute("message",
                        messageSource.getMessage("user.error.badCredentials", new Object[]{},
                            LocaleContextHolder.getLocale()));
                    break;
                case "forgotPasswordSuccess":
                    model.addAttribute("message",
                        messageSource.getMessage("user.success" + ".forgotPassword", new Object[]{},
                            LocaleContextHolder.getLocale()));
                    break;
                case "DisabledException":
                    model.addAttribute("message",
                        messageSource.getMessage("user.error.userDisabled", new Object[]{},
                            LocaleContextHolder.getLocale()));
                    break;
                default:
                    break;
            }
        } else {
            // If the message is null, forget old username
            httpSession.removeAttribute(LAST_USERNAME_KEY);
        }
        //Get the default language of the application from the configuration
        String defaultLanguage = configurationDao.getDefaultLanguage();
        model.addAttribute("defaultLanguage", defaultLanguage);
        model.addAttribute("encounter", null);
        return "mobile/user/login";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/user/clinicrights</i>. Shows the page
     * containing the form fields for granting or revoking rights for clinics to a
     * {@link User User}.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>user/edit</i> website.
     */
    @RequestMapping(value = "/user/clinicrights", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editClinicRights(final Model model) {

        if (model.asMap().get("user") == null) {
            model.addAttribute("user", null);
            return "redirect:/user/list";
        }

        Collection<Clinic> assignedClinics = clinicDao.getElementsById(
            aclEntryDao.getObjectIdsForClassUserAndRight(Clinic.class,
                (User) model.asMap().get("user"), PermissionType.READ));
        Collection<Clinic> availableClinics = clinicDao.getAllElements();
        availableClinics.removeAll(assignedClinics);
        model.addAttribute("availableClinics", availableClinics);
        model.addAttribute("assignedClinics", assignedClinics);
        return "user/clinicrights";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/user/clinicrights</i>. Provides the ability
     * to grant or revoke rights for clinics to a {@link User User}.
     *
     * @param clinicIDs The {@link Clinic clinic} object IDs, which are assigned to the
     *                  {@link User User}.
     * @param action    The name of the submit button which has been clicked.
     * @param user      The {@link User User} object whose {@link PermissionType rights} should be
     *                  edited.
     * @param result    The result for validation of the bundle object.
     * @param model     The model, which holds the information for the view.
     * @return The <i>/user/clinicrights</i> website.
     */
    @RequestMapping(value = "/user/clinicrights", method = RequestMethod.POST)
    public String editClinicRights(
        @RequestParam(required = false, value = "clinicIDs") final List<Long> clinicIDs,
        @RequestParam(value = "action", required = true) final String action,
        @ModelAttribute("user") final User user, final BindingResult result, final Model model) {
        if (action.equalsIgnoreCase("save")) {
            Collection<Clinic> assignedClinics = clinicDao.getElementsById(
                aclEntryDao.getObjectIdsForClassUserAndRight(Clinic.class, user,
                    PermissionType.READ));
            Collection<Clinic> currentClinics = new ArrayList<>();
            if (clinicIDs != null && !clinicIDs.isEmpty()) {
                currentClinics = clinicDao.getElementsById(clinicIDs);
            }
            assignedClinics.removeAll(currentClinics);
            currentClinics.removeAll(assignedClinics);
            for (Clinic clinic : currentClinics) {
                AclEntry clinicACLEntry = aclEntryDao.getEntryForObjectUserAndRight(clinic, user,
                    PermissionType.READ);
                if (clinicACLEntry == null) {
                    clinicDao.grantRight(clinic, user, PermissionType.READ, Boolean.TRUE);
                }
            }
            for (Clinic clinic : assignedClinics) {
                AclEntry clinicACLEntry = aclEntryDao.getEntryForObjectUserAndRight(clinic, user,
                    PermissionType.READ);
                if (clinicACLEntry != null) {
                    clinicDao.revokeRight(clinic, user, PermissionType.READ, Boolean.TRUE);
                }
            }
        }
        return "redirect:/user/list";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/user/mailtoall</i>. Provides the ability to
     * send an email to all {@link User users}.
     *
     * @param subject  The default subject for the mail.
     * @param content  The default content for the mail.
     * @param language The default selected language for the view.
     * @param model    The model of the view.
     * @param request  The current http request object.
     * @return the <i>user/mailtoall</i> website.
     */
    @RequestMapping(value = "/user/mailtoall", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getMailToAll(
        @RequestParam(value = "subject", required = false) final String subject,
        @RequestParam(value = "content", required = false) final String content,
        @RequestParam(value = "language", required = false) final Locale language,
        final Model model, final HttpServletRequest request) {

        if (subject != null) {
            model.addAttribute("subject", subject);
        }
        if (content != null) {
            model.addAttribute("content", content);
        }
        if (language != null) {
            model.addAttribute("language", language);
        } else {
            model.addAttribute("language", LocaleContextHolder.getLocale());
        }
        return "user/mailtoall";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/user/mailtoall</i>. Provides the ability to
     * send an email to all {@link User users}.
     *
     * @param subject  the subject of the email. Must not be <code>null</code>, must not be empty.
     * @param content  the email's content. Must not be <code>null</code>, must not be empty.
     * @param language The selected language for the mail.
     * @param action   The name of the submit button which has been clicked.
     * @param model    The model of the view.
     * @return The <i>user/list</i> website after sending the email.
     */
    @RequestMapping(value = "/user/mailtoall", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String postMailToAll(
        @RequestParam(value = "subject", required = true) final String subject,
        @RequestParam(value = "content", required = true) final String content,
        @RequestParam(value = "language", required = true) final Locale language,
        @RequestParam final String action, final Model model) {

        LOGGER.debug(
            "Enter postMailToAll(String, String) with 1st parameter: {}, " + "2nd parameter: {}",
            subject, content);

        String result = null;
        List<String> errors = new ArrayList<>();

        if (subject == null || subject.trim().isEmpty()) {
            errors.add(messageSource.getMessage("user.error.mailToAll.subjectEmpty", new Object[]{},
                LocaleContextHolder.getLocale()));
        }

        if (content == null || content.trim().isEmpty()) {
            errors.add(messageSource.getMessage("user.error.mailToAll.contentEmpty", new Object[]{},
                LocaleContextHolder.getLocale()));
        }

        model.addAttribute("subject", subject);
        model.addAttribute("content", content);
        model.addAttribute("language", language);
        String footerEmail = applicationMailer.getMailFooterEMail();
        String footerPhone = applicationMailer.getMailFooterPhone();
        String footer = messageSource.getMessage("mail.invitation.footer",
            new Object[]{footerEmail, footerPhone}, language);

        if (action.equalsIgnoreCase("preview")) {
            model.addAttribute("errors", errors);
            model.addAttribute("preview", true);
            String previewContent = content.concat("\n").concat(footer);
            model.addAttribute("previewContent", previewContent);
            result = "user/mailtoall";
            // Contains errors
        } else if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("preview", false);
            // Everything is fine
        } else {
            Set<String> allEMailAddressesDistinct = userDao.getAllEnabledEMailAddressesDistinct();
            if (allEMailAddressesDistinct == null) {
                errors.add(
                    messageSource.getMessage("user.error.mailToAll.errorReceiving", new Object[]{},
                        LocaleContextHolder.getLocale()));
                model.addAttribute("errors", errors);
                result = "user/mailtoall";
            } else {
                String emailFrom = applicationMailer.getMailFrom();
                allEMailAddressesDistinct.add(emailFrom);
                try {
                    applicationMailer.sendMail(null, allEMailAddressesDistinct, subject,
                        content + footer, null);
                } catch (MailException e) {
                    LOGGER.debug("It wasn't possible to send email: " + e.getMessage());
                }
                result = "redirect:/user/list";
            }
        }
        return result;
    }
}
