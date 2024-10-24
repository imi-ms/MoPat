package de.imi.mopat.dao;

import de.imi.mopat.model.ClinicConfigurationMapping;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public interface ClinicConfigurationMappingDao extends MoPatDao<ClinicConfigurationMapping> {

    public List<ClinicConfigurationMapping> getAllElementsByClinicId(final Integer clinicId);

    public ClinicConfigurationMapping getConfigurationByAttributeAndClass(final Long clinicId, final String attribute,
                                                             final String clazz);

    /**
     * Returns true if registryOfPatient is activated and false otherwise. Get this boolean from the
     * {@link ClinicConfigurationMappingDao} by using the name of this class and the appropriate attribute name.
     *
     * @return The configured registryOfPatient boolean.
     */
    Boolean isRegistryOfPatientActivated(final Long clinicId);

    /**
     * Returns true if UsePatientDataLookup toggle is activated and false otherwise. Get this
     * boolean from the {@link ClinicConfigurationMappingDao} by using the name of this class and the appropriate
     * attribute name.
     *
     * @return The configured usePatientDataLookup boolean.
     */
    Boolean isUsePatientDataLookupActivated(final Long clinicId);

    /**
     * Returns true if PseudonymizationService is activated and false otherwise. Get this boolean
     * from the {@link ClinicConfigurationMappingDao} by using the name of this class and the appropriate
     * attribute name.
     *
     * @return The configured pseudonymizationService boolean.
     */
    Boolean isPseudonymizationServiceActivated(final Long clinicId);
}
