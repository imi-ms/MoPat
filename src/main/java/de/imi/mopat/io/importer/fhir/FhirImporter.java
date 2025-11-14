package de.imi.mopat.io.importer.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.FhirVersionHelper;
import de.imi.mopat.helper.controller.QuestionnaireVersionGroupService;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.io.importer.ImportFailedException;
import de.imi.mopat.io.importer.ImportQuestionnaireResult;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.enumeration.FhirVersion;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


/**
 * Service to handle the upload of FHIR Questionnaires
 */
@Service
public class FhirImporter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FhirImporter.class);


    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ConfigurationGroupDao configurationGroupDao;
    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;
    @Autowired
    FhirVersionHelper fhirVersionHelper;

    /**
     * Function to handle the file upload of a FHIR questionnaire document. Determines the FHIR
     * version automatically, performs validation, handles the file creation on the server, and
     * creates the necessary MoPat entities.
     *
     * @param file           FHIR questionnaire to upload
     * @param url            for the FHIR Server
     * @param frontendLocale locale from the frontend
     * @return ImportQuestionnaireValidation with the results of the import
     * @throws IOException
     */
    public ImportQuestionnaireValidation importFhirQuestionnaire(MultipartFile file,
        final String url, FhirVersion fhirVersion, String frontendLocale) throws IOException {

        ImportQuestionnaireValidation result = new ImportQuestionnaireValidation();

        if (fhirVersion == null) {
            result.reject("import.fhir.validate.invalidFile");
            return result;
        }

        FhirImporterVersionAdapter adapter = getAdapterForVersion(fhirVersion);
        if (adapter == null) {
            throw new ImportFailedException(
                "Could not load importer for version " + fhirVersion.name());
        }

        try {
            processFileOrUrl(adapter, file, url, result, frontendLocale);
            performImport(file, adapter, result);
        } catch (Exception e) {
            handleImportErrors(result, e);
        }

        return result;
    }

    /**
     * Processes the file or URL, validates, and initializes the adapter with the FHIR
     * questionnaire.
     */
    private void processFileOrUrl(FhirImporterVersionAdapter adapter, MultipartFile file,
        String url, ImportQuestionnaireValidation result, String frontendLocale)
        throws IOException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

        if (file != null && !file.isEmpty() && file.getSize() > 0) {
            adapter.validateFileWithFhirInstanceValidator(
                new String(file.getBytes(), StandardCharsets.UTF_8), result, frontendLocale);

            if (result.hasErrors()) {
                throw new ImportFailedException("File validation failed");
            }

            adapter.setFhirQuestionnaire(adapter.parseResourceFromFile(file.getInputStream()));
        } else if (url != null && !url.trim().isEmpty()) {
            validateAndProcessUrl(adapter, url, result, dateFormat);
        }
    }

    /**
     * Validates and processes the FHIR questionnaire from a URL.
     */
    private void validateAndProcessUrl(FhirImporterVersionAdapter adapter, String url,
        ImportQuestionnaireValidation result, SimpleDateFormat dateFormat) {

        if (!url.contains("/")) {
            result.reject("import.error.invalidUrl", new Object[]{}, "Input URL is not valid");
            throw new ImportFailedException("Invalid URL");
        }

        String serverBase = url.substring(0,
            url.substring(0, url.lastIndexOf("/")).lastIndexOf("/"));
        IGenericClient client = adapter.getContext().newRestfulGenericClient(serverBase);
        adapter.setFhirQuestionnaire(
            client.read().resource(org.hl7.fhir.dstu3.model.Questionnaire.class).withUrl(url)
                .execute());
        adapter.setFhirQuestionnaireTitle(
            adapter.isFhirQuestionnaireTitleEmpty() ? "Default Title " + dateFormat.format(
                new Date()) : adapter.getFhirQuestionnaireTitle());
    }


    /**
     * Merges questionnaire and export templates into the database.
     */
    private void performImport(MultipartFile file, FhirImporterVersionAdapter adapter,
        ImportQuestionnaireValidation importValidation) {

        List<ExportTemplate> templates;
        try {
            templates = transformQuestionnaireAndExportTemplates(file, adapter,
                importValidation);
        } catch (IOException e) {
            LOGGER.error("Could not create export templates", e);
            throw new ImportFailedException("Error during FHIR questionnaire import");
        }

        ImportQuestionnaireResult importResult = importValidation.getImportResult();
        Questionnaire questionnaire = importResult.getQuestionnaire();

        if (!questionnaireDao.isQuestionnaireNameUnique(questionnaire.getName(), null)) {
            String currentTimestamp = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(
                Date.from(Instant.now()));

            questionnaire.setName(
                String.format("%s %s", questionnaire.getName(), currentTimestamp));
        }

        questionnaireDao.merge(questionnaire);

        QuestionnaireVersionGroup group = questionnaireVersionGroupService.createQuestionnaireGroup(
            questionnaire.getName());
        questionnaire.setQuestionnaireVersionGroup(group);
        group.addQuestionnaire(questionnaire);
        questionnaireVersionGroupService.add(group);

        for (ExportTemplate exportTemplate : templates) {
            exportTemplate.setQuestionnaire(questionnaire);
            questionnaire.addExportTemplate(exportTemplate);
            exportTemplateDao.merge(exportTemplate);
        }

        questionnaireDao.merge(questionnaire);
    }

    /**
     * Handles creation of export templates and writing files to disk.
     */
    private List<ExportTemplate> transformQuestionnaireAndExportTemplates(MultipartFile file,
        FhirImporterVersionAdapter adapter, ImportQuestionnaireValidation result)
        throws IOException {

        List<ExportTemplate> templates = ExportTemplate.createExportTemplates(
            "Automatically Generated Exporttemplate", adapter.getExportTemplateType(), file,
            configurationGroupDao, exportTemplateDao);

        for (ExportTemplate template : templates) {
            File templateFile = createTemplateFile(template);
            adapter.writeQuestionnaireToFile(templateFile);
        }

        ImportQuestionnaireResult fhirResult = adapter.convertFHIRQuestionnaireToMoPatQuestionnaire(
            templates, messageSource);

        result.setImportResult(fhirResult);

        return templates;
    }

    /**
     * Handles exceptions during FHIR questionnaire import, rolling back changes.
     */
    private void handleImportErrors(ImportQuestionnaireValidation result, Exception e) {
        LOGGER.error("Error during FHIR questionnaire import", e);
        result.reject("import.fhir.error.message", new Object[]{e.getMessage()},
            "An error occurred: " + e.getMessage());
    }

    /**
     * Creates a file for the given ExportTemplate.
     */
    private File createTemplateFile(ExportTemplate template) throws IOException {
        String path =
            configurationDao.getObjectStoragePath() + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, template.getId() + "_" + template.getFilename() + ".xml");
        file.createNewFile();
        template.setFilename(file.getName());
        return file;
    }


    public void uploadFhirExportTemplate(MultipartFile file, File uploadFile,
        ExportTemplateType exportTemplateType) throws IOException {
        //Parse the upload file to get the fhir resource type
        FhirVersion fhirVersion =
            fhirVersionHelper.mapExportTemplateTypeToFhirVersion(exportTemplateType);
        if (fhirVersion != null) {
            FhirImporterVersionAdapter adapter = getAdapterForVersion(fhirVersion);

            if (adapter != null) {
                adapter.setFhirQuestionnaire(adapter.parseResourceFromFile(file.getInputStream()));
                adapter.writeQuestionnaireToFile(uploadFile);
            }
        }
    }

    /**
     * Function that checks if the given file is valid for the given FhirVersion by checking it
     * against the schema validation for it.
     *
     * @param file        to validate against the schemas
     * @param fhirVersion the FhirVersion to check against
     * @return true if there is a version without any errors
     */
    public boolean validateFhirFileAgainstFhirVersion(MultipartFile file,
        ImportQuestionnaireValidation validationResult, FhirVersion fhirVersion,
        String frontendLocale) {
        FhirImporterVersionAdapter adapter = getAdapterForVersion(fhirVersion);

        if (adapter != null) {
            try {
                return adapter.validateFileWithFhirInstanceValidator(
                    new String(file.getBytes(), StandardCharsets.UTF_8), validationResult,
                    frontendLocale);
            } catch (IOException e) {
                LOGGER.error("Error during FHIR questionnaire import", e);
                throw new ImportFailedException("Error during FHIR questionnaire import");
            }
        } else {
            return false;
        }
    }

    /**
     * Overloaded function to automatically handle the validation against a given
     * ExportTemplateType. Works only for FHIR ExportTemplateTypes.
     *
     * @param file               to validate against the schemas
     * @param exportTemplateType ExportTemplateType to determine the FHIR Version for
     * @return true if there is a version without any errors
     */
    public boolean validateFhirFileAgainstFhirVersion(MultipartFile file,
        ImportQuestionnaireValidation validationResult, ExportTemplateType exportTemplateType,
        String frontendLocale) {
        FhirVersion fhirVersion =
            fhirVersionHelper.mapExportTemplateTypeToFhirVersion(exportTemplateType);
        if (fhirVersion != null) {
            return this.validateFhirFileAgainstFhirVersion(file, validationResult, fhirVersion,
                frontendLocale);
        } else {
            return false;
        }
    }

    private FhirImporterVersionAdapter getAdapterForVersion(FhirVersion fhirVersion) {
        switch (fhirVersion) {
            case DSTU3 -> {
                return new FhirImporterDstu3Adapter();
            }

            case R5 -> {
                return new FhirImporterR5Adapter();
                //Not implemented yet
            }

            //Set R4B as the default Version
            default -> {
                return new FhirImporterR4bAdapter();
            }
        }
    }
}
