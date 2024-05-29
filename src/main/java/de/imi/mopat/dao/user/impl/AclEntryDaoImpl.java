package de.imi.mopat.dao.user.impl;

import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclEntry;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AclEntryDaoImpl extends UserManagementDaoImpl<AclEntry> implements AclEntryDao {

    @Autowired
    private AclClassDao aclClassDao;
    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;

    @Override
    public AclEntry getEntryForObjectUserAndRight(final Object object, final User user,
        final PermissionType right) {
        // Get the corresponding ACLClass object for the given object
        AclClass aclClass = aclClassDao.getElementByClass(object.getClass().getName());
        try {
            // Get the getId method for the element
            Method method = object.getClass().getDeclaredMethod("getId");
            // Get the database Id for the element
            Long objectId = (Long) method.invoke(object);
            // Get the acl object identity for the given object
            AclObjectIdentity aclObjectIdentity = aclObjectIdentityDao.getElementByClassAndObjectId(
                aclClass, objectId);
            TypedQuery<AclEntry> query = moPatUserEntityManager.createQuery(
                "SELECT a FROM AclEntry a WHERE a" + ".aclObjectIdentity=:aclObjectIdentity AND a"
                    + ".user=:user AND a.permissionType=:permissionType", AclEntry.class);
            query.setParameter("aclObjectIdentity", aclObjectIdentity);
            query.setParameter("user", user);
            query.setParameter("permissionType", right);
            AclEntry aclEntry = query.getSingleResult();
            return aclEntry;
        } catch (NoResultException | NoSuchMethodException | SecurityException |
                 IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            return null;
        }
    }

    @Override
    public List<Long> getObjectIdsForClassUserAndRight(final Class clazz, final User user,
        final PermissionType right) {
        // Initialize result list
        List<Long> objectIds = new ArrayList<>();
        // Get the correspondig ACLClass object for the given class
        AclClass aclClass = aclClassDao.getElementByClass(clazz.getName());
        // Get all managed object identites for given class
        List<AclObjectIdentity> aclObjectIdentities = aclObjectIdentityDao.getElementsByClass(
            aclClass);
        if (!aclObjectIdentities.isEmpty()) { // [bt] querying the database
            // only has to happen if at least one object is managed by ACLs
            Collection<Long> aclObjectIdentityIds = new ArrayList<>();
            for (AclObjectIdentity aclObjectIdentity : aclObjectIdentities) {
                aclObjectIdentityIds.add(aclObjectIdentity.getId());
            }
            TypedQuery<AclEntry> query = moPatUserEntityManager.createQuery(
                "SELECT a FROM AclEntry a WHERE a.user=:user AND a"
                    + ".permissionType=:permissionType AND a"
                    + ".aclObjectIdentity.id IN :aclObjectIdentities", AclEntry.class);
            query.setParameter("aclObjectIdentities", aclObjectIdentityIds);
            query.setParameter("user", user);
            query.setParameter("permissionType", right);
            List<AclEntry> aclEntries = query.getResultList();

            for (AclEntry aclEntry : aclEntries) {
                objectIds.add(aclEntry.getAclObjectIdentity().getObjectIdIdentity());
            }
        }
        return objectIds;
    }

    @Override
    public Map<User, PermissionType> getUserRightsByObject(final Object object) {
        // Get the correspondig ACLClass object for the given object
        AclClass aclClass = aclClassDao.getElementByClass(object.getClass().getName());
        try {
            // Get the getId method for the element
            Method method = object.getClass().getDeclaredMethod("getId");
            // Get the database Id for the element
            Long objectId = (Long) method.invoke(object);
            // Get the acl object identity for the given object
            AclObjectIdentity aclObjectIdentity = aclObjectIdentityDao.getElementByClassAndObjectId(
                aclClass, objectId);
            TypedQuery<Object[]> query = moPatUserEntityManager.createQuery(
                "SELECT a.user, a.permissionType FROM AclEntry a WHERE a"
                    + ".aclObjectIdentity=:aclObjectIdentity", Object[].class);
            query.setParameter("aclObjectIdentity", aclObjectIdentity);
            List<Object[]> userRightsFromDatabase = query.getResultList();
            // Convert SQL result into a map of users with corresponding rights
            Map<User, PermissionType> userRights = new HashMap<>(userRightsFromDatabase.size());
            for (Object[] userRight : userRightsFromDatabase) {
                userRights.put((User) userRight[0], (PermissionType) userRight[1]);
            }
            return userRights;
        } catch (NoResultException | NoSuchMethodException | SecurityException |
                 IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            return null;
        }
    }


}
