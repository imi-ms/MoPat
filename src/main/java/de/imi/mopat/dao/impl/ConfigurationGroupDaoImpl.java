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
    public ConfigurationGroup getConfigurationGroupByName(final String name) {
        try {
            Query query = moPatEntityManager.createQuery("SELECT c FROM "
                + "ConfigurationGroup c where c.name = :name");
            query.setParameter("name", name);
            return (ConfigurationGroup) query.getSingleResult();
        } catch (PersistenceException e) {
            return null;
        }
    }

    @Override
    public boolean isConfigurationGroupDeletable(final Long configurationGroupId) {
        TypedQuery<Long> existingEncounterExportTemplatesQuery = moPatEntityManager.createQuery(
            "SELECT count(c) FROM EncounterExportTemplate c WHERE c"
                + ".exportTemplate.id IN (SELECT e.id FROM "
                + "ExportTemplate e WHERE e.configurationGroup.id = " + ":configurationGroupId)",
            Long.class);
        existingEncounterExportTemplatesQuery.setParameter("configurationGroupId", configurationGroupId);
        Long countUsedExportTemplates = existingEncounterExportTemplatesQuery.getSingleResult();
        
        TypedQuery<Long> existingClinicConfigurationMappingsQuery = moPatEntityManager.createQuery(
            "SELECT count (ccgm) FROM ClinicConfigurationGroupMapping ccgm WHERE ccgm" +
            ".configurationGroup.id = " + ":configurationGroupId",
            Long.class);
        existingClinicConfigurationMappingsQuery.setParameter("configurationGroupId", configurationGroupId);
        Long countExistingClinicConfigurationGroupMappings = existingClinicConfigurationMappingsQuery.getSingleResult();
        
        return countUsedExportTemplates == 0 && countExistingClinicConfigurationGroupMappings == 0;
    }
}
