package de.imi.mopat.dao.user.impl;

import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclObjectIdentity;

import java.util.List;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AclObjectIdentityDaoImpl extends UserManagementDaoImpl<AclObjectIdentity> implements
    AclObjectIdentityDao {

    @Override
    public AclObjectIdentity getElementByClassAndObjectId(final AclClass clazz,
        final Long objectId) {
        try {
            TypedQuery<AclObjectIdentity> query = moPatUserEntityManager.createQuery(
                "SELECT a FROM AclObjectIdentity a WHERE a.objectIdClass" + ".id='" + clazz.getId()
                    + "' AND a" + ".objectIdIdentity='" + objectId + "'", getEntityClass());
            AclObjectIdentity aclObjectIdentity = query.getSingleResult();
            return aclObjectIdentity;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<AclObjectIdentity> getElementsByClass(final AclClass clazz) {
        try {
            TypedQuery<AclObjectIdentity> query = moPatUserEntityManager.createQuery(
                "SELECT a FROM AclObjectIdentity a WHERE a.objectIdClass" + ".id=" + clazz.getId(),
                getEntityClass());
            List<AclObjectIdentity> aclObjectIdentities = query.getResultList();
            return aclObjectIdentities;
        } catch (Exception e) {
            return null;
        }
    }

}
