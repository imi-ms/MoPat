package de.imi.mopat.dao.user.impl;

import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.model.user.AclClass;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AclClassDaoImpl extends UserManagementDaoImpl<AclClass> implements AclClassDao {

    @Override
    public AclClass getElementByClass(final String className) {
        try {
            TypedQuery<AclClass> query = moPatUserEntityManager.createQuery(
                "SELECT e FROM " + getEntityClass().getSimpleName() + " e" + " WHERE e.className='"
                    + (className) + "'", getEntityClass());
            AclClass element = query.getSingleResult();
            return element;
        } catch (NoResultException e) {
            return null;
        }
    }

}
