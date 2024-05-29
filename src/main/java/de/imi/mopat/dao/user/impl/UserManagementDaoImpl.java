package de.imi.mopat.dao.user.impl;

import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;

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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the generic interface UserManagementDao. This dao is inherited by all daos,
 * which implement a dao for user specified operations.
 * <p>
 * Implements specific methods suitable for daos with user specified operations.
 */
@Component
public abstract class UserManagementDaoImpl<T> implements
    de.imi.mopat.dao.user.UserManagementDao<T> {
    // Provides the EntityManager, which manages the persistence layer

    @PersistenceContext(unitName = "MoPat_User")
    protected EntityManager moPatUserEntityManager;
    // Holds the generic entity class T
    private final Class<T> entityClass;

    /**
     * Constructor, which gets the generic class T.
     */
    public UserManagementDaoImpl() {
        // Get the generic class T
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
    @Transactional("MoPat_User")
    public void merge(final T element) {
        moPatUserEntityManager.merge(element);
    }

    @Override
    @Transactional("MoPat_User")
    public void persist(final T element) {
        moPatUserEntityManager.persist(element);
    }

    @Override
    @Transactional("MoPat_User")
    public void remove(final T element) {
        moPatUserEntityManager.remove(moPatUserEntityManager.merge(element));
    }

    @Override
    @Transactional("MoPat_User")
    public T getElementById(final Long id) {
        try {
            TypedQuery<T> query = moPatUserEntityManager.createQuery(
                "SELECT e FROM " + getEntityClass().getSimpleName() + " e" + " WHERE e.id=" + (id),
                getEntityClass());
            T element = query.getSingleResult();
            return element;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional("MoPat_User")
    public T getElementByUUID(final String uuid) {
        try {
            TypedQuery<T> query = moPatUserEntityManager.createQuery(
                "SELECT e FROM " + getEntityClass().getSimpleName() + " e" + " WHERE e.uuid='"
                    + (uuid) + "'", getEntityClass());
            T element = query.getSingleResult();
            return element;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional("MoPat_User")
    public Collection<T> getElementsById(final Collection<Long> ids) {
        Collection<T> elements = new ArrayList<T>();
        if (!ids.isEmpty()) {
            Query query = moPatUserEntityManager.createQuery(
                "SELECT e FROM " + getEntityClass().getSimpleName() + " e WHERE e.id IN :ids");
            query.setParameter("ids", ids);
            elements = query.getResultList();
        }
        return elements;
    }

    @Override
    @Transactional("MoPat_User")
    public List<T> getAllElements() {
        TypedQuery<T> query = moPatUserEntityManager.createQuery(
            "SELECT e FROM " + getEntityClass().getSimpleName() + " e", getEntityClass());

        List<T> elements = query.getResultList();
        return elements;
    }

    @Override
    @Transactional("MoPat_User")
    public Long getCount() {
        TypedQuery<Long> query = moPatUserEntityManager.createQuery(
            "SELECT count(u) FROM " + getEntityClass().getSimpleName() + " u", Long.class);
        Long count = query.getSingleResult();
        return count;
    }

    @Override
    @Transactional("MoPat_User")
    public void grantRight(final T element, final User user, final PermissionType right,
        final Boolean inheritance) {
        // In general nothing to do here
    }

    @Override
    @Transactional("MoPat_User")
    public void grantInheritedRight(final T element, final User currentUser,
        final PermissionType right) {
        // In general nothing to do here
    }

    @Override
    @Transactional("MoPat_User")
    public void revokeRight(final T element, final User user, final PermissionType right,
        final Boolean inheritance) {
        // In general nothing to do here
    }

    @Override
    @Transactional("MoPat_User")
    public void revokeInheritedRight(final T element, final User currentUser,
        final PermissionType right) {
        // In general nothing to do here
    }
}
