package de.imi.mopat.cron;

import de.imi.mopat.helper.controller.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


/**
 * Cron job to automatically disable users whose expiration date has passed
 */
@Service
public class UserExpirationScheduler {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        UserExpirationScheduler.class);

    @Autowired
    private UserService userService;

    /**
     * Runs daily at 2:00 AM to check and disable expired users
     */
    @Scheduled(cron = "${de.imi.mopat.cron.UserExpirationScheduler.trigger:0 0 2 * * ?}")
    public void disableExpiredUsers() {
        LOGGER.info("Starting scheduled task to disable expired users");

        try {
            int disabledCount = userService.disableExpiredUsers();
            LOGGER.info("Successfully disabled {} expired user account(s)", disabledCount);
        } catch (Exception e) {
            LOGGER.error("Error during expired users check: {}", e.getMessage(), e);
        }
    }

}
