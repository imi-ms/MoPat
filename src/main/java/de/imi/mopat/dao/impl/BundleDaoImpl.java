package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.model.Bundle;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class BundleDaoImpl extends MoPatDaoImpl<Bundle> implements BundleDao {

    @Override
    public boolean isBundleNameUnused(final String name, final Long id) {
        try {
            Query query = moPatEntityManager.createQuery(
                "SELECT b FROM " + "Bundle b " + "WHERE b" + ".name='" + name + "'");
            Bundle bundle = (Bundle) query.getSingleResult();
            // If there is a result, check if it is the same bundle that
            // should be edit
            return id != null && bundle.getId().longValue() == id.longValue();
        } catch (NoResultException e) {
            return true;
        }
    }


}
