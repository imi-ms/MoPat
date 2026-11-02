package de.imi.mopat.auth;

import de.imi.mopat.dao.user.PinAuthorizationDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.PinAuthorization;
import de.imi.mopat.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.List;


@Service
public class PinAuthorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PinAuthorizationService.class);

    @Autowired
    PinAuthorizationDao pinAuthorizationDao;

    @Autowired
    UserDao userDao;

    /**
     * Checks multiple aspects to check, if the pin view should be shown
     * Those are:
     * - If an authentication is available in Spring
     * - If the user has activated quick login
     * - If there is an entry for a user in the pin_authorization table
     * @return true if conditions are met, false otherwise
     */
    public boolean isPinLoginApplicable() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            User user = userDao.loadUserByUsername(authentication.getName());
            if (user.getUsePin()) {
                return pinAuthorizationDao.isPinAuthActivatedForUser(user);
            }
        }
        return false;
    }

    /**
     * Removes the entry from the pin_authorization table
     * that triggers the PinAuthorizationFilter
     */
    public void removePinAuthForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            User user = userDao.loadUserByUsername(authentication.getName());
            if (user.getUsePin()) {
                Set<PinAuthorization> pinAuthorizationSet = pinAuthorizationDao.getEntriesForUser(user);
                for (PinAuthorization pinAuth : pinAuthorizationSet) {
                    pinAuthorizationDao.remove(pinAuth);
                }
            }
        }
    }

    /**
     * Removes the entries from the pin_authorization table
     * for a specific user that triggers the PinAuthorizationFilter
     * @param user
     */
    public void removePinAuthForUser(User user) {
        if (user.getUsePin()) {
            Set<PinAuthorization> pinAuthorizationSet = pinAuthorizationDao.getEntriesForUser(user);
            for (PinAuthorization pinAuth : pinAuthorizationSet) {
                pinAuthorizationDao.remove(pinAuth);
            }
        }
    }

    /**
     * Removes all entries from the db and adds a new one
     * @param user to reset the authorization state for
     */
    public void resetPinAuthForUser(User user) {
        removePinAuthForUser(user);
        PinAuthorization pinAuthorization = new PinAuthorization(user);
        pinAuthorizationDao.merge(pinAuthorization);
    }

    /**
     * Removes all pin authorities entries from the database
     */
    public void removeAllPinAuthotizationEntries() {
        try {
            List<PinAuthorization> pinAuthorizationList = pinAuthorizationDao.getAllElements();
            for (PinAuthorization pinAuthorization : pinAuthorizationList) {
                pinAuthorizationDao.remove(pinAuthorization);
            }
        } catch (AuthenticationCredentialsNotFoundException e) {
            LOGGER.info("No authentication credentials found while clearing sessions.");
        }

    }

    /**
     * Helper function to reduce the number of tries for a user
     * If the user has no tries left, the entry will be removed from the database
     * And the session will be invalidated
     * @param user to reduce the amount of tries for
     */
    public void decreaseRemainingTriesForUser(User user) {
        Set<PinAuthorization> pinAuthorizationSet = pinAuthorizationDao.getEntriesForUser(user);
        for (PinAuthorization pinAuthorization : pinAuthorizationSet) {

            pinAuthorization.decreaseRemainingTries();

            if (pinAuthorization.getRemainingTries() <= 0) {
                pinAuthorizationDao.remove(pinAuthorization);
                //Invalidate the session, just to be safe
                SecurityContextHolder.getContext().setAuthentication(null);
            } else {
                pinAuthorizationDao.merge(pinAuthorization);
            }
        }
    }
}
