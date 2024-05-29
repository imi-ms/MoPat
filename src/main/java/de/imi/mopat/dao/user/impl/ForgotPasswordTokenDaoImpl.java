package de.imi.mopat.dao.user.impl;

import de.imi.mopat.dao.user.ForgotPasswordTokenDao;
import de.imi.mopat.model.user.ForgotPasswordToken;
import de.imi.mopat.model.user.User;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the interface {@link ForgotPasswordTokenDao}, which provides specific methods
 * for the objects of type {@link ForgotPasswordToken}.
 * <p>
 * Implements specific methods for the objects of type {@link ForgotPasswordToken}.
 */
@Repository
public class ForgotPasswordTokenDaoImpl extends
    UserManagementDaoImpl<ForgotPasswordToken> implements ForgotPasswordTokenDao {

    @Override
    @Transactional("MoPat_User")
    public ForgotPasswordToken getElementByUser(final User user) {
        try {
            TypedQuery<ForgotPasswordToken> query = moPatUserEntityManager.createQuery(
                "SELECT f FROM ForgotPasswordToken f WHERE f.user=:user",
                ForgotPasswordToken.class);
            query.setParameter("user", user);
            ForgotPasswordToken element = query.getSingleResult();
            return element;
        } catch (NoResultException e) {
            return null;
        }
    }
}
