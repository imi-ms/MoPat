package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.ClinicConfigurationGroupMapping;
import de.imi.mopat.model.Configuration;

import java.io.IOException;
import java.util.Locale;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

/**
 *
 */
// The value makes this dao reacheable in the jsp's
@Component(value = "ConfigurationDao")
public class ConfigurationDaoImpl extends MoPatDaoImpl<Configuration> implements ConfigurationDao {

    @Override
    public Configuration getConfigurationByAttributeAndClass(final String attribute,
        final String clazz) {
        try {
            TypedQuery<Configuration> query = moPatEntityManager.createQuery(
                "SELECT c FROM Configuration c WHERE c.attribute = '" + attribute
                    + "' AND c.entityClass = '" + clazz + "'", getEntityClass());

            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    @Override
    public Configuration getConfigurationByGroupName(final Long clinicId, final String attribute,
        final String clazz, final String groupName) {
        try {
            String mappingIdJpql = "SELECT ccm.id FROM ClinicConfigurationMapping ccm " +
                "JOIN ccm.clinic c " +
                "JOIN ccm.clinicConfiguration cc " +
                "WHERE c.id = :clinicId " +
                "AND cc.mappedConfigurationGroup = :mappedGroupName";

            TypedQuery<Long> mappingIdQuery = moPatEntityManager.createQuery(mappingIdJpql, Long.class);
            mappingIdQuery.setParameter("clinicId", clinicId);
            mappingIdQuery.setParameter("mappedGroupName", groupName);

            Long clinicConfigurationMappingId = mappingIdQuery.getSingleResult();

            String groupIdJpql = "SELECT ccgm FROM ClinicConfigurationGroupMapping ccgm " +
                "WHERE ccgm.clinicConfigurationMapping.id = :mappingId";

            TypedQuery<ClinicConfigurationGroupMapping> groupIdQuery = moPatEntityManager.createQuery(groupIdJpql, ClinicConfigurationGroupMapping.class);
            groupIdQuery.setParameter("mappingId", clinicConfigurationMappingId);

            ClinicConfigurationGroupMapping clinicConfigurationGroupMapping = groupIdQuery.getSingleResult();
            Long configurationGroupId = clinicConfigurationGroupMapping.getConfigurationGroup().getId();

            String configurationsJpql = "SELECT conf FROM Configuration conf " +
                "WHERE conf.configurationGroup.id = :groupId " +
                "AND conf.attribute = :attribute " +
                "AND conf.entityClass = :clazz";

            TypedQuery<Configuration> configurationsQuery = moPatEntityManager.createQuery(configurationsJpql, Configuration.class);
            configurationsQuery.setParameter("groupId", configurationGroupId);
            configurationsQuery.setParameter("attribute", attribute);
            configurationsQuery.setParameter("clazz", clazz);

            return configurationsQuery.getSingleResult();
        } catch (NoResultException e) {
            return null; // Return an empty list if no results are found
        }
    }

    @Override
    public String getBaseURL() {
        Configuration configuration = getConfigurationByAttributeAndClass(Constants.BASE_URL,
            Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public String getDefaultLanguage() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.DEFAULT_LANGUAGE, Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public Locale getDefaultLocale() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.DEFAULT_LANGUAGE, Constants.CLASS_GLOBAL);
        String defaultLanguage = configuration.getValue();
        String[] localeSplit = defaultLanguage.split("_");
        Locale defaultLanguageLocale = new Locale(localeSplit[0]);
        if (localeSplit.length == 2) {
            defaultLanguageLocale = new Locale(localeSplit[0], localeSplit[1]);
        }
        return defaultLanguageLocale;
    }

    @Override
    public String getObjectStoragePath() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.OBJECT_STORAGE_PATH_PROPERTY, Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public String getLogo() {
        Configuration configuration = getConfigurationByAttributeAndClass(
                Constants.LOGO_PROPERTY,
                Constants.CLASS_GLOBAL);
        if(configuration.getValue()!=null){
            String realPath = this.getImageUploadPath() + configuration.getValue();
            String fileName = realPath.substring(realPath.lastIndexOf("/"));
            try {
                return StringUtilities.convertImageToBase64String(realPath, fileName);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getLogoPath(){
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.LOGO_PROPERTY,
            Constants.CLASS_GLOBAL);

        return configuration.getValue();
    }

    @Override
    public String getSupportEMail() {
        Configuration configuration = getConfigurationByAttributeAndClass(Constants.SUPPORT_MAIL,
            Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public String getSupportPhone() {
        Configuration configuration = getConfigurationByAttributeAndClass(Constants.SUPPORT_PHONE,
            Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public Long getFinishedEncounterTimeWindow() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.FINISHED_ENCOUNTER_TIME_WINDOW_IN_MILLIS, Constants.CLASS_GLOBAL);
        return Long.valueOf(configuration.getValue());
    }

    @Override
    public Long getIncompleteEncounterTimeWindow() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.INCOMPLETE_ENCOUNTER_TIME_WINDOW_IN_MILLIS, Constants.CLASS_GLOBAL);
        return Long.valueOf(configuration.getValue());
    }

    @Override
    public Long getFinishedEncounterScheduledTimeWindow() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.FINISHED_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS, Constants.CLASS_GLOBAL);
        return Long.valueOf(configuration.getValue());
    }

    @Override
    public String getImageUploadPath() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.IMAGE_UPLOAD_PATH,
            Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public String getFHIRsystemURI() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.FHIR_SYSTEM_URL,
            Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public String getWebappRootPath() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.WEBBAPP_ROOT_PATH,
            Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public Long getIncompleteEncounterScheduledTimeWindow() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.INCOMPLETE_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS, Constants.CLASS_GLOBAL);
        return Long.valueOf(configuration.getValue());
    }

    @Override
    public Long getFinishedEncounterMailaddressTimeWindow() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.FINISHED_ENCOUNTER_MAILADDRESS_TIME_WINDOW_IN_MILLIS, Constants.CLASS_GLOBAL);
        return Long.valueOf(configuration.getValue());
    }

    @Override
    public String getMetadataExporterODMOID() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.METADATA_EXPORTER_ODM_OID, Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public String getMetadataExporterPDF() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.METADATA_EXPORTER_PDF, Constants.CLASS_GLOBAL);
        return configuration.getValue();
    }

    @Override
    public Boolean isRegistryOfPatientActivated() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.REGISTER_PATIENT_DATA, Constants.CLASS_GLOBAL);
        return Boolean.valueOf(configuration.getValue());
    }

    @Override
    public Boolean isUsePatientDataLookupActivated() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.USE_PATIENT_DATA_LOOKUP, Constants.CLASS_GLOBAL);
        return Boolean.valueOf(configuration.getValue());
    }

    @Override
    public Boolean isPseudonymizationServiceActivated() {
        Configuration configuration = getConfigurationByAttributeAndClass(
            Constants.USE_PSEUDONYMIZATION_SERVICE, Constants.CLASS_GLOBAL);
        return Boolean.valueOf(configuration.getValue());
    }
}
