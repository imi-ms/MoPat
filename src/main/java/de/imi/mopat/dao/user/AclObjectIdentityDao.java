package de.imi.mopat.dao.user;

import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclObjectIdentity;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface AclObjectIdentityDao extends UserManagementDao<AclObjectIdentity> {

    /**
     * Searches for an {@link AclObjectIdentity AclObjectIdentity} object by
     * {@link AclClass AclClass} and the objectId.
     *
     * @param clazz    {@link AclClass AclClass} of the searched
     *                 {@link AclObjectIdentity AclObjectIdentity} object.
     * @param objectId Object Id of the searched {@link AclObjectIdentity AclObjectIdentity}
     *                 object.
     * @return The {@link AclObjectIdentity AclObjectIdentity} object, which was found by its
     * {@link AclClass AclClass} and the objectId.
     */
    AclObjectIdentity getElementByClassAndObjectId(AclClass clazz, Long objectId);

    /**
     * Searches for all {@link AclObjectIdentity AclObjectIdentity} objects with given
     * {@link AclClass AclClass}.
     *
     * @param clazz {@link AclClass AclClass} of the searched
     *              {@link AclObjectIdentity AclObjectIdentity} objects.
     * @return The {@link AclObjectIdentity AclObjectIdentity} objects, which where found by their
     * {@link AclClass AclClass}.
     */
    List<AclObjectIdentity> getElementsByClass(AclClass clazz);
}