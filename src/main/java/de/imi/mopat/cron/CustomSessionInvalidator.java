package de.imi.mopat.cron;

import de.imi.mopat.auth.PinAuthorizationService;
import de.imi.mopat.dao.user.PinAuthorizationDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.User;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

/**
 * This is a custom cron job to manually handle session invalidation. Since the session time is
 * increased to 24h, due to pin authorization, we need to manually invalidate sessions for users
 * that do not have pin auth activated.
 */
@Service
public class CustomSessionInvalidator {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        CustomSessionInvalidator.class);

    @Autowired
    PinAuthorizationService pinAuthorizationService;

    @Autowired
    PinAuthorizationDao pinAuthorizationDao;

    @Autowired
    UserDao userDao;

    @Autowired
    SessionRegistry sessionRegistry;

    /**
     * Cron job checks, if a session is idle for at least 25 minutes and handles the invalidation of
     * the session / the setting for the pin authorization
     */
    @Scheduled(cron = "${de.imi.mopat.cron.CustomSessionInvalidator.trigger}")
    public void handleSessionTimeouts() {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                Date currentTime = new Date(System.currentTimeMillis());
                Date lastRequest = session.getLastRequest();

                long diffInMillies = currentTime.getTime() - lastRequest.getTime();
                long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);

                /**
                 * Set to 25, as schedule trigger is 5 minutes, so max session time is 30 min, but is always
                 * between 25 - 30 minutes
                 */
                if (diffInMinutes >= 25) {
                    handleInvalidation(principal);
                }
            }
        }
    }

    /**
     * Helper method to handle invalidation. Sets pin auth flag, if user has activated pin auth.
     * Will invalidate session otherwise
     *
     * @param principal of the session to invalidate
     */
    private void handleInvalidation(Object principal) {
        if (principal.getClass().equals(User.class)) {
            User principalUser = (User) principal;
            User dbUser = userDao.loadUserByUsername(principalUser.getUsername());
            if (dbUser.getUsePin()) {
                if (!pinAuthorizationDao.isPinAuthActivatedForUser(dbUser)) {
                    LOGGER.info("Session has been idle for too long. Pin Auth was activated for user {}, but not active, therefore the flag was set",
                        dbUser.getUsername());
                    pinAuthorizationService.resetPinAuthForUser(dbUser);
                }
            } else {
                LOGGER.info("Session has been idle for too long. Pin Auth was not activated for user {}, therefore the session was invalidated",
                    dbUser.getUsername());
                for (SessionInformation session : sessionRegistry.getAllSessions(principal,
                    false)) {
                    session.expireNow();
                }
            }
        }
    }

}
