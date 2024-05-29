package de.imi.mopat.dao.user.impl;

import org.springframework.stereotype.Repository;
import de.imi.mopat.dao.user.InvitationDao;
import de.imi.mopat.model.user.Invitation;

/**
 * Implementation of the interface {@link InvitationDao}, which provides specific methods for the
 * objects of type {@link Invitation}.
 * <p>
 * Implements specific methods for the objects of type {@link Invitation}.
 */
@Repository
public class InvitationDaoImpl extends UserManagementDaoImpl<Invitation> implements InvitationDao {

}
