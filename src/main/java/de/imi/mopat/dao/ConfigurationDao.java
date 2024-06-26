package de.imi.mopat.dao;

import de.imi.mopat.model.Configuration;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface ConfigurationDao extends MoPatDao<Configuration> {

    /**
     * Get a configuration object by a given attribute and class
     *
     * @param attribute The name of the searched configuration attribute.
     * @param clazz     The class of the searched configuration attribute.
     * @return The configuration object found by the given attribute and class
     */
    Configuration getConfigurationByAttributeAndClass(String attribute, String clazz);

    /**
     * Returns the base url of the application.
     *
     * @return The base url as a string.
     */
    String getBaseURL();

    /**
     * Returns the default language of the application.
     *
     * @return The default language as a string (i.e. en).
     */
    String getDefaultLanguage();

    /**
     * Returns the default {@link java.util.Locale} of the application.
     *
     * @return The default {@link java.util.Locale}.
     */
    Locale getDefaultLocale();

    /**
     * Returns the path, where objects (templates...) are saved.
     *
     * @return The path, where objects are saved.
     */
    String getObjectStoragePath();

    /**
     * Returns the logo base64 of the MoPat application.
     *
     * @return The logo base64 of the MoPat application.
     */
    public String getLogo();

    /**
     * Returns the logo path of the MoPat application.
     *
     * @return The logo path of the MoPat application.
     */
    public String getLogoPath();

    /**
     * Returns the email address for the MoPat support.
     *
     * @return The email address for the MoPat support.
     */
    String getSupportEMail();

    /**
     * Returns the phone number for the MoPat support.
     *
     * @return The phone number for the MoPat support.
     */
    String getSupportPhone();

    /**
     * Returns the time window after finished encounters should be deleted in millis from the
     * {@link ConfigurationDao} by using the name of this class and the appropriate attribute name.
     *
     * @return The time window for finished encounters in millis as a long.
     */
    Long getFinishedEncounterTimeWindow();

    /**
     * Returns the time window after incomplete encounters should be deleted in millis from the
     * {@link ConfigurationDao} by using the name of this class and the appropriate attribute name.
     *
     * @return The time window for incomplete encounters in millis as a long.
     */
    Long getIncompleteEncounterTimeWindow();

    /**
     * Returns the time window after which the mail address of finished encounters should be deleted
     * in millis from the {@link ConfigurationDao} by using the name of this class and the
     * appropriate attribute name.
     *
     * @return The time window for deletion of mail addresses of finished encounters in millis as a
     * long.
     */
    Long getFinishedEncounterMailaddressTimeWindow();

    /**
     * Returns the base OID for the ODM metadata exporter.
     *
     * @return The base OID for the ODM metadata exporter.
     */
    String getMetadataExporterODMOID();

    /**
     * Returns the URL of the ODM to PDF converter.
     *
     * @return The URL of the ODM to PDF converter.
     */
    String getMetadataExporterPDF();

    /**
     * Returns true if registryOfPatient is activated and false otherwise. Get this boolean from the
     * {@link ConfigurationDao} by using the name of this class and the appropriate attribute name.
     *
     * @return The configured registryOfPatient boolean.
     */
    Boolean isRegistryOfPatientActivated();

    /**
     * Returns true if UsePatientDataLookup toggle is activated and false otherwise. Get this
     * boolean from the {@link ConfigurationDao} by using the name of this class and the appropriate
     * attribute name.
     *
     * @return The configured usePatientDataLookup boolean.
     */
    Boolean isUsePatientDataLookupActivated();

    /**
     * Returns true if PseudonymizationService is activated and false otherwise. Get this boolean
     * from the {@link ConfigurationDao} by using the name of this class and the appropriate
     * attribute name.
     *
     * @return The configured pseudonymizationService boolean.
     */
    Boolean isPseudonymizationServiceActivated();

    /**
     * Returns the time window after incomplete encounterScheduleds should be deleted in millis from
     * the {@link ConfigurationDao} by using the name of this class and the appropriate attribute
     * name.
     *
     * @return The time window for incomplete encounters in millis as a long.
     */
    Long getIncompleteEncounterScheduledTimeWindow();

    /**
     * Returns the time window after finished encounterScheduleds should be deleted in millis from
     * the {@link ConfigurationDao} by using the name of this class and the appropriate attribute
     * name.
     *
     * @return The time window for incomplete encounters in millis as a long.
     */
    Long getFinishedEncounterScheduledTimeWindow();
    

    /**
     * Returns the path, where image are saved.
     *
     * @return The path, where images are saved.
     */
    public String getImageUploadPath();

    /**
     * Returns the system url.
     *
     * @return system url for fhir export.
     */
    public String getFHIRsystemURI();

    /**
     * Returns the deployed webapp ROOT path
     *
     * @return deployed webapp ROOT path.
     */
    public String getWebappRootPath();
}
