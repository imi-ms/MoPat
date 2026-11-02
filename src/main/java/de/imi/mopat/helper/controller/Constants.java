package de.imi.mopat.helper.controller;

import java.text.SimpleDateFormat;

/**
 * A class to collect all the constant variables shared between different classes within the
 * mopat.helper.controller package
 */
public abstract class Constants {

    public static final String CONFIGURATION = "configuration";
    public static final String EXPORT_TEMPLATE_SUB_DIRECTORY = "exporttemplates";
    public static final String FHIR_VALIDATION_SCHEMA_SUB_DIRECTORY_DSTU3 = "fhir" + "-dstu3-xsd";
    public static final String FHIR_VALIDATION_SCHEMA_SUB_DIRECTORY_R4B = "fhir" + "-r4b-xsd";
    public static final String FHIR_VALIDATION_SCHEMA_SUB_DIRECTORY_R5 = "fhir" + "-r5-xsd";
    public static final String IMAGE_QUESTIONNAIRE = "questionnaire";
    public static final int PASSWORD_MINIMUM_SIZE = 8;
    public static final int PASSWORD_MAXIMUM_SIZE = 255;
    public static final int PIN_MINIMUM_SIZE = 6;
    public static final int PIN_MAX_TRIES = 3;

    // Decimal format. Matches for eg.: 100; 1,1; 1,1; 321,2
    public static final String DECIMAL_FORMAT = "\\d+[,|\\.]{0,1}\\d{0,1}";

    // Number format. Matches for eg.: 100; 1;
    public static final String NUMBER_FORMAT = "\\d+";

    // Date format. For eg.: 2013/05/12
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    //Paths to body part images
    public static final String BODY_FRONT = "images/body_front.svg";
    public static final String BODY_BACK = "images/body_back.svg";
    public static final String[] BODY_PART_IMAGE_TYPES = {"FRONT", "BACK", "FRONT_BACK"};

    // Configuration: Global class for configuration properties that are used
    // more than in one class.
    public static final String CLASS_GLOBAL = "GLOBAL";

    // Configuration: The name of the validation schema files
    public static final String SCHEMA_QUESTIONNAIRE_FILE = "questionnaire.xsd";
    public static final String SCHEMA_QUESTIONNAIRE_RESPONSE_FILE = "questionnaireResponse.xsd";
    // Configuration: The name of the attribute for the logo
    public static final String LOGO_PROPERTY = "logo";
    // Configuration: The name of the attribute for the object storage path.
    // E.g.: Templates
    public static final String OBJECT_STORAGE_PATH_PROPERTY = "objectStoragePath";
    // Configuration: The name of the attribute for the email address of the
    // support
    public static final String SUPPORT_MAIL = "supportMail";
    // Configuration: The name of the attribute for the phone number of the
    // support
    public static final String SUPPORT_PHONE = "supportPhone";
    // Configuration: The name of the attribute for the default language of
    // the application
    public static final String DEFAULT_LANGUAGE = "defaultLanguage";
    // Configuration: The name of the attribute for the usePatientDataLookup
    public static final String USE_PATIENT_DATA_LOOKUP = "usePatientDataLookup";
    // Configuration: The name of the attribute for the
    // finishedEncounterTimeWindowInMillis
    public static final String FINISHED_ENCOUNTER_TIME_WINDOW_IN_MILLIS = "finishedEncounterTimeWindowInMillis";
    // Configuration: The name of the attribute for the
    // incompleteEncounterTimeWindowInMillis
    public static final String INCOMPLETE_ENCOUNTER_TIME_WINDOW_IN_MILLIS = "incompleteEncounterTimeWindowInMillis";
    // Configuration: The name of the attribute for the
    // finishedEncounterScheduledTimeWindowInMillis
    public static final String FINISHED_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS = "finishedEncounterScheduledTimeWindowInMillis";
    // Configuration: The name of the attribute for the
    // incompleteEncounterScheduledTimeWindowInMillis
    public static final String INCOMPLETE_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS = "incompleteEncounterScheduledTimeWindowInMillis";
    // Configuration: The name of the attribute for the
    // finishedEncounterMailadressTimeWindowInMillis
    public static final String FINISHED_ENCOUNTER_MAILADDRESS_TIME_WINDOW_IN_MILLIS = "finishedEncounterMailaddressTimeWindowInMillis";
    // Configuration: The name of the attribute for the pseudonymizationService
    public static final String USE_PSEUDONYMIZATION_SERVICE = "usePseudonymizationService";
    // Configuration: The name of the attribute for the registryOfPatient
    public static final String REGISTER_PATIENT_DATA = "registerPatientData";
    // Configuration: The name of the attribute for the baseUrl
    public static final String BASE_URL = "baseUrl";
    // Configuration: The name of the attribute for the ODM base OID
    public static final String METADATA_EXPORTER_ODM_OID = "metadataExporterODMOID";
    // Configuration: The name of the attribute for the ODM to PDF exporter
    public static final String METADATA_EXPORTER_PDF = "metadataExporterPDF";

    public static final String IMAGE_UPLOAD_PATH= "imageUploadPath";

    public static final String FHIR_SYSTEM_URL= "FHIRsystemURI";

    public static final String WEBBAPP_ROOT_PATH = "webappRootPath";

    public static final String ENABLE_GLOBAL_PIN_AUTH = "enableGlobalPinAuth";

    // Configuration: The name of the attribute for the Imprint text message
    public static final String IMPRINT_TEXT = "imprintText";
}