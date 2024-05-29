package de.imi.mopat.dao.user;

import de.imi.mopat.model.user.Invitation;
import org.springframework.stereotype.Repository;

/**
 * Interface for the data access for objects of type {@link Invitation}.
 * <p>
 * Provides specific methods for the objects of type {@link Invitation}.
 */
@Repository
public interface InvitationDao extends UserManagementDao<Invitation> {

}
