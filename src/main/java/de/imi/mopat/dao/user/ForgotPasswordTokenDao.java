package de.imi.mopat.dao.user;

import de.imi.mopat.model.user.ForgotPasswordToken;
import de.imi.mopat.model.user.User;
import org.springframework.stereotype.Repository;

/**
 * Interface for the data access for objects of type {@link ForgotPasswordToken}.
 * <p>
 * Provides specific methods for the objects of type {@link ForgotPasswordToken}.
 */
@Repository
public interface ForgotPasswordTokenDao extends UserManagementDao<ForgotPasswordToken> {

    /**
     * Searches for a element of type {@link ForgotPasswordToken} by user.
     *
     * @param user User, which has requested the searched {@link ForgotPasswordToken}.
     * @return The {@link ForgotPasswordToken}, which was found by its user.
     */
    ForgotPasswordToken getElementByUser(User user);
}