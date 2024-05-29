package de.imi.mopat.dao.user;

import de.imi.mopat.model.user.AclClass;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface AclClassDao extends UserManagementDao<AclClass> {

    /**
     * Searches for a element of type AclClass by class name.
     *
     * @param className Class name of the searched AclClass.
     * @return The AclClass, which was found by its class name.
     */
    AclClass getElementByClass(String className);
}