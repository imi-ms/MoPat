package de.imi.mopat.auth;

import org.slf4j.Logger;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * Customized Pre Authentication Checker. The Difference from the default checker is that the check
 * for a disabled user was moved to {@link CustomPostAuthenticationChecks}.
 */
public class CustomPreAuthenticationChecks implements UserDetailsChecker {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        CustomPreAuthenticationChecks.class);

    @Override
    public void check(final UserDetails user) {
        if (!user.isAccountNonLocked()) {
            LOGGER.debug("User account is locked");

            throw new LockedException(
                messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked",
                    "User account is locked"));
        }

        if (!user.isAccountNonExpired()) {
            LOGGER.debug("User account is expired");

            throw new AccountExpiredException(
                messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired",
                    "User account has expired"));
        }
    }

}
