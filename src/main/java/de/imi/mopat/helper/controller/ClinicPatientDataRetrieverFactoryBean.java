package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ClinicConfigurationMappingDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Configuration;
import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory to look up, instantiate and provide an implementation of {@link PatientDataRetriever} via the
 * {@link ClinicPatientDataRetrieverFactoryBean#getObject()} method. Checks the configuration whether a
 * {@link PatientDataRetriever} has been activated. If yes, searches for a class given in the configuration and tries to
 * instantiate it (the implementation itself has to gather and use the information it needs). If everything worked, the
 * implementation is provided. Otherwise, it just returns <code>null</code>.
 *
 * @version 1.0
 */
public class ClinicPatientDataRetrieverFactoryBean implements FactoryBean<PatientDataRetriever> {

    @Autowired
    private ClinicConfigurationMappingDao clinicConfigurationMappingDao;

    @Autowired
    private ConfigurationDao configurationDao;

    private Long clinicId;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ClinicPatientDataRetrieverFactoryBean.class);

    private final String className = this.getClass().getName();

    public final String usePatientDataLookupProperty = "usePatientDataLookup";
    public final String usePatientDataLookupGroupName = "configurationGroup.label.usePatientLookUp";
    public final String patientDataRetrieverClassProperty = "patientDataRetrieverClass";

    public ClinicPatientDataRetrieverFactoryBean() {

    }

    public ClinicPatientDataRetrieverFactoryBean(Long clinicId) {
        this.clinicId = clinicId;
    }

    @Override
    public PatientDataRetriever getObject() throws Exception {
        PatientDataRetriever result = null;
        Boolean activated = clinicConfigurationMappingDao.isUsePatientDataLookupActivated(clinicId);

        LOGGER.info("[SETUP] Checking whether a property ({}) for activation of "
            + "patient data retrieving is given...[DONE]", usePatientDataLookupProperty);
        LOGGER.info("[SETUP] activation of PatientDataRetriever configuration "
            + "found. Activation is set to {}. If this is not "
            + "expected, beware that only 'true' is detected as " + "activation", activated);

        if (activated) {
            String patientDataRetrieverImplementationClass = getPatientRetrieverClass(clinicId);
            LOGGER.info("[SETUP] Checking whether a property ({}) for the patient"
                + " data retriever is given...", patientDataRetrieverClassProperty);
            if (patientDataRetrieverImplementationClass == null) {
                LOGGER.error("[SETUP] no class configuration found. Please provide"
                    + " a value for {} in the configuration. No "
                    + "Patient Data Retriever will be used", patientDataRetrieverClassProperty);
            } else {
                LOGGER.info("[SETUP] Checking whether a property ({}) for the "
                        + "patient data retriever is given...[DONE]",
                    patientDataRetrieverClassProperty);
                try {
                    Class<?> patientDataRetrieverClass = Class.forName(
                        patientDataRetrieverImplementationClass);
                    LOGGER.info(
                        "[SETUP] Implementation of PatientDataRetriever " + "found. Using {}",
                        patientDataRetrieverClass.getCanonicalName());
                    Object newPatientDataRetriever = patientDataRetrieverClass.getDeclaredConstructor()
                        .newInstance();
                    PatientDataRetriever patientDataRetriever = (PatientDataRetriever) newPatientDataRetriever;
                    result = patientDataRetriever;
                } catch (Exception e) {
                    LOGGER.info("[SETUP] No implementation found with the name {}"
                            + ". Please ensure that the " + "PatientDataRetriever implementation is "
                            + "correctly spelled and available. Use " + "debug output for more info",
                        patientDataRetrieverImplementationClass);
                    LOGGER.debug("[SETUP] Reason: {}", e);
                }
            }
        }

        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    private String getPatientRetrieverClass(Long clinicId) {
        Configuration configuration = configurationDao.getConfigurationByGroupName(
            clinicId, patientDataRetrieverClassProperty, className, usePatientDataLookupGroupName);
        return configuration.getValue();
    }

    private void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }
}
