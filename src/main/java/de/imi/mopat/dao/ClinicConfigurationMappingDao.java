package de.imi.mopat.dao;

import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.ClinicConfigurationMapping;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public interface ClinicConfigurationMappingDao extends MoPatDao<ClinicConfigurationMapping> {

    public List<ClinicConfigurationMapping> getAllElementsByClinicId(final Integer clinicId);

}
