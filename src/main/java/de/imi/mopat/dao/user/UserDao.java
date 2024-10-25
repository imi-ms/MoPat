package de.imi.mopat.dao.user;

import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import de.imi.mopat.model.user.User;

/**
 * Interface for the data access for objects of type {@link User User}.
 * <p>
 * Provides specific methods for the objects of type {@link User User}.
 */
@Repository
public interface UserDao extends UserManagementDao<User> {

    /**
     * Searches a user by a given username.
     *
     * @param username Username of the searched user.
     * @return The searched user object.
     * @throws UsernameNotFoundException if a User cannot be found by its username.
     * @throws DataAccessException       if there is a problem with the access to data
     */
    User loadUserByUsername(String username)
        throws UsernameNotFoundException, DataAccessException;

    /**
     * Set the password for an user.
     *
     * @param user User, whose password will be set.
     */
    void setPassword(User user);

    /**
     * Set the pin for an user
     * @param user User, whose pin will be set
     */
    void setPin(User user);

    /**
     * @param user     User, whose password will be checked.
     * @param password Given password for this user.
     * @return True if the password is correct for the given user, otherwise false.
     */
    boolean isCorrectPassword(User user, String password);

    /**
     * Checks the pin with the value in the db by using the password encryptor
     * @param user User, whose password will be checked
     * @param pin Given pin for this user.
     * @return True, if pin is correct for the given user, otherwise false.
     */
    boolean isCorrectPin(User user, String pin);

    /**
     * Returns a distinct set containing email addresses of all users.
     *
     * @return A distinct set containing email addresses of all users.
     */
    Set<String> getAllEnabledEMailAddressesDistinct();
}