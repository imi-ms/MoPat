package de.imi.mopat.auth;

import org.slf4j.Logger;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * Customized Post Authentication Checker.
 * <p>
 * See also {@link CustomPreAuthenticationChecks}.
 */
public class CustomPostAuthenticationChecks implements UserDetailsChecker {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        CustomPostAuthenticationChecks.class);

    @Override
    public void check(final UserDetails user) {
        if (!user.isEnabled()) {
            LOGGER.debug("User account is disabled");

            throw new DisabledException(
                messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled",
                    "User is disabled"));
        }

        if (!user.isCredentialsNonExpired()) {
            LOGGER.debug("User account credentials have expired");

            throw new CredentialsExpiredException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider" + ".credentialsExpired",
                "User credentials have expired"));
        }
    }
}