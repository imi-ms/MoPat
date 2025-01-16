package de.imi.mopat.dao;

import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Component;

/**
 * Generic interface, which is implemented by all data access objects (daos).
 * <p>
 * Provides CRUD methods for all daos.
 */
@Component
public interface MoPatDao<T> {

    /**
     * Returns the entity class of the current dao implementation.
     *
     * @return The entity class of the current dao implementation.
     */
    Class<T> getEntityClass();

    /**
     * Searches for a element of type T by id.
     *
     * @param id Id of the searched element.
     * @return The element, which was found by its id. If non has been found,
     * <code>null</code>.
     */
    //@PostAuthorize("hasRole('ROLE_ADMIN') OR hasPermission(returnObject, 'READ')")
    T getElementById(Long id);

    /**
     * Searches for a element of type T by UUID.
     *
     * @param uuid UUID of the searched element.
     * @return The element, which was found by its UUID.
     */
    //@PostAuthorize("hasRole('ROLE_ADMIN') OR hasPermission(returnObject, 'READ')")
    T getElementByUUID(String uuid);

    /**
     * Searches for set of elements of type T by id.
     *
     * @param ids Ids of the searched elements.
     * @return All elements, which were found by there ids.
     */
    Collection<T> getElementsById(Collection<Long> ids);

    /**
     * Returns all elements of Type T in the database.
     *
     * @return All elements of type T within the database.
     */
    @PostFilter("hasRole('ROLE_EDITOR') OR hasPermission(filterObject, 'READ')")
    List<T> getAllElements();

    /**
     * Returns the number of objects of type T.
     *
     * @return The number of objects of type T.
     */
    Long getCount();

    /**
     * Persists a object which is defined in the model class of the given type T. Persists the
     * access control list information for this object.
     *
     * @param element The T object, which will be added or changed in the database.
     */
    // @PreAuthorize("hasRole('ROLE_ADMIN') OR hasPermission(#element, 'WRITE')")
    void merge(T element);

    /**
     * Removes a object which is defined in the model class of the given type T.
     *
     * @param element The T object, which will be removed in the database.
     */
    // @PreAuthorize("hasRole('ROLE_ADMIN') OR hasPermission(#element, 'WRITE')")
    void remove(T element);

    /**
     * Grants the given {@link PermissionType right} for the given element to the given
     * {@link User User}.
     *
     * @param element     The element on which the given {@link PermissionType right} should be
     *                    granted.
     * @param user        The {@link User User} who gets the given {@link PermissionType right}.
     * @param right       The {@link PermissionType right} which should be granted.
     * @param inheritance A flag whether to call {@link #grantInheritedRight grantInheritedRight} or
     *                    not.
     */
    void grantRight(T element, User user, PermissionType right, Boolean inheritance);

    /**
     * Grants the given {@link PermissionType right} for the inherited objects of the given element
     * to the given {@link User User}.
     *
     * @param element The element on whose inherited objects the given {@link PermissionType right}
     *                should be granted.
     * @param user    The {@link User User} who gets the given {@link PermissionType right}.
     * @param right   The {@link PermissionType right} which should be granted.
     */
    void grantInheritedRight(T element, User user, PermissionType right);

    /**
     * Revokes the given {@link PermissionType right} for the given element from the given
     * {@link User User}.
     *
     * @param element     The element on which the given {@link PermissionType right} should be
     *                    revoked.
     * @param user        The {@link User User} who loses the given {@link PermissionType right}.
     * @param right       The {@link PermissionType right} which should be revoked.
     * @param inheritance A flag whether to call {@link #revokeInheritedRight grantInheritedRight}
     *                    or not.
     */
    void revokeRight(T element, User user, PermissionType right, Boolean inheritance);

    /**
     * Revokes the given {@link PermissionType right} for the inherited objects of the given element
     * from the given {@link User User}.
     *
     * @param element The element on whose inherited objects the given {@link PermissionType right}
     *                should be revoked.
     * @param user    The {@link User User} who loses the given {@link PermissionType right}.
     * @param right   The {@link PermissionType right} which should be revoked.
     */
    void revokeInheritedRight(T element, User user, PermissionType right);
}