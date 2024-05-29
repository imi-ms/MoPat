package de.imi.mopat.auth;

import de.imi.mopat.helper.controller.ApplicationMailer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 */
public class CustomAuthenticationFailureHandler extends
    ExceptionMappingAuthenticationFailureHandler {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        CustomAuthenticationFailureHandler.class);
    public static final String LAST_USERNAME_KEY = "LAST_USERNAME";

    @Autowired
    private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;
    @Autowired
    private ApplicationContext appContext;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ApplicationMailer applicationMailer;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
        final HttpServletResponse response, final AuthenticationException exception)
        throws IOException, ServletException {
        boolean ldapAuthenticationSuccess = false;

        // Get credentials
        String usernameParameter = usernamePasswordAuthenticationFilter.getUsernameParameter();
        String lastUserName = request.getParameter(usernameParameter);
        String passwordParameter = usernamePasswordAuthenticationFilter.getPasswordParameter();
        String lastPassword = request.getParameter(passwordParameter);
        if (exception instanceof DisabledException) {
            // if user is disabled show message and make no ldap check
            super.onAuthenticationFailure(request, response, exception);
            return;
        }
        MoPatActiveDirectoryLdapAuthenticationProvider adAuthenticationProvider = appContext.getBean(
            "adAuthenticationProvider", MoPatActiveDirectoryLdapAuthenticationProvider.class);
        Boolean activatedLdap = adAuthenticationProvider.isActiveDirectoryLDAPAuthenticationActivated();

        if (activatedLdap) {
            // Try authentication via active directory
            try {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    lastUserName, lastPassword);
                adAuthenticationProvider.authenticate(token);
                ldapAuthenticationSuccess = true;
            } catch (BadCredentialsException thrownException) {
                // If authentication via active directory was not successful
                // return the thrown exception
                super.onAuthenticationFailure(request, response, thrownException);
            } catch (InsufficientAuthenticationException thrownException) {
                // If authentication via active directory was successful but
                // authorization in MoPat was not
                // send a notification to the domain user by email
                String footerEmail = applicationMailer.getMailFooterEMail();
                String footerPhone = applicationMailer.getMailFooterPhone();
                String domain = adAuthenticationProvider.getActiveDirectoryLDAPDomain();

                Locale domainDefaultLanguageLocale = adAuthenticationProvider.getActiveDirectoryLDAPDefaultLanguage();

                String contextPath = request.getContextPath();
                String baseUrl = null;
                if (contextPath == null || contextPath.isEmpty()) {
                    baseUrl = (String) request.getRequestURL()
                        .subSequence(0, request.getRequestURL().lastIndexOf("/"));
                } else {
                    baseUrl = request.getRequestURL()
                        .subSequence(0, request.getRequestURL().lastIndexOf(contextPath))
                        + contextPath;
                }
                String subject = messageSource.getMessage(
                    "mail.authenticationWithoutAuthorization.subject", new Object[]{},
                    domainDefaultLanguageLocale);
                String content = messageSource.getMessage(
                    "mail.authenticationWithoutAuthorization.content",
                    new Object[]{domain, baseUrl, footerEmail, footerPhone},
                    domainDefaultLanguageLocale);
                String footer = messageSource.getMessage(
                    "mail.authenticationWithoutAuthorization.footer",
                    new Object[]{footerEmail, footerPhone}, domainDefaultLanguageLocale);
                HashSet<String> recipientsBCC = null;
                try {
                    applicationMailer.sendMail(lastUserName + "@" + domain, recipientsBCC, subject,
                        content + footer, null);
                } catch (MailException e) {
                    LOGGER.debug("It wasn't possible to send email: " + e.getMessage());
                }
                // If authentication via active directory was not successful
                // return the thrown exception
                super.onAuthenticationFailure(request, response, thrownException);
            }
        }
        // If authentication via active directory was successful return the
        // given exception
        if (!activatedLdap || ldapAuthenticationSuccess) {
            super.onAuthenticationFailure(request, response, exception);
        }
        // Store the given username in the session
        try {
            HttpSession session = request.getSession(false);
            if (session != null || isAllowSessionCreation()) {
                request.getSession().setAttribute(LAST_USERNAME_KEY, lastUserName);
            }
        } catch (IllegalStateException illegalStateException) {
            LOGGER.debug(Arrays.toString(illegalStateException.getStackTrace()));
        }
    }
}
