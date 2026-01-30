package de.imi.mopat.io.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.io.importer.fhir.FhirR4bHelper;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;
import org.hl7.fhir.r4b.model.*;
import org.hl7.fhir.r4b.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4b.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class EncounterExporterTemplateFhirR4b implements EncounterExporterTemplate {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExporterTemplateFhirR4b.class);
    private static final SimpleDateFormat FILENAMEDATEFORMAT = new SimpleDateFormat(
        "dd.MM.yyyy_HH.mm.ss");

    private final ConfigurationDao configurationDao;

    private Encounter encounter;
    private ExportTemplate exportTemplate;
    private QuestionnaireResponse questionnaireResponse;

    public EncounterExporterTemplateFhirR4b(final ConfigurationDao configurationDao) {
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
        questionnaireResponse = FhirR4bHelper.getQuestionnaireResponse(
            (Questionnaire) FhirR4bHelper.parseResourceFromFile(new FileInputStream(file)));
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
        for (QuestionnaireResponseItemAnswerComponent answer : FhirR4bHelper.getAllAnswersOfQuestionnaireResponse(
            questionnaireResponse)) {
            if (value != null && !value.isEmpty()) {
                if (answer.getId().equalsIgnoreCase(splitExportField[0])) {
                    if (splitExportField.length > 1 && splitExportField[1].equals("true")
                        && value.equals("TRUE")) {
                        answer.setValue(new BooleanType(Boolean.TRUE));
                        LOGGER.info(
                            "ExportField found. Value of answer '" + answer.getId() + "' set to '"
                                + value + "'.");
                    } else if (splitExportField.length > 1 && splitExportField[1].equals("false")
                        && value.equals("TRUE")) {
                        answer.setValue(new BooleanType(Boolean.FALSE));
                        LOGGER.info(
                            "ExportField found. Value of answer '" + answer.getId() + "' set to '"
                                + value + "'.");
                    } else if (splitExportField.length > 1 && splitExportField[1].equals(
                        "freetext")) {
                        answer.setValue(new StringType(value));
                        LOGGER.info(
                            "ExportField found. Value of answer '" + answer.getId() + "' set to '"
                                + value + "'.");
                    } else if (answer.getValue() instanceof Coding) {
                        answer.setValue(new Coding().setDisplay(value));
                        LOGGER.info(
                            "ExportField found. Value of answer '" + answer.getId() + "' set to '"
                                + value + "'.");
                    } else if (answer.getValue() instanceof DateType) {
                        try {
                            answer.setValue(
                                new DateType(new SimpleDateFormat("yyyy-MM-dd").parse(value)));
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId()
                                + "' set" + " to '" + new SimpleDateFormat("yyyy-MM-dd").parse(
                                value) + "'.");
                        } catch (ParseException e) {
                            LOGGER.info(
                                "ExportField could not be written. Value " + "is invalid. {}",
                                e.getMessage());
                            answer.setValue(null);
                        }
                    } else if (answer.getValue() instanceof DecimalType) {
                        answer.setValue(new DecimalType(Double.parseDouble(value)));
                        LOGGER.info(
                            "ExportField found. Value of answer '" + answer.getId() + "' set to '"
                                + value + "'.");
                    } else if (answer.getValue() instanceof IntegerType) {
                        Double doubleValue = null;
                        try {
                            doubleValue = Double.parseDouble(value);
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId()
                                + "' set" + " to '" + value + "'.");
                        } catch (NumberFormatException e) {
                            LOGGER.info(
                                "ExportField could not be written. Value " + "is invalid. {}",
                                e.getMessage());
                            answer.setValue(null);
                        }
                        if (doubleValue != null) {
                            answer.setValue(new IntegerType(doubleValue.intValue()));
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId()
                                + "' set" + " to '" + value + "'.");
                        } else {
                            try {
                                answer.setValue(new IntegerType(Integer.parseInt(value)));
                                LOGGER.info(
                                    "ExportField found. Value of " + "answer '" + answer.getId()
                                        + "' set to '" + value + "'.");
                            } catch (NumberFormatException e) {
                                LOGGER.info(
                                    "ExportField could not be written. " + "Value is invalid. {}",
                                    e.getMessage());
                                answer.setValue(null);
                                break;
                            }
                        }
                    } else if (answer.getValue() instanceof StringType) {
                        try {
                            answer.setValue(new StringType(value));
                            LOGGER.info("ExportField found. Value of answer " + "'" + answer.getId()
                                + "' set" + " to '" + value + "'.");
                        } catch (Exception e) {
                            LOGGER.info(
                                "ExportField could not be written. Value " + "is invalid. {}",
                                e.getMessage());
                            answer.setValue(null);
                        }
                    }
                    // For multiple choice questions the exportFields also
                    // save the items linkId.
                    // Thus, the second item of splitExportFields contains
                    // the answer id.
                } else if (splitExportField.length > 1 && splitExportField[1].equals(answer.getId())
                    && value.equals("TRUE")) {
                    answer.setValue(new BooleanType(Boolean.parseBoolean(value)));
                    LOGGER.info(
                        "ExportField found. Value of answer '" + answer.getId() + "' set to '"
                            + value + "'.");
                }
            } else {
                answer.setValue(null);
                LOGGER.info("Value was null or empty. Value of export field " + "[" + exportField
                    + "] was set to null.");
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
                default:
                    break;
            }
        }

        questionnaireResponse.setStatus(
            QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);

        if (exportToDirectory) {
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
            FhirR4bHelper.writeResourceToFile(questionnaireResponse, exportFile);
        }

        if (exportViaREST && exportUrl != null && !exportUrl.isEmpty()) {
            return exportViaREST(exportUrl);
        }
        return ExportStatus.SUCCESS;
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
        IGenericClient client = FhirR4bHelper.getContext().newRestfulGenericClient(serverBase);
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
