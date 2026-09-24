package de.imi.mopat.io.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.io.importer.fhir.FhirDstu3Helper;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemOptionComponent;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;

/**
 *
 */
public class EncounterExporterTemplateFhirDstu3 implements EncounterExporterTemplate {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExporterTemplateHL7v2.class);
    private static final SimpleDateFormat FILENAMEDATEFORMAT = new SimpleDateFormat(
        "dd.MM.yyyy_HH.mm.ss");

    private final ConfigurationDao configurationDao;

    private Encounter encounter;
    private ExportTemplate exportTemplate;
    private QuestionnaireResponse questionnaireResponse;
    private Questionnaire questionnaire;


    public EncounterExporterTemplateFhirDstu3(final ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    @Override
    public void load(final Encounter encounter, final ExportTemplate exportTemplate)
        throws Exception {

        this.encounter = encounter;
        this.exportTemplate = exportTemplate;

        String objectStoragePath = configurationDao.getObjectStoragePath();
        if (objectStoragePath == null) {
            LOGGER.error("[SETUP] No object storage path found. Please provide a "
                    + "value for {} in the {} file", Constants.OBJECT_STORAGE_PATH_PROPERTY,
                Constants.CONFIGURATION);
        } else {
            LOGGER.info("[SETUP] Object storage path configuration found.");
        }
        LOGGER.info("[SETUP] Accessing properties file to look up the export " + "path"
            + " in  {}...[DONE]", Constants.CONFIGURATION);

        String templatePath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
        String filename = exportTemplate.getFilename();
        File file = new File(templatePath, filename);

        // Create questionnaireResponse and set patientID and caseNumber
        questionnaire = (Questionnaire) FhirDstu3Helper.parseResourceFromFile(
            new FileInputStream(file));
        questionnaireResponse = FhirDstu3Helper.getQuestionnaireResponse(questionnaire);
        Patient patient = new Patient();
        patient.addIdentifier(new Identifier().setValue(encounter.getCaseNumber()));
        questionnaireResponse.addContained(patient);
        questionnaireResponse.setSource(new Reference(patient));
    }

    @Override
    public void write(final String exportField, final String value) throws Exception {
        String exportClean = exportField.replace("u002E", ".");
        String[] splitExportField = exportClean.split("_");
        for (int i = 0; i < splitExportField.length; i++) {
            splitExportField[i] = splitExportField[i].replace("u005F", "_");
        }

        List<QuestionnaireResponseItemAnswerComponent> allAnswers = FhirDstu3Helper
            .getAllAnswersOfQuestionnaireResponse(questionnaireResponse);

        // Handle null/empty value: Find the specific answer and clear it, then stop.
        if (value == null || value.isEmpty()) {
            for (QuestionnaireResponseItemAnswerComponent answer : allAnswers) {
                if (isMatchingAnswer(answer, splitExportField)) {
                    answer.setValue(null);
                    LOGGER.info(
                        "ExportField '{}' found. Value of answer '{}' set to null.",
                        exportField,
                        answer.getId()
                    );
                    return; // CRITICAL: Exit immediately to avoid clearing other answers
                }
            }
            // If no matching answer was found for a null value, log it and exit
            LOGGER.warn("No matching answer found for null/empty value of exportField: {}",
                exportField);
            return;
        }

        // Handle non-empty value: Find the matching answer and set the value.
        for (QuestionnaireResponseItemAnswerComponent answer : allAnswers) {
            if (!isMatchingAnswer(answer, splitExportField)) {
                continue;
            }

            try {
                setValueBasedOnType(answer, value, splitExportField);
                LOGGER.info(
                    "ExportField '{}' found. Value of answer '{}' set to '{}'.",
                    exportField,
                    answer.getId(),
                    value
                );
                return; // Stop after setting the first match
            } catch (Exception e) {
                LOGGER.error(
                    "Failed to write value '{}' to answer '{}': {}",
                    value,
                    answer.getId(),
                    e.getMessage(),
                    e
                );
            }
        }
    }

    @Override
    public void clean() {
        removeEmptyAnswersFromQuestionnaireResponse();
    }

    /**
     * Removes all answers with empty or null values from the questionnaire response items. This
     * method iterates through each item in the questionnaire response and filters out any answer
     * components where the value is null or an empty string.
     */
    private void removeEmptyAnswersFromQuestionnaireResponse() {
        questionnaireResponse.getItem().forEach(qr ->
            qr.setAnswer(qr.getAnswer().stream().filter(answer ->
                answer.getValue() != null && !answer.getValue().isEmpty()
            ).toList())
        );
    }

    /**
     * Determines if a given answer component matches the specified field identifier.
     *
     * @param answer     The answer component to check against.
     * @param splitField The array of field parts extracted from the export field definition.
     * @return true if the answer ID matches the field identifier directly or as a multiple choice
     * option, false otherwise.
     */
    private boolean isMatchingAnswer(QuestionnaireResponseItemAnswerComponent answer,
        String[] splitField) {
        if (splitField.length == 0) {
            return false;
        }

        // Case 1: Direct match (e.g., "item.linkId" or "item.linkId_true")
        if (answer.getId().equalsIgnoreCase(splitField[0])) {
            return true;
        }

        // Case 2: Multiple choice option match (e.g., "item.linkId_optionId")
        return splitField.length > 1 && splitField[1].equals(answer.getId());
    }

    /**
     * Sets the value of the given answer component based on the existing FHIR primitive type and
     * special field logic. Handles boolean logic embedded in the field name as well as standard
     * type-based conversions for Coding, Date, Decimal, Integer, and String types.
     *
     * @param answer     The questionnaire response item answer component to which the value should
     *                   be assigned.
     * @param value      The string representation of the value to be parsed and assigned to the
     *                   answer.
     * @param splitField An array containing the split parts of the export field definition, used
     *                   for identifying special cases such as boolean or freetext handling.
     * @throws Exception If an error occurs during date parsing or type conversion.
     */
    private void setValueBasedOnType(
        QuestionnaireResponseItemAnswerComponent answer,
        String value,
        String[] splitField
    ) throws Exception {

        // 1. Handle Explicit Boolean Flags (e.g., "item_true", "item_false")
        // Only apply if the value is strictly "TRUE" or "FALSE"
        if (splitField.length > 1) {
            String suffix = splitField[1];

            // Case: Explicit boolean toggle
            if ((suffix.equals("true") || suffix.equals("false")) &&
                (value.equals("TRUE") || value.equals("FALSE"))) {
                // if suffix "false" && value "TRUE" -> set FALSE.
                if (suffix.equals("false") && value.equals("TRUE")) {
                    answer.setValue(new BooleanType(false));
                } else if (suffix.equals("true") && value.equals("TRUE")) {
                    answer.setValue(new BooleanType(true));
                } else {
                    answer.setValue(new BooleanType(Boolean.parseBoolean(value)));
                }
                return;
            }

            // Case: Free Text
            if (suffix.equals("ft")) {
                answer.setValue(new StringType(value));
                return;
            }

            // Case: Multiple Choice / Coding Lookup
            // If splitField[1] is an option ID, we need to find the Coding in the Questionnaire
            Coding coding = findCodingFromQuestionnaire(splitField[0], splitField[1]);
            if (coding != null && value.equals("TRUE")) {
                // Set the Coding value.
                // Note: If the answer component currently holds a Coding, we replace it.
                // If it holds a Boolean, this will change the type, which is correct for MCQs.
                answer.setValue(coding);
            }
        }

        // 2. Standard Type-Based Conversion (Fallback for non-matched suffixes or single-part fields)
        else {
            if (answer.getValue() instanceof Coding) {
                answer.setValue(new Coding().setDisplay(value));
            } else if (answer.getValue() instanceof DateType) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date parsedDate = sdf.parse(value);
                answer.setValue(new DateType(parsedDate));
            } else if (answer.getValue() instanceof DecimalType) {
                answer.setValue(new DecimalType(Double.parseDouble(value)));
            } else if (answer.getValue() instanceof IntegerType) {
                // Try integer first, then double if it fails
                try {
                    answer.setValue(new IntegerType(Integer.parseInt(value)));
                } catch (NumberFormatException e) {
                    answer.setValue(new IntegerType((int) Double.parseDouble(value)));
                }
            } else if (answer.getValue() instanceof StringType) {
                answer.setValue(new StringType(value));
            } else {
                // Default fallback: If type is unknown or null, try String
                answer.setValue(new StringType(value));
            }
        }
    }

    /**
     * Traverses the Questionnaire to find the Coding for a specific answer option.
     *
     * @param itemId   The linkId of the Questionnaire Item (e.g., "question1")
     * @param optionId The linkId or code of the Answer Option (e.g., "option1" or "yes")
     * @return The Coding object, or null if not found.
     */
    private Coding findCodingFromQuestionnaire(String itemId, String optionId) {
        if (this.questionnaire == null) {
            return null;
        }

        // Traverse items to find the one with linkId == itemId
        for (QuestionnaireItemComponent item : questionnaire.getItem()) {
            if (item.getLinkId().equals(itemId)) {
                // Check if this item has answerOptions
                for (QuestionnaireItemOptionComponent option : item.getOption()) {
                    // The option might be identified by linkId or valueCoding
                    if (option.getValueCoding() != null) {
                        Coding coding = option.getValueCoding();
                        // Match by Code, Display, or System+Code
                        if (coding.getCode().equals(optionId) ||
                            (coding.getDisplay() != null && coding.getDisplay().equals(optionId))) {
                            return coding;
                        }
                    }
                }
            }
        }
        return null;
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
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
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

        questionnaireResponse.setStatus(
            QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);

        ExportStatus exportStatus = ExportStatus.SUCCESS;

        if (exportToDirectory != null && exportToDirectory && exportPath != null) {
            try {
                File path = new File(exportPath);
                if (!path.isDirectory()) {
                    path.mkdirs();
                }

                // Create a sub-directory for the exported files
                String filepath =
                    exportPath + File.separator + exportTemplate.getQuestionnaire().getName()
                        .replaceAll(":", "_") + "/" + exportTemplate.getName().replaceAll(":", "_")
                        + "/";
                File subDirectory = new File(filepath);
                if (!subDirectory.isDirectory()) {
                    subDirectory.mkdirs();
                }
                String result =
                    encounter.getCaseNumber() + "_" + exportTemplate.getOriginalFilename() + "_"
                        + FILENAMEDATEFORMAT.format(new Date()) + ".xml";
                // Write to disk
                File exportFile = new File(subDirectory, result);
                FhirDstu3Helper.writeResourceToFile(questionnaireResponse, exportFile);
            } catch (Exception e) {
                LOGGER.error("Could not write to disk. {}", e.getMessage());
                exportStatus = ExportStatus.FAILURE;
            }
        }

        try {
            doHandleHL7Export(
                exportViaHL7, hl7Hostname, hl7Port, sendingFacility,
                receivingApplication, receivingFacility, obrFillerOrderNumber
            );
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
        Boolean exportViaHL7, String hl7Hostname, Integer hl7Port,
        String sendingFacility, String receivingApplication, String receivingFacility,
        String obrFillerOrderNumber
    ) throws Exception {

        if (exportViaHL7 != null && exportViaHL7 && hl7Hostname != null && !hl7Hostname.isEmpty()
            && hl7Port != null && sendingFacility != null && receivingApplication != null
            && receivingFacility != null && obrFillerOrderNumber != null) {
            HL7MessageHelper hl7MessageHelper = new HL7MessageHelper();

            String fhirString = FhirDstu3Helper.decodeResourceToString(questionnaireResponse, false);

            ORU_R01 hl7Message = hl7MessageHelper.createMessageWithBlob(
                exportTemplate, encounter, sendingFacility, receivingApplication,
                receivingFacility, obrFillerOrderNumber, fhirString
            );

            Questionnaire containedQuestionnaire = questionnaireResponse.getContained().stream()
                .filter(Questionnaire.class::isInstance)
                .map(Questionnaire.class::cast)
                .findFirst()
                .orElse(null);

            if (containedQuestionnaire != null) {
                hl7Message = hl7MessageHelper.overwriteMsh3NamespaceId(hl7Message,
                    containedQuestionnaire.getName());
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
        IGenericClient client = FhirDstu3Helper.getContext().newRestfulGenericClient(serverBase);
        OperationOutcome outcome = (OperationOutcome) client.create()
            .resource(questionnaireResponse).execute().getOperationOutcome();

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
