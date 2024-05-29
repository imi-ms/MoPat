package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.model.ConfigurationGroup;

import java.util.List;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component(value = "ConfigurationGroupDao")
public class ConfigurationGroupDaoImpl extends MoPatDaoImpl<ConfigurationGroup> implements
    ConfigurationGroupDao {

    @Override
    public List<ConfigurationGroup> getConfigurationGroups(final String labelMessageCode) {
        try {
            Query query = moPatEntityManager.createQuery("SELECT c FROM "
                + "ConfigurationGroup c where c.labelMessageCode = :labelMessageCode");
            query.setParameter("labelMessageCode", labelMessageCode);
            List<ConfigurationGroup> configurationGroups = query.getResultList();
            return configurationGroups;
        } catch (PersistenceException e) {
            return null;
        }
    }

    @Override
    public boolean isConfigurationGroupDeletable(final Long configurationGroupId) {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "SELECT count(c) FROM EncounterExportTemplate c WHERE c"
                + ".exportTemplate.id IN (SELECT e.id FROM "
                + "ExportTemplate e WHERE e.configurationGroup.id = " + ":configurationGroupId)",
            Long.class);
        query.setParameter("configurationGroupId", configurationGroupId);
        Long countUsedExportTemplates = query.getSingleResult();
        return countUsedExportTemplates == 0;
    }
}
