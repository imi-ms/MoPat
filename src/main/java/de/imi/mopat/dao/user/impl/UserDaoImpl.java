package de.imi.mopat.dao.user.impl;

import de.imi.mopat.auth.PepperedBCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.User;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the interface UserDao, which provides specific methods for the objects of type
 * user.
 * <p>
 * Implements specific methods for the objects of type user.
 */
@Repository
public class UserDaoImpl extends UserManagementDaoImpl<User> implements UserDao {

    @Autowired
    private PepperedBCryptPasswordEncoder passwordEncoder;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    @Transactional("MoPat_User")
    public User loadUserByUsername(final String username)
        throws UsernameNotFoundException, DataAccessException {
        // Get the user from database
        TypedQuery<User> query = moPatUserEntityManager.createQuery(
            "SELECT u FROM User u WHERE u.username = '" + username.toLowerCase() + "'", User.class);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void setPassword(final User user) {
        // Set the new BCrypt encoded password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @Override
    public void setPin(User user) {
        user.setPin(passwordEncoder.encode(user.getPin()));
    }

    @Override
    public boolean isCorrectPassword(final User user, final String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public boolean isCorrectPin(User user, String pin) {
        return passwordEncoder.matches(pin, user.getPin());
    }


    /* (non-Javadoc)
     * @see de.imi.mopat.dao.UserDao#getAllEnabledEMailAddressesDistinct()
     */
    @Override
    @Transactional("MoPat_User")
    public Set<String> getAllEnabledEMailAddressesDistinct() throws DataAccessException {
        TypedQuery<String> query = moPatUserEntityManager.createQuery(
            "SELECT DISTINCT u.email FROM User u WHERE u.isEnabled = true", String.class);
        return new HashSet<>(query.getResultList());
    }
}
