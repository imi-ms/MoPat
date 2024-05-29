package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.MoPatDao;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the generic interface MoPatDao, which is inherited by daos within the
 * application.
 * <p>
 * Implements CRUD methods for all daos.
 */
@Component
public abstract class MoPatDaoImpl<T> implements MoPatDao<T> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        MoPatDaoImpl.class);

    @Autowired
    private AclClassDao aclClassDao;
    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;
    @Autowired
    private AclEntryDao aclEntryDao;
    // Provides the EntityManager, which manages the persistence layer
    @PersistenceContext(unitName = "MoPat")
    protected EntityManager moPatEntityManager;
    // Holds the generic entity class T
    private final Class<T> entityClass;

    /**
     * Constructor, which gets the generic class T.
     */
    public MoPatDaoImpl() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        entityClass = (Class) pt.getActualTypeArguments()[0];
    }

    /**
     * Returns the entity class of the current dao implementation.
     *
     * @return The entity class of the current dao implementation.
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    @Transactional("MoPat")
    public T getElementById(final Long id) {
        try {
            TypedQuery<T> query = moPatEntityManager.createQuery(
                "SELECT e FROM " + getEntityClass().getSimpleName() + " " + "e" + " WHERE e.id="
                    + (id), getEntityClass());
            T element = query.getSingleResult();
            return element;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional("MoPat")
    public T getElementByUUID(final String uuid) {
        try {
            TypedQuery<T> query = moPatEntityManager.createQuery(
                "SELECT e FROM " + getEntityClass().getSimpleName() + " " + "e" + " WHERE e.uuid='"
                    + (uuid) + "'", getEntityClass());
            T element = query.getSingleResult();
            return element;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional("MoPat")
    public Collection<T> getElementsById(final Collection<Long> ids) {
        Collection<T> elements = new ArrayList<T>();
        if (!ids.isEmpty()) {
            Query query = moPatEntityManager.createQuery(
                "SELECT e FROM " + getEntityClass().getSimpleName() + " e WHERE e.id IN :ids");
            query.setParameter("ids", ids);
            elements = query.getResultList();
        }
        return elements;
    }

    @Override
    @Transactional("MoPat")
    public List<T> getAllElements() {
        TypedQuery<T> query = moPatEntityManager.createQuery(
            "SELECT e FROM " + getEntityClass().getSimpleName() + " e", getEntityClass());
        List<T> elements = query.getResultList();
        return elements;
    }

    @Override
    @Transactional("MoPat")
    public Long getCount() {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "SELECT count(e) FROM " + getEntityClass().getSimpleName() + " e", Long.class);
        Long count = query.getSingleResult();
        return count;
    }

    @Override
    @Transactional("MoPat")
    public void merge(final T element) {
        try {
            // Cast the element to the appropriate entity class
            getEntityClass().cast(element);
            // Get the getId method for the element
            Method method = element.getClass().getMethod("getId");
            // Get the database Id for the element
            Long elementId = (Long) method.invoke(element);
            // If the element is new to the database
            if (elementId == null) {
                // Save element to the database
                moPatEntityManager.persist(element);
            } else {
                // Otherwise update the element
                moPatEntityManager.merge(element);
            }
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException | SecurityException ex) {
            LOGGER.error("Error while saving entity of type {}. Check if there is "
                + "a consistent database.", getEntityClass().getName());
        }
    }

    @Override
    @Transactional("MoPat")
    public void remove(final T element) {
        moPatEntityManager.remove(moPatEntityManager.merge(element));
    }

    @Override
    @Transactional("MoPat_User")
    public void grantRight(final T element, final User user, final PermissionType right,
        final Boolean inheritance) {
        try {
            // Get the corresponding ACL class of the given element
            AclClass elementClass = aclClassDao.getElementByClass(getEntityClass().getName());
            // Cast the element to the appropriate entity class
            getEntityClass().cast(element);
            // Get the getId method for the element
            Method method = element.getClass().getDeclaredMethod("getId");
            // Get the database Id for the element
            Long elementId = (Long) method.invoke(element);
            // Create a new ACLObjectIdentity for the element and save it
            AclObjectIdentity elementObjectIdentity = aclObjectIdentityDao.getElementByClassAndObjectId(
                elementClass, elementId);
            AclEntry elementUserAccess = aclEntryDao.getEntryForObjectUserAndRight(element, user,
                right);
            // If the user does not have the given right for the given object
            // . grant it
            if (elementUserAccess == null) {
                elementUserAccess = new AclEntry(user, elementObjectIdentity, 1, right, true, false,
                    false);
                aclEntryDao.persist(elementUserAccess);
            }
            // Grant inherited rights if requested
            if (inheritance) {
                grantInheritedRight(element, user, right);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                 NoSuchMethodException | SecurityException ex) {
            LOGGER.error("Error during granting the right{} for entity of type {} "
                    + "and user with ID {}. Check if there is a " + "consistent database.", right,
                getEntityClass().getName(), user.getId());
        }
    }

    @Override
    @Transactional("MoPat_User")
    public void grantInheritedRight(final T element, final User user, final PermissionType right) {
        // In general nothing to do here
    }

    @Override
    @Transactional("MoPat_User")
    public void revokeRight(final T element, final User user, final PermissionType right,
        final Boolean inheritance) {
        AclEntry elementUserAccess = aclEntryDao.getEntryForObjectUserAndRight(element, user,
            right);
        if (elementUserAccess != null) {
            aclEntryDao.remove(elementUserAccess);
        }
        // Revoke inherited rights if requested
        if (inheritance) {
            revokeInheritedRight(element, user, right);
        }
    }

    @Override
    @Transactional("MoPat_User")
    public void revokeInheritedRight(final T element, final User user, final PermissionType right) {
        // In general nothing to do here
    }
}
