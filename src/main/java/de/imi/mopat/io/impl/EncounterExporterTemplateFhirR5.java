package de.imi.mopat.io.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.io.importer.fhir.FhirR5Helper;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.DecimalType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.hl7.fhir.r5.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;
import org.hl7.fhir.r5.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.StringType;

/**
 *
 */
public class EncounterExporterTemplateFhirR5 implements EncounterExporterTemplate {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(EncounterExporterTemplateFhirR5.class);
    private static final SimpleDateFormat FILENAMEDATEFORMAT = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss");

    private final ConfigurationDao configurationDao;

    private Encounter encounter;
    private ExportTemplate exportTemplate;
    private QuestionnaireResponse questionnaireResponse;

    public EncounterExporterTemplateFhirR5(final ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    @Override
    public void load(final Encounter encounter, final ExportTemplate exportTemplate) throws Exception {

        this.encounter = encounter;
        this.exportTemplate = exportTemplate;

        String objectStoragePath = configurationDao.getObjectStoragePath();
        if (objectStoragePath == null) {
            LOGGER.error(
                    "[SETUP] No object storage path found. Please provide a " + "value for {} in the {} file",
                    Constants.OBJECT_STORAGE_PATH_PROPERTY,
                    Constants.CONFIGURATION);
        } else {
            LOGGER.info("[SETUP] Object storage path configuration found.");
        }
        LOGGER.info(
                "[SETUP] Accessing properties file to look up the export " + "path" + " in  {}...[DONE]",
                Constants.CONFIGURATION);

        String templatePath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
        String filename = exportTemplate.getFilename();
        File file = new File(templatePath, filename);

        // Create questionnaireResponse and set patientID and caseNumber
        questionnaireResponse = FhirR5Helper.getQuestionnaireResponse(
                (Questionnaire) FhirR5Helper.parseResourceFromFile(new FileInputStream(file)));
        Patient patient = new Patient();
        patient.addIdentifier(new Identifier().setValue(encounter.getCaseNumber()));
        questionnaireResponse.addContained(patient);
        questionnaireResponse.setSource(new Reference(patient));
    }

    @Override
    public void write(final String exportField, final String value) throws Exception {
        String exportClean = exportField.replace("u002E", ".");
        // Split the exportField into splitExportField[0] (item.linkId)
        // and splitExportField[1] (option.code) or boolean value
        String[] splitExportField = exportClean.split("_");
        for (int i = 0; i < splitExportField.length; i++) {
            splitExportField[i] = splitExportField[i].replace("u005F", "_");
        }

        // Search all answers for the exportField and write the value
        for (QuestionnaireResponseItemAnswerComponent answer :
                FhirR5Helper.getAllAnswersOfQuestionnaireResponse(questionnaireResponse)) {
            if (value != null && !value.isEmpty()) {
                if (answer.getId().equalsIgnoreCase(splitExportField[0])) {
                    if (splitExportField.length > 1 && splitExportField[1].equals("true") && value.equals("TRUE")) {
                        answer.setValue(new BooleanType(Boolean.TRUE));
                        LOGGER.info(
                                "ExportField found. Value of answer '" + answer.getId() + "' set to '" + value + "'.");
                    } else if (splitExportField.length > 1
                            && splitExportField[1].equals("false")
                            && value.equals("TRUE")) {
                        answer.setValue(new BooleanType(Boolean.FALSE));
                        LOGGER.info(
                                "ExportField found. Value of answer '" + answer.getId() + "' set to '" + value + "'.");
                    } else if (splitExportField.length > 1 && splitExportField[1].equals("freetext")) {
                        answer.setValue(new StringType(value));
                        LOGGER.info(
                                "ExportField found. Value of answer '" + answer.getId() + "' set to '" + value + "'.");
                    } else if (answer.getValue() instanceof Coding) {
                        answer.setValue(new Coding().setDisplay(value));
                        LOGGER.info(
                                "ExportField found. Value of answer '" + answer.getId() + "' set to '" + value + "'.");
                    } else if (answer.getValue() instanceof DateType) {
                        try {
                            answer.setValue(new DateType(new SimpleDateFormat("yyyy-MM-dd").parse(value)));
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId() + "' set" + " to '"
                                    + new SimpleDateFormat("yyyy-MM-dd").parse(value) + "'.");
                        } catch (ParseException e) {
                            LOGGER.info("ExportField could not be written. Value " + "is invalid. {}", e.getMessage());
                            answer.setValue(null);
                        }
                    } else if (answer.getValue() instanceof DecimalType) {
                        answer.setValue(new DecimalType(Double.parseDouble(value)));
                        LOGGER.info(
                                "ExportField found. Value of answer '" + answer.getId() + "' set to '" + value + "'.");
                    } else if (answer.getValue() instanceof IntegerType) {
                        Double doubleValue = null;
                        try {
                            doubleValue = Double.parseDouble(value);
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId() + "' set" + " to '"
                                    + value + "'.");
                        } catch (NumberFormatException e) {
                            LOGGER.info("ExportField could not be written. Value " + "is invalid. {}", e.getMessage());
                            answer.setValue(null);
                        }
                        if (doubleValue != null) {
                            answer.setValue(new IntegerType(doubleValue.intValue()));
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId() + "' set" + " to '"
                                    + value + "'.");
                        } else {
                            try {
                                answer.setValue(new IntegerType(Integer.parseInt(value)));
                                LOGGER.info("ExportField found. Value of " + "answer '" + answer.getId() + "' set to '"
                                        + value + "'.");
                            } catch (NumberFormatException e) {
                                LOGGER.info(
                                        "ExportField could not be written. " + "Value is invalid. {}", e.getMessage());
                                answer.setValue(null);
                                break;
                            }
                        }
                    } else if (answer.getValue() instanceof StringType) {
                        try {
                            answer.setValue(new StringType(value));
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId() + "' set" + " to '"
                                    + value + "'.");
                        } catch (Exception e) {
                            LOGGER.info("ExportField could not be written. Value " + "is invalid. {}", e.getMessage());
                            answer.setValue(null);
                        }
                    }
                    // For multiple choice questions the exportFields also
                    // save the items linkId.
                    // Thus, the second item of splitExportFields contains
                    // the answer id.
                } else if (splitExportField.length > 1
                        && splitExportField[1].equals(answer.getId())
                        && value.equals("TRUE")) {
                    answer.setValue(new BooleanType(Boolean.parseBoolean(value)));
                    LOGGER.info("ExportField found. Value of answer '" + answer.getId() + "' set to '" + value + "'.");
                }
            } else {
                answer.setValue(null);
                LOGGER.info(
                        "Value was null or empty. Value of export field " + "[" + exportField + "] was set to null.");
                return;
            }
        }
    }

    @Override
    public ExportStatus flush() throws Exception {
        Boolean exportToDirectory = null;
        Boolean exportViaREST = null;
        String exportPath = null;
        String exportUrl = null;
        Boolean exportViaHL7 = null;
        String hl7Hostname = null;
        Integer hl7Port = null;
        String sendingFacility = null;
        String receivingApplication = null;
        String receivingFacility = null;
        String obrFillerOrderNumber = null;
        for (Configuration configuration :
                exportTemplate.getConfigurationGroup().getConfigurations()) {
            switch (configuration.getAttribute()) {
                case "exportInDirectory":
                    exportToDirectory = Boolean.parseBoolean(configuration.getValue());
                    break;
                case "exportPath":
                    exportPath = configuration.getValue();
                    break;
                case "exportViaCommunicationServer":
                    exportViaREST = Boolean.parseBoolean(configuration.getValue());
                    break;
                case "exportUrl":
                    exportUrl = configuration.getValue();
                    break;
                case "exportFHIRViaHL7v2":
                    exportViaHL7 = Boolean.parseBoolean(configuration.getValue());
                    break;
                case "FHIRViaHL7v2Host":
                    hl7Hostname = configuration.getValue();
                    break;
                case "FHIRViaHL7v2Port":
                    try {
                        hl7Port = Integer.parseInt(configuration.getValue());
                    } catch (NumberFormatException numberFormatException) {
                        hl7Port = null;
                    }
                    break;
                case "FHIRViaHL7v2SendingFacility":
                    sendingFacility = configuration.getValue();
                    break;
                case "FHIRViaHL7v2ReceivingApplication":
                    receivingApplication = configuration.getValue();
                    break;
                case "FHIRViaHL7v2ReceivingFacility":
                    receivingFacility = configuration.getValue();
                    break;
                case "FHIRViaHL7v2OBRFillerOrderNumber":
                    obrFillerOrderNumber = configuration.getValue();
                    break;
                default:
                    break;
            }
        }

        questionnaireResponse.setStatus(QuestionnaireResponseStatus.COMPLETED);

        ExportStatus exportStatus = ExportStatus.SUCCESS;

        if (exportToDirectory != null && exportToDirectory && exportPath != null) {
            try {
                File path = new File(exportPath);
                if (!path.isDirectory()) {
                    path.mkdirs();
                }

                // Create a sub-directory for the exported files
                String filepath = exportPath + File.separator
                        + exportTemplate.getQuestionnaire().getName().replaceAll(":", "_") + "/"
                        + exportTemplate.getName().replaceAll(":", "_")
                        + "/";
                File subDirectory = new File(filepath);
                if (!subDirectory.isDirectory()) {
                    subDirectory.mkdirs();
                }
                String result = encounter.getCaseNumber() + "_" + exportTemplate.getOriginalFilename() + "_"
                        + FILENAMEDATEFORMAT.format(new Date()) + ".xml";
                // Write to disk
                File exportFile = new File(subDirectory, result);
                FhirR5Helper.writeResourceToFile(questionnaireResponse, exportFile);
            } catch (Exception e) {
                LOGGER.error("Could not write to disk. {}", e.getMessage());
                exportStatus = ExportStatus.FAILURE;
            }
        }

        try {
            doHandleHL7Export(
                    exportViaHL7,
                    hl7Hostname,
                    hl7Port,
                    sendingFacility,
                    receivingApplication,
                    receivingFacility,
                    obrFillerOrderNumber);
        } catch (Exception e) {
            LOGGER.error("Could not send via HL7. {}", e.getMessage());
            exportStatus = ExportStatus.FAILURE;
        }

        if (exportViaREST != null && exportViaREST && exportUrl != null && !exportUrl.isEmpty()) {
            exportStatus = exportViaREST(exportUrl);
        }

        return exportStatus;
    }

    /**
     * Handles the HL7 export process by generating and transmitting an HL7 message.
     *
     * @param exportViaHL7 Indicates whether to proceed with HL7 export. If set to {@code true},
     *                     the method generates and sends the HL7 message.
     * @param hl7Hostname The hostname of the HL7 server to which the message should be sent.
     * @param hl7Port The port of the HL7 server to which the message should be sent.
     * @param sendingFacility The facility identifier that is sending the HL7 message.
     * @param receivingApplication The application designated to receive the HL7 message.
     * @param receivingFacility The facility designated to receive the HL7 message.
     * @param obrFillerOrderNumber The filler order number to be included in the HL7 message within the OBR segment.
     * @throws Exception If any error occurs during the message generation or transmission process.
     */
    private void doHandleHL7Export(
            Boolean exportViaHL7,
            String hl7Hostname,
            Integer hl7Port,
            String sendingFacility,
            String receivingApplication,
            String receivingFacility,
            String obrFillerOrderNumber)
            throws Exception {

        if (exportViaHL7 != null
                && exportViaHL7
                && hl7Hostname != null
                && !hl7Hostname.isEmpty()
                && hl7Port != null
                && sendingFacility != null
                && receivingApplication != null
                && receivingFacility != null
                && obrFillerOrderNumber != null) {
            HL7MessageHelper hl7MessageHelper = new HL7MessageHelper();

            String fhirString = FhirR5Helper.decodeResourceToString(questionnaireResponse, false);

            ORU_R01 hl7Message = hl7MessageHelper.createMessageWithBlob(
                    exportTemplate,
                    encounter,
                    sendingFacility,
                    receivingApplication,
                    receivingFacility,
                    obrFillerOrderNumber,
                    fhirString);

            Questionnaire containedQuestionnaire = questionnaireResponse.getContained().stream()
                    .filter(Questionnaire.class::isInstance)
                    .map(Questionnaire.class::cast)
                    .findFirst()
                    .orElse(null);

            if (containedQuestionnaire != null) {
                hl7Message = hl7MessageHelper.overwriteMsh3NamespaceId(hl7Message, containedQuestionnaire.getName());
            }

            hl7MessageHelper.sendMessageViaComServer(hl7Hostname, hl7Port, hl7Message);
        }
    }

    /**
     * Exports a {@link QuestionnaireResponse questionnaireResponse} to a fhir server via REST
     * interface.
     *
     * @param serverBase The base adress of the server the response has to be send to.
     * @return {@link ExportStatus#FAILURE} if the {@link OperationOutcome} has failure or fatal as
     * issue severity, returns {@link ExportStatus#CONFLICT} if the OperationOutcome has warning as
     * issue severity otherwise returns success.
     */
    public ExportStatus exportViaREST(final String serverBase) {
        ExportStatus status = ExportStatus.SUCCESS;
        IGenericClient client = FhirR5Helper.getContext().newRestfulGenericClient(serverBase);
        OperationOutcome outcome = (OperationOutcome)
                client.create().resource(questionnaireResponse).execute().getOperationOutcome();

        for (OperationOutcomeIssueComponent issue : outcome.getIssue()) {
            switch (issue.getSeverity()) {
                case ERROR:
                    return ExportStatus.FAILURE;
                case FATAL:
                    return ExportStatus.FAILURE;
                case WARNING:
                    status = ExportStatus.CONFLICT;
                    break;
                default:
                    break;
            }
        }
        return status;
    }
}
