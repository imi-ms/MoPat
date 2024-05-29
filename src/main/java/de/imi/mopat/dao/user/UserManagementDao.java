package de.imi.mopat.dao.user;

import de.imi.mopat.dao.MoPatDao;
import org.springframework.stereotype.Repository;

/**
 * Generic interface, which is implemented by all data access objects (daos), which implements user
 * specified operations.
 * <p>
 * Provides specific methods suitable for daos with user specified operations.
 */
@Repository
public interface UserManagementDao<T> extends MoPatDao<T> {

    /**
     * Persists a object which is defined in the model class of the given type T.
     *
     * @param element The T object, added to the database.
     */
    void persist(T element);
}