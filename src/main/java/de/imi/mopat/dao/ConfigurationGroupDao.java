package de.imi.mopat.dao;

import de.imi.mopat.model.ConfigurationGroup;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface ConfigurationGroupDao extends MoPatDao<ConfigurationGroup> {

    /**
     * Get all configuration group objects with the given label message code.
     *
     * @param labelMessageCode The label message code of the searched configuration groups.
     * @return The configuration group objects found by the given label message code.
     */
    List<ConfigurationGroup> getConfigurationGroups(String labelMessageCode);

    /**
     * Returns whether a {@link ConfigurationGroup} is deletable, that means its {ExportTemplate
     * ExportTemplates} are not used  in any {Encounter}.
     *
     * @param configurationGroupId The Id of the {@link ConfigurationGroup}.
     * @return True, if the {@link ConfigurationGroup} is deletable, false otherwise.
     */
    boolean isConfigurationGroupDeletable(Long configurationGroupId);
}
