package de.imi.mopat.dao.user;

import de.imi.mopat.model.user.PinAuthorization;
import de.imi.mopat.model.user.User;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public interface PinAuthorizationDao extends UserManagementDao<PinAuthorization> {

    /**
     * Return true, if entry for a user is in the database
     * @param user to look for
     * @return true if entry exists, false otherwise
     */
    boolean isPinAuthActivatedForUser(User user);


    /**
     * Returns all entries for a user from the pin_authorization table
     * @param user for which the entries should be fetched
     * @return List of all entries
     */
    Set<PinAuthorization> getEntriesForUser(User user);
}
