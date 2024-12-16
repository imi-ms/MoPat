package de.imi.mopat.cron;

import de.imi.mopat.auth.PinAuthorizationService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

/**
 * Cron job to handle the automatic reset of the pin authorization
 */
@Service
public class NightlyAuthorizationResetter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        NightlyAuthorizationResetter.class);

    @Autowired
    PinAuthorizationService pinAuthorizationService;

    @Autowired
    SessionRegistry sessionRegistry;

    @Scheduled(cron = "${de.imi.mopat.cron.NightlyAuthorizationResetter.trigger}")
    public void clearAuthorization() {
        pinAuthorizationService.removeAllPinAuthotizationEntries();
        LOGGER.info("Cleared all pin authorization entries from database");
        invalidateSessions();
    }

    /**
     * Helper function to remove all current sessions
     */
    private void invalidateSessions() {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                session.expireNow();
                LOGGER.info("Expired session: {}", session.getSessionId());
            }
        }
    }

}
