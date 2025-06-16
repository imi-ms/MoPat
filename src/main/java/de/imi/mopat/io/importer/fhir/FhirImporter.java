package de.imi.mopat.io.importer.fhir;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.controller.Constants;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private StringUtilities stringUtilityHelper;
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
    
    /**
     * Helper function to determine the FHIR version for an uploaded file. Trys to validate the uploaded file against
     * the schemas one by one and returns the first valid one or null
     *
     * @param file       to check fhir version for
     * @param webappPath to check for the validation schemas
     * @return FhirVersion that matches or null
     */
    private FhirVersion getFhirVersionForInput(MultipartFile file, String webappPath) {
        try {
            FhirImporterVersionAdapter adapter = new FhirImporterDstu3Adapter();
            ImportQuestionnaireValidation validation = new ImportQuestionnaireValidation();
            String schemaValidationPath = webappPath + Constants.FHIR_VALIDATION_SCHEMA_SUB_DIRECTORY_DSTU3;
            
            if (adapter.validateFileAgainstSchema(file, schemaValidationPath, validation, messageSource)) {
                return FhirVersion.DSTU3;
            }
            
            adapter = new FhirImporterR4bAdapter();
            validation = new ImportQuestionnaireValidation();
            schemaValidationPath = webappPath + Constants.FHIR_VALIDATION_SCHEMA_SUB_DIRECTORY_R4B;
            
            if (adapter.validateFileAgainstSchema(file, schemaValidationPath, validation, messageSource)) {
                return FhirVersion.R4B;
            }
            
            return null;
        } catch (Exception e) {
            LOGGER.error("Could not determine FHIR version for file", e);
            return null;
        }
        
    }
    
    /**
     * Function to handle the file upload of a FHIR questionnaire document. Determines the FHIR version automatically
     * and then performs validation, handles the file creation on the server and creates the necessary MoPat entities.
     *
     * @param file       FHIR questionnaire to upload
     * @param url        for the FHIR Server
     * @param webappPath Path to the webapps directory
     * @return ImporQuestionnaireValidation with the results of the import
     * @throws IOException
     */
    public ImportQuestionnaireValidation importFhirQuestionnaire(MultipartFile file, final String url,
        String webappPath) throws IOException {
        Questionnaire questionnaire;
        
        ImportQuestionnaireValidation result = new ImportQuestionnaireValidation();
        FhirVersion fhirVersion = getFhirVersionForInput(file, webappPath);
        
        if (fhirVersion == null) {
            result.reject("import.fhir.validate.invalidFile");
            return result;
        }
        
        FhirImporterVersionAdapter adapter = getAdapterForVersion(fhirVersion);
        
        if (adapter != null) {
            
            // Create list of uploadFiles to collect ExportTemplate files for
            // each configured export configuration group.
            List<File> uploadFiles = new ArrayList<>();
            // In case of import fails, collect all ExportTemplate files that
            // has been created to delete those ones.
            List<File> deletableFiles = new ArrayList<>();
            List<ExportTemplate> exportTemplates = new ArrayList<>();
            adapter.setParserValidator(new LenientErrorHandler());
            try {
                String objectStoragePath = configurationDao.getObjectStoragePath();
                // Save uploaded file and update xml filename in template
                String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
                String filename = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss " + "dd" + ".MM" + ".yyyy");
                
                adapter.createFhirQuestionnaire();
                
                if (file != null && !file.isEmpty() && file.getSize() > 0) {
                    filename = stringUtilityHelper.replaceGermanUmlauts(file.getOriginalFilename());
                    String validationSchemaFilePath = webappPath + adapter.getValidationPath();
                    // Validate the questionnaire against a xml schema
                    // definition to check if it's conform with fhir
                    // specification
                    
                    adapter.validateFileAgainstSchema(file, validationSchemaFilePath, result, messageSource);
                    
                    if (result.hasErrors()) {
                        return result;
                        //throw new ImportFailedException("Could not import Questionnaire");
                        //return getImportUpload(model);
                    }
                    exportTemplates = ExportTemplate.createExportTemplates("Automatically Generated Exporttemplate",
                        adapter.getExportTemplateType(), file, configurationGroupDao, exportTemplateDao);
                    adapter.setFhirQuestionnaire(adapter.parseResourceFromFile(file.getInputStream()));
                    
                } else if (url != null && !url.trim().isEmpty()) {
                    
                    // Check if the url contains "/" otherwise it cannot be
                    // resolved
                    if (!url.contains("/")) {
                        result.reject("import.error.invalidUrl", new Object[]{}, "Input url is not valid");
                        return result;
                        //throw new ImportFailedException("Could not import Questionnaire");
                        //return getImportUpload(model);
                    }
                    
                    // Get the serverBase adress to create connection to the
                    // server
                    String serverBase = url.substring(0, url.substring(0, url.lastIndexOf("/")).lastIndexOf("/"));
                    IGenericClient client = adapter.getContext().newRestfulGenericClient(serverBase);
                    adapter.setFhirQuestionnaire(
                        client.read().resource(org.hl7.fhir.dstu3.model.Questionnaire.class).withUrl(url).execute());
                    adapter.setFhirQuestionnaireUrl(url.substring(url.indexOf("Questionnaire")));
                    
                    if (adapter.isFhirQuestionnaireTitleEmpty()) {
                        filename = "Questionnaire default title.xml";
                        adapter.setFhirQuestionnaireTitle(
                            "Questionnaire default " + "title " + dateFormat.format(new Date()));
                    } else {
                        filename = adapter.getFhirQuestionnaireTitle() + ".xml";
                    }
                    exportTemplates = ExportTemplate.createExportTemplates("Automatically Generated Exporttemplate",
                        adapter.getExportTemplateType(), null, configurationGroupDao, exportTemplateDao);
                }
                
                // The export templates are created for each configuration
                // group existing for FHIR
                // For each group there will be created a upload file that
                // will be exported on the basis of the configurations
                for (ExportTemplate exportTemplate : exportTemplates) {
                    String uploadFilename = exportTemplate.getId() + "_" + filename;
                    File uploadDir = new File(contextPath);
                    if (!uploadDir.isDirectory()) {
                        uploadDir.mkdirs();
                    }
                    File uploadFile = new File(contextPath, uploadFilename);
                    uploadFiles.add(uploadFile);
                    deletableFiles.add(uploadFile);
                    exportTemplate.setFilename(uploadFilename);
                }
                
                // Convert fhir questionnaire to the mopat questionnaire
                ImportQuestionnaireResult fhirQuestionnaireResult = adapter.convertFHIRQuestionnaireToMoPatQuestionnaire(
                    exportTemplates, messageSource);
                questionnaire = fhirQuestionnaireResult.getQuestionnaire();
                
                // Write the questionnaire in each upload file
                for (File uploadFile : uploadFiles) {
                    uploadFile.createNewFile();
                    adapter.writeQuestionnaireToFile(uploadFile);
                }
                
                // Just append the current date if the questionnaire's name
                // is already in use
                if (!questionnaireDao.isQuestionnaireNameUnique(questionnaire.getName(), null)) {
                    questionnaire.setName(questionnaire.getName() + " " + dateFormat.format(new Date()));
                }
                
                // Merge questionnaire
                questionnaireDao.merge(questionnaire);
                
                QuestionnaireVersionGroup questionnaireVersionGroup = questionnaireVersionGroupService.createQuestionnaireGroup(
                    questionnaire.getName());
                questionnaire.setQuestionnaireVersionGroup(questionnaireVersionGroup);
                questionnaireVersionGroup.addQuestionnaire(questionnaire);
                questionnaireVersionGroupService.add(questionnaireVersionGroup);
                
                // Merge the export templates
                for (ExportTemplate exportTemplate : exportTemplates) {
                    exportTemplate.setQuestionnaire(questionnaire);
                    questionnaire.addExportTemplate(exportTemplate);
                    exportTemplateDao.merge(exportTemplate);
                }
                
                // Merge questionnaire second time
                questionnaireDao.merge(questionnaire);
                
                result.setImportResult(fhirQuestionnaireResult);
            } catch (IOException | IllegalStateException | ConfigurationException | ResourceNotFoundException |
                     FhirClientConnectionException e) {
                for (ExportTemplate template : exportTemplates) {
                    exportTemplateDao.remove(template);
                }
                
                for (File fileToDelete : deletableFiles) {
                    fileToDelete.delete();
                }
                
                result.reject("import.fhir.error.message", new Object[]{e.getLocalizedMessage()},
                    "The following error occurred: " + e.getMessage());
                throw new ImportFailedException("Could not import Questionnaire");
                
                //return getImportUpload(model);
            }
            return result;
        } else {
            throw new ImportFailedException("Could not load importer for version " + fhirVersion.name());
        }
    }
    
    public void uploadFhirExportTemplate(MultipartFile file, File uploadFile, ExportTemplateType exportTemplateType)
        throws IOException {
        //Parse the upload file to get the fhir resource type
        FhirVersion fhirVersion = FhirVersion.getVersionForExportTemplateType(exportTemplateType);
        if (fhirVersion != null) {
            FhirImporterVersionAdapter adapter = getAdapterForVersion(fhirVersion);
            
            if (adapter != null) {
                adapter.setFhirQuestionnaire(adapter.parseResourceFromFile(file.getInputStream()));
                adapter.writeQuestionnaireToFile(uploadFile);
            }
        }
    }
    
    /**
     * Function that checks if the given file is valid for the given FhirVersion by checking it against the schema
     * validation for it.
     *
     * @param file        to validate against the schemas
     * @param webappPath  The root directory of the webapp
     * @param fhirVersion the FhirVersion to check against
     * @return true if there is a version without any errors
     */
    public boolean validateFhirFileAgainstFhirVersion(MultipartFile file, String webappPath,
        ImportQuestionnaireValidation validationResult, FhirVersion fhirVersion) {
        FhirImporterVersionAdapter adapter = getAdapterForVersion(fhirVersion);
        
        if (adapter != null) {
            String validationSchemaFilePath = webappPath + adapter.getValidationPath();
            return adapter.validateFileAgainstSchema(file, validationSchemaFilePath, validationResult, messageSource);
        } else {
            return false;
        }
    }
    
    /**
     * Overloaded function to automatically handle the validation against a given ExportTemplateType. Works only for
     * FHIR ExportTemplateTypes.
     *
     * @param file               to validate against the schemas
     * @param webappPath         The root directory of the webapp
     * @param exportTemplateType ExportTemplateType to determine the FHIR Version for
     * @return true if there is a version without any errors
     */
    public boolean validateFhirFileAgainstFhirVersion(MultipartFile file, String webappPath,
        ImportQuestionnaireValidation validationResult, ExportTemplateType exportTemplateType) {
        FhirVersion fhirVersion = FhirVersion.getVersionForExportTemplateType(exportTemplateType);
        if (fhirVersion != null) {
            return this.validateFhirFileAgainstFhirVersion(file, webappPath, validationResult, fhirVersion);
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
                return null;
                //Not implemented yet
            }
            
            //Set R4B as the default Version
            default -> {
                return new FhirImporterR4bAdapter();
            }
        }
    }
}
