package de.imi.mopat.dao.user;

import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclEntry;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface AclEntryDao extends UserManagementDao<AclEntry> {

    /**
     * Searches for an {@link AclEntry AclEntry} by object, user and granted right.
     *
     * @param object The object of the searched {@link AclEntry AclEntry}.
     * @param user   The {@link User User} of the searched {@link AclEntry AclEntry}.
     * @param right  The {@link PermissionType right} of the searched {@link AclEntry AclEntry}.
     * @return The {@link AclEntry AclEntry}, which was found by its object identity, user and
     * granted right.
     */
    AclEntry getEntryForObjectUserAndRight(Object object, User user, PermissionType right);

    /**
     * Searches for all objects ids corresponding to {@link AclClass AclClass} for given
     * {@link User User} and {@link PermissionType right}.
     *
     * @param clazz The {@link AclClass AclClass} of the searched objects.
     * @param user  The {@link User User} of the searched objects.
     * @param right The {@link PermissionType right} of the searched objects.
     * @return A list of object ids which where found by {@link AclClass AclClass},
     * {@link User User} and {@link PermissionType right}.
     */
    List<Long> getObjectIdsForClassUserAndRight(Class clazz, User user, PermissionType right);

    /**
     * Searches for all {@link User User} {@link PermissionType right} combinations granted for the
     * given object.
     *
     * @param object The object whose {@link PermissionType rights} should be retrieved.
     * @return A map with {@link User Users} and corresponding {@link PermissionType rights}.
     */
    Map<User, PermissionType> getUserRightsByObject(Object object);
}