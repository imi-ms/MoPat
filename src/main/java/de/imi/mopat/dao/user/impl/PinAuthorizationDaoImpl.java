package de.imi.mopat.dao.user.impl;

import de.imi.mopat.dao.user.PinAuthorizationDao;
import de.imi.mopat.model.user.PinAuthorization;
import de.imi.mopat.model.user.User;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PinAuthorizationDaoImpl extends UserManagementDaoImpl<PinAuthorization> implements PinAuthorizationDao  {

    @Override
    public boolean isPinAuthActivatedForUser(User user) {
        Query query = moPatUserEntityManager.createQuery(
            "SELECT count(pa) " + "FROM " + "PinAuthorization " + "pa" + " where pa" + ".user" + " "
                + "= :user");
        query.setParameter("user", user);
        Long result = (Long) query.getSingleResult();
        return result > 0;
    }

    @Override
    public Set<PinAuthorization> getEntriesForUser(User user) {
        TypedQuery<PinAuthorization> query = moPatUserEntityManager.createQuery(
            "SELECT pa FROM PinAuthorization pa WHERE pa.user = :user", PinAuthorization.class);
        query.setParameter("user", user);
        return new HashSet<>(query.getResultList());
    }
}
