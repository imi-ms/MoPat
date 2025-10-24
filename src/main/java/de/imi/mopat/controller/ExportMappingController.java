package de.imi.mopat.controller;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.parser.DataFormatException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.ExportRuleDao;
import de.imi.mopat.dao.ExportRuleFormatDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.FhirVersionHelper;
import de.imi.mopat.io.importer.ImportQuestionnaireError;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.io.importer.fhir.FhirDstu3Helper;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.io.ExportTemplateImporter;
import de.imi.mopat.io.importer.fhir.FhirImporter;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterExportTemplate;
import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportEncounterFieldType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;
import de.imi.mopat.model.ExportRule;
import de.imi.mopat.model.ExportRuleAnswer;
import de.imi.mopat.model.ExportRuleEncounter;
import de.imi.mopat.model.ExportRuleFormat;
import de.imi.mopat.model.ExportRuleQuestion;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.dto.ExportRuleDTO;
import de.imi.mopat.model.dto.ExportRuleFormatDTO;
import de.imi.mopat.model.dto.ExportRulesDTO;
import de.imi.mopat.model.enumeration.ExportScoreFieldType;
import de.imi.mopat.model.enumeration.FhirVersion;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.validator.ExportRulesDTOValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import jakarta.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

/**
 *
 */
@Controller
public class ExportMappingController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ExportMappingController.class);
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private ConfigurationGroupDao configurationGroupDao;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private ExportRuleDao exportRuleDao;
    @Autowired
    private ExportRuleFormatDao exportRuleFormatDao;
    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private ExportRulesDTOValidator exportRulesDTOValidator;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private StringUtilities stringUtilityHelper;
    @Autowired
    private FhirImporter fhirImporter;
    @Autowired
    private FhirVersionHelper fhirVersionHelper;

    /**
     * Sets autogrowth for sent list data to a new limit. This prevents index out of bounds
     * exceptions when auto filling ArrayList objects.
     *
     * @param binder WebDataBinder, which is a special DataBinder for data binding from web request
     *               parameters to JavaBean objects
     */
    @InitBinder
    public void initListBinder(final WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(1000);
    }

    /**
     * Returns all {@link ExportTemplate} objects based on the given id of an {@link Questionnaire}
     * object.
     *
     * @param id An Id of an {@link Questionnaire} object.
     * @return All {@link ExportTemplate} objects based on the given id of an {@link Questionnaire}
     * object.
     */
    public List<ExportTemplate> getAllMappings(final Long id) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);
        List<ExportTemplate> exportTemplates = new ArrayList<>(questionnaire.getExportTemplates());
        return exportTemplates;
    }

    /**
     * Returns all existing {@link ExportTemplateType}.
     *
     * @return All existing {@link ExportTemplateType}.
     */
    @ModelAttribute("exportTemplateTypeList")
    public ArrayList<ExportTemplateType> getExportTemplateTypeList() {
        return new ArrayList<>(Arrays.asList(ExportTemplateType.values()));
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/mapping/list</i>. Shows the list of export
     * templates of a specific questionnaire.
     *
     * @param id    An Id of an {@link Questionnaire} object.
     * @param model The model, which holds the information for the view.
     * @return The <i>mapping/list</i> website.
     */
    @GetMapping(value = "/mapping/list")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String showMapping(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);
        if (questionnaire == null) {
            //clear the models attributes that are set in the @ModelAttribute
            // methods
            model.addAttribute("exportTemplateTypeList", null);
            return "redirect:/questionnaire/list";
        }
        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("allMappings", this.getAllMappings(id));
        return "mapping/list";
    }

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/mapping/uploadtemplate</i>. Provides the ability to upload a new
     * export template file.
     *
     * @param id    An Id of an {@link Questionnaire} object.
     * @param model The model, which holds the information for the view.
     * @return The <i>mapping/uploadtemplate</i> website.
     */
    @GetMapping(value = "/mapping/uploadtemplate")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String showUploadForm(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);
        model.addAttribute("export", new ExportTemplate());
        model.addAttribute("questionnaire", questionnaire);
        return "mapping/uploadtemplate";
    }

    /**
     * Controls the HTTP Post request for the URL
     * <i>/mapping/uploadtemplate/</i>. Uploads a new export template file.
     *
     * @param questionnaireId The questionnaire id corresponding to the export template.
     * @param name            The name of the exportTemplate template.
     * @param file            The file associated to the exportTemplate template.
     * @param type            The export template type.
     * @param export          An {@link ExportTemplate} object.
     * @param result          The result for validation of the {@link ExportTemplate} object.
     * @param request         The request, which was sent from the client's browser.
     * @param model           The model, which holds the information for the view.
     * @return The <i>/mapping/list</i> website.
     */
    @PostMapping(value = "/mapping/uploadtemplate")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String handleUpload(
        @RequestParam(value = "questionnaire_id", required = true) final Long questionnaireId,
        @RequestParam(value = "name", required = true) final String name,
        @RequestParam(value = "file", required = true) final MultipartFile file,
        @RequestParam(value = "type", required = true) final String type,
        @ModelAttribute("export") final ExportTemplate export, final BindingResult result,
        final HttpServletRequest request, final Model model) {

        String locale = LocaleContextHolder.getLocale().toString();

        if (name == null || name.isEmpty()) {
            result.reject("name",
                messageSource.getMessage("mapping.error.uploadtemplateName", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }
        if (file == null || file.getSize() == 0) {
            result.reject("file",
                messageSource.getMessage("mapping.error.uploadtemplateFile", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }
        // If basic validation fails, show the error messages
        if (result.hasErrors()) {
            return showUploadForm(questionnaireId, model);
        }

        // Get the appropriate configuration groups for this export type
        ExportTemplateType exportTemplateType = ExportTemplateType.valueOf(type);

        if (ExportTemplateType.isExportTemplateTypeAFhirType(exportTemplateType)) {
            String webappPath = request.getSession().getServletContext().getRealPath("") + "/";
            ImportQuestionnaireValidation validationResult = new ImportQuestionnaireValidation();

            FhirVersion matchingVersion =
                fhirVersionHelper.mapExportTemplateTypeToFhirVersion(exportTemplateType);

            fhirImporter.validateFhirFileAgainstFhirVersion(file, validationResult,
                matchingVersion, locale);

            if (validationResult.hasErrors()) {
                for (ImportQuestionnaireError error: validationResult.getValidationErrors()) {
                    if (error.getErrorArguments() != null && error.getDefaultErrorMessage() != null) {
                        result.reject(error.getErrorCode(), error.getErrorArguments(), error.getDefaultErrorMessage());
                    } else {
                        result.reject(error.getErrorCode());
                    }
                }
            }
        } else if (exportTemplateType == ExportTemplateType.REDCap) {
            // Validate the imported file
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<String, String> jsonAttributes = mapper.convertValue(
                    mapper.readTree(file.getInputStream()).get(0),
                    new TypeReference<Map<String, String>>() {
                    });
                if (!jsonAttributes.containsKey("record_id")) {
                    result.reject("file", messageSource.getMessage(
                        "mapping.error" + ".uploadtemplateREDCapFileMissingRecordId",
                        new Object[]{}, LocaleContextHolder.getLocale()));
                }
            } catch (IOException | NoSuchMessageException exception) {
                result.reject("file",
                    messageSource.getMessage("mapping.error.uploadtemplateREDCapFileError",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }

        if (result.hasErrors()) {
            return showUploadForm(questionnaireId, model);
        }

        Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireId);

        List<ExportTemplate> exportTemplates = ExportTemplate.createExportTemplates(name,
            exportTemplateType, file, configurationGroupDao, exportTemplateDao);
        //Create second list to avoid ConcurrentModificationException
        List<ExportTemplate> templates = new ArrayList<>();
        templates.addAll(exportTemplates);
        for (ExportTemplate template : templates) {
            try {
                // Replace umlauts and whitespace
                String uploadFilename =
                    template.getId() + "_" + template.getFilename() + file.getOriginalFilename()
                        .substring(file.getOriginalFilename().lastIndexOf("."));
                template.setFilename(uploadFilename);
                String objectStoragePath = configurationDao.getObjectStoragePath();
                // Save uploaded file and update xml filename in template
                String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
                File uploadDir = new File(contextPath);
                if (!uploadDir.isDirectory()) {
                    uploadDir.mkdirs();
                }
                File uploadFile = new File(contextPath, uploadFilename);
                uploadFile.createNewFile();
                
                //Do the upload for FHIR resource.
                if (ExportTemplateType.isExportTemplateTypeAFhirType(exportTemplateType)) {
                    try {
                        fhirImporter.uploadFhirExportTemplate(file, uploadFile, exportTemplateType);
                    } catch (Exception e) {
                        LOGGER.error("error while uploading a new export template {}", e);
                        for (ExportTemplate exportTemplate : exportTemplates) {
                            File exportFile = new File(export.getFilename());
                            if (exportFile.isFile()) {
                                exportFile.delete();
                            }
                            exportTemplateDao.remove(exportTemplate);
                        }
                        exportTemplateDao.remove(template);
                        result.reject("exportTemplate.import.fhir.error",
                            new Object[]{e.getLocalizedMessage()},
                            "Error while uploading export template: " + e.getMessage());
                        return showUploadForm(questionnaireId, model);
                    }
                } else {
                    FileUtils.writeByteArrayToFile(new File(contextPath, uploadFilename),
                        IOUtils.toByteArray(file.getInputStream()));
                }
                template.setQuestionnaire(questionnaire);
                questionnaire.addExportTemplate(template);
                exportTemplateDao.merge(template);
            } catch (IOException e) {
                // delete export template on error
                LOGGER.error("error while uploading a new export template {}", e);
                for (ExportTemplate exportTemplate : exportTemplates) {
                    File exportFile = new File(export.getFilename());
                    if (exportFile.isFile()) {
                        exportFile.delete();
                    }
                    exportTemplateDao.remove(exportTemplate);
                }
                return "redirect:/mapping/list?id=" + questionnaireId;
            }
        }

        // maybe not necessary
        questionnaireDao.merge(questionnaire);

        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("allMappings", this.getAllMappings(questionnaireId));

        return "redirect:/mapping/list?id=" + questionnaireId;
    }

    /**
     * Controls the HTTP requests for the URL <i>/mapping/remove</i>. Provides the ability to remove
     * an export template.
     *
     * @param id      An Id of an {@link ExportTemplate} object.
     * @param request The request, which was sent from the client's browser.
     * @param model   The model, which holds the information for the view.
     * @return The <i>mapping/list</i> website.
     */
    @RequestMapping(value = "/mapping/remove")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String removeExportTemplate(@RequestParam(value = "id", required = true) final Long id,
        final HttpServletRequest request, final Model model) {
        ExportTemplate exportTemplate = exportTemplateDao.getElementById(id);
        Questionnaire questionnaire = exportTemplate.getQuestionnaire();
        // an export template can not be deleted if used in a bundle
        if (exportTemplate.getBundleQuestionnaires() != null
            && !exportTemplate.getBundleQuestionnaires().isEmpty()) {
            model.addAttribute("error", messageSource.getMessage("mapping.error.assignedtobundle",
                new Object[]{exportTemplate.getName()}, LocaleContextHolder.getLocale()));
        } else {
            String objectStoragePath = configurationDao.getObjectStoragePath();
            // delete file from disk
            String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
            File templateFile = new File(contextPath, exportTemplate.getFilename());
            // only if exists and is a file
            if (templateFile.isFile()) {
                templateFile.delete();
            }
            // Update all encounters associated with the removed export template
            for (EncounterExportTemplate encounterExportTemplate : exportTemplate.getEncounterExportTemplates()) {
                Encounter encounter = encounterExportTemplate.getEncounter();
                encounter.removeEncounterExportTemplate(encounterExportTemplate);
                encounterDao.merge(encounter);
            }
            questionnaire.removeExportTemplate(exportTemplate);
            exportTemplateDao.remove(exportTemplate);
            questionnaireDao.merge(questionnaire);
        }

        return showMapping(questionnaire.getId(), model);
    }

    /**
     * Controls the HTTP Get request for the URL <i>/mapping/map</i>. Provides the ability to map a
     * questionnaire with an export template.
     *
     * @param id      An Id of an {@link ExportTemplate} object.
     * @param request The request, which was sent from the client's browser.
     * @param model   The model, which holds the information for the view.
     * @return The <i>/mapping/map</i> website.
     */
    @GetMapping(value = "/mapping/map")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String assignTemplate(@RequestParam(value = "id", required = true) final Long id,
        final HttpServletRequest request, final Model model) {
        final ExportTemplate exportTemplate = exportTemplateDao.getElementById(id);
        Questionnaire questionnaire = exportTemplate.getQuestionnaire();
        // The exportTemplate contains an outdated questionnaire for whatever
        // reasons
        // using the questionnaireDao is a workaround to also get all recently
        // added questions of the questionnaire
        questionnaire = questionnaireDao.getElementById(questionnaire.getId());

        // Sort the scores
        List<Score> scores = new ArrayList<>();
        scores.addAll(questionnaire.getScores());
        Collections.sort(scores, (Score o1, Score o2) -> o1.getName().compareTo(o2.getName()));

        model.addAttribute("scores", scores);
        model.addAttribute("exportEncounterFieldType", ExportEncounterFieldType.values());
        model.addAttribute("exportQuestionnaireFieldType", ExportScoreFieldType.values());
        model.addAttribute("exportDateFormatType", ExportDateFormatType.values());
        model.addAttribute("exportRoundingStrategyType", ExportRoundingStrategyType.values());
        model.addAttribute("exportDecimalDelimiterType", ExportDecimalDelimiterType.values());
        model.addAttribute("exportNumberType", ExportNumberType.values());
        model.addAttribute("export", exportTemplate);
        model.addAttribute("exportRules", new ExportRulesDTO());
        Set<Question> questions = new TreeSet<>();
        for (Question question : questionnaire.getQuestions()) {
            if (question.getQuestionType() != QuestionType.INFO_TEXT) {
                questions.add(question);
            }
        }
        model.addAttribute("questions", questions);
        // Add current language to the model
        Locale locale = LocaleContextHolder.getLocale();
        model.addAttribute("currentLanguage", locale.toString());

        try {
            // construct the context path based on the object storage path
            // and export template directory
            // and read the xml file from this directory
            String objectStoragePath = configurationDao.getObjectStoragePath();
            String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
            File file = new File(contextPath, exportTemplate.getFilename());
            FileInputStream inputStream = new FileInputStream(file);

            ExportTemplateType exportTemplateType = exportTemplate.getExportTemplateType();
            // instantiate a new importer based on the type of the export
            // template
            ExportTemplateImporter importer = exportTemplateType.createNewImporterInstance();
            // if no implementation for the importer exists throw error
            if (importer == null) {
                LOGGER.error("No Implementation found for {}",
                    exportTemplate.getExportTemplateType());
                throw new ReflectiveOperationException(
                    "No Implementation " + "found for " + exportTemplate.getExportTemplateType());
            }

            model.addAttribute("doc", importer.importFile(inputStream));
        } catch (ReflectiveOperationException | IOException | SAXException |
                 ParserConfigurationException e) {
            model.addAttribute("error", e);
        }
        return "mapping/map";
    }

    /**
     * Controls the HTTP Post request for the URL <i>/mapping/map/</i>. Provides the ability to map
     * a questionnaire with an exportTemplate.
     *
     * @param exportRulesDTO Representation of an {@link ExportRulesDTO} object.
     * @param result         The result for validation.
     * @param request        The request, which was sent from the client's browser.
     * @param model          The model, which holds the information for the view.
     * @return The <i>/mapping/list</i> website.
     */
    @PostMapping(value = "/mapping/map")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String submitAssignment(
        @ModelAttribute("exportRules") final ExportRulesDTO exportRulesDTO,
        final BindingResult result, final HttpServletRequest request, final Model model) {

        // Validation: Only if the exportRuleDTO is not null. If it is null
        // there
        // are not mapped questions for this export, which means we do not
        // have to validate
        // the rules.
        if (exportRulesDTO != null) {
            exportRulesDTOValidator.validate(exportRulesDTO, result);
        }

        if (result.hasErrors()) {
            return assignTemplate(exportRulesDTO.getExportTemplateId(), request, model);
        }

        updateExportMapping(exportRulesDTO);

        ExportTemplate exportTemplate = exportTemplateDao.getElementById(
            exportRulesDTO.getExportTemplateId());

        return "redirect:/mapping/list?id=" + exportTemplate.getQuestionnaire().getId();
    }

    public void updateExportMapping(ExportRulesDTO exportRulesDTO){
        for (ExportRuleDTO exportRuleDTO : exportRulesDTO.getExportRules()) {
            ExportTemplate exportTemplate = exportTemplateDao.getElementById(
                exportRulesDTO.getExportTemplateId());

            // the export rule belongs to an answer
            if (exportRuleDTO.getAnswerId() != null) {
                // get the corresponding format dto for later use
                ExportRuleFormatDTO formatDTO = new ExportRuleFormatDTO();
                //if no there are no questions in questionnaire,
                // getExportRuleFormat points at null
                if (exportRulesDTO.getExportRuleFormats() != null) {
                    formatDTO = exportRulesDTO.getExportRuleFormats()
                        .get(exportRuleDTO.getTempExportFormatId());
                }
                Answer answer = answerDao.getElementById(exportRuleDTO.getAnswerId());
                // get all existing export rules for the given export
                // template and answer object
                Set<ExportRule> exportRules = Sets.intersection(exportTemplate.getExportRules(),
                    answer.getExportRules());
                // create a map between the export field and export rule
                Map<String, ExportRule> fieldRuleMap = new HashMap<>();
                for (ExportRule exportRule : exportRules) {
                    if (answer instanceof SliderFreetextAnswer) {
                        // special case for appended freetext to slider answers
                        if (exportRuleDTO.getUseFreetextValue() != null
                            && exportRuleDTO.getUseFreetextValue()) {
                            if (((ExportRuleAnswer) exportRule).getUseFreetextValue()) {
                                fieldRuleMap.put(exportRule.getExportField(), exportRule);
                            }
                        } else if (!((ExportRuleAnswer) exportRule).getUseFreetextValue()) {
                            fieldRuleMap.put(exportRule.getExportField(), exportRule);
                        }
                        // special case END (should be removed if the
                        // appended freetext will be implemented as a real
                        // answer)
                    } else {
                        // default case
                        fieldRuleMap.put(exportRule.getExportField(), exportRule);
                    }
                }
                // all assigned export fields for the export rule
                final List<String> newExportFields = exportRuleDTO.getExportField();
                if (newExportFields == null || newExportFields.isEmpty()) {
                    // remove all export rules for this answer if none is
                    // assigned
                    for (String exportField : fieldRuleMap.keySet()) {
                        ExportRule exportRule = fieldRuleMap.get(exportField);
                        ExportRuleFormat format = exportRule.getExportRuleFormat();
                        format.removeExportRule(exportRule);
                        exportTemplate.removeExportRule(exportRule);
                        answer.removeExportRule((ExportRuleAnswer) exportRule);
                        if (format.getExportRules().isEmpty()) {
                            exportRuleFormatDao.remove(format);
                        } else {
                            exportRuleFormatDao.merge(format);
                        }
                        exportRuleDao.remove(exportRule);
                        answerDao.merge(answer);
                        exportTemplateDao.merge(exportTemplate);
                    }
                    continue;
                }
                // get all already existing export fields for this export
                // template and answer
                List<String> exportFields = exportTemplate.getExportFieldsByAnswer(answer,
                    exportRuleDTO.getUseFreetextValue());
                for (String exportField : newExportFields) {
                    // we always have to get an updated answer representation
                    // to get all recently added export rules and formats
                    answer = answerDao.getElementById(answer.getId());
                    // all answers from a question share the same export rule
                    // format
                    // so if another answer from the question has an export rule
                    // assign the same format to the new export rule
                    ExportRuleFormat exportRuleFormat = answer.getQuestion()
                        .getExportRuleFormatFromAnswers(exportTemplate);
                    if (fieldRuleMap.containsKey(exportField)) {
                        // an exportRule already exists
                        // update the rule, format and move on
                        ExportRuleAnswer exportRule = (ExportRuleAnswer) fieldRuleMap.get(
                            exportField);
                        exportRule.setExportField(exportField);
                        exportRule.setUseFreetextValue(exportRuleDTO.getUseFreetextValue());
                        exportRuleFormat = exportRule.getExportRuleFormat();
                        // set the new format settings based on the users input
                        exportRuleFormat.setDateFormat(formatDTO.getDateFormat());
                        exportRuleFormat.setDecimalDelimiter(formatDTO.getDecimalDelimiter());
                        if (formatDTO.getDecimalPlaces() != null) {
                            exportRuleFormat.setDecimalPlaces(
                                Integer.parseInt(formatDTO.getDecimalPlaces()));
                        }
                        exportRuleFormat.setNumberType(formatDTO.getNumberType());
                        exportRuleFormat.setRoundingStrategy(formatDTO.getRoundingStrategy());
                        exportRuleDao.merge(exportRule);
                        exportFields.remove(exportField);
                    } else {
                        // no exportRule exists
                        // create a new export rule and get format from other
                        // export rules if available
                        // otherwise create an empty format
                        if (exportRuleFormat == null) {
                            exportRuleFormat = new ExportRuleFormat();
                        }
                        // set the new format settings based on the users input
                        exportRuleFormat.setDateFormat(formatDTO.getDateFormat());
                        exportRuleFormat.setDecimalDelimiter(formatDTO.getDecimalDelimiter());
                        if (formatDTO.getDecimalPlaces() != null) {
                            exportRuleFormat.setDecimalPlaces(
                                Integer.parseInt(formatDTO.getDecimalPlaces()));
                        }
                        exportRuleFormat.setNumberType(formatDTO.getNumberType());
                        exportRuleFormat.setRoundingStrategy(formatDTO.getRoundingStrategy());
                        ExportRule exportRule = new ExportRuleAnswer(exportTemplate, exportField,
                            answer, exportRuleDTO.getUseFreetextValue());
                        // Persist the new rule to be able to attach the
                        // already existing format
                        exportRuleDao.merge(exportRule);
                        exportRuleFormat.addExportRule(exportRule);
                        exportRuleDao.merge(exportRule);
                        answerDao.merge(answer);
                        exportTemplateDao.merge(exportTemplate);
                    }
                }
                // remove all remaining export rules because those are the
                // ones which are no longer assigned
                for (String exportField : exportFields) {
                    ExportRule exportRule = fieldRuleMap.get(exportField);
                    ExportRuleFormat format = exportRule.getExportRuleFormat();
                    format.removeExportRule(exportRule);
                    exportTemplate.removeExportRule(exportRule);
                    answer.removeExportRule((ExportRuleAnswer) exportRule);
                    exportRuleDao.merge(exportRule);
                    answerDao.merge(answer);
                    exportTemplateDao.merge(exportTemplate);
                }
                // the export rule belongs to a question
            } else if (exportRuleDTO.getQuestionId() != null) {
                // get the corresponding format dto for later use
                ExportRuleFormatDTO formatDTO = new ExportRuleFormatDTO();
                // if there are no questions in questionnaire,
                // getExportRuleFormat points at null
                if (exportRulesDTO.getExportRuleFormats() != null) {
                    formatDTO = exportRulesDTO.getExportRuleFormats()
                        .get(exportRuleDTO.getTempExportFormatId());
                }
                Question question = questionDao.getElementById(exportRuleDTO.getQuestionId());
                // get all existing export rules for the given export
                // template and question object
                Set<ExportRule> exportRules = Sets.intersection(exportTemplate.getExportRules(),
                    question.getExportRules());
                // create a map between the export field and export rule
                Map<String, ExportRule> fieldRuleMap = new HashMap<>();
                for (ExportRule exportRule : exportRules) {
                    fieldRuleMap.put(exportRule.getExportField(), exportRule);
                }
                // all assigned export fields for the export rule
                final List<String> newExportFields = exportRuleDTO.getExportField();
                if (newExportFields == null || newExportFields.isEmpty()) {
                    // remove all export rules for this answer if none is
                    // assigned
                    for (String exportField : fieldRuleMap.keySet()) {
                        ExportRule exportRule = fieldRuleMap.get(exportField);
                        ExportRuleFormat format = exportRule.getExportRuleFormat();
                        format.removeExportRule(exportRule);
                        exportTemplate.removeExportRule(exportRule);
                        question.removeExportRule((ExportRuleQuestion) exportRule);
                        if (format.getExportRules().isEmpty()) {
                            exportRuleFormatDao.remove(format);
                        } else {
                            exportRuleFormatDao.merge(format);
                        }
                        exportRuleDao.remove(exportRule);
                        questionDao.merge(question);
                        exportTemplateDao.merge(exportTemplate);
                    }
                    continue;
                }
                // get all already existing export fields for this export
                // template and question
                List<String> exportFields = exportTemplate.getExportFieldsByQuestion(question);
                for (String exportField : newExportFields) {
                    // we always have to get an updated question representation
                    // to get all recently added export rules and formats
                    question = questionDao.getElementById(question.getId());
                    // all questions from a question share the same export
                    // rule format
                    // so if another question from the question has an export
                    // rule
                    // assign the same format to the new export rule
                    ExportRuleFormat exportRuleFormat = question.getExportRuleFormatFromQuestion(
                        exportTemplate);
                    if (fieldRuleMap.containsKey(exportField)) {
                        // an exportRule already exists
                        // update the rule, format and move on
                        ExportRuleQuestion exportRule = (ExportRuleQuestion) fieldRuleMap.get(
                            exportField);
                        exportRule.setExportField(exportField);
                        exportRuleFormat = exportRule.getExportRuleFormat();
                        // set the new format settings based on the users input
                        exportRuleFormat.setDateFormat(formatDTO.getDateFormat());
                        exportRuleFormat.setDecimalDelimiter(formatDTO.getDecimalDelimiter());
                        if (formatDTO.getDecimalPlaces() != null) {
                            exportRuleFormat.setDecimalPlaces(
                                Integer.parseInt(formatDTO.getDecimalPlaces()));
                        }
                        exportRuleFormat.setNumberType(formatDTO.getNumberType());
                        exportRuleFormat.setRoundingStrategy(formatDTO.getRoundingStrategy());
                        exportRuleDao.merge(exportRule);
                        exportFields.remove(exportField);
                    } else {
                        // no exportRule exists
                        // create a new export rule and get format from other
                        // export rules if available
                        // otherwise create an empty format
                        if (exportRuleFormat == null) {
                            exportRuleFormat = new ExportRuleFormat();
                        }
                        // set the new format settings based on the users input
                        exportRuleFormat.setDateFormat(formatDTO.getDateFormat());
                        exportRuleFormat.setDecimalDelimiter(formatDTO.getDecimalDelimiter());
                        if (formatDTO.getDecimalPlaces() != null) {
                            exportRuleFormat.setDecimalPlaces(
                                Integer.parseInt(formatDTO.getDecimalPlaces()));
                        }
                        exportRuleFormat.setNumberType(formatDTO.getNumberType());
                        exportRuleFormat.setRoundingStrategy(formatDTO.getRoundingStrategy());
                        ExportRule exportRule = new ExportRuleQuestion(exportTemplate, exportField,
                            question);
                        // Persist the new rule to be able to attach the
                        // already existing format
                        exportRuleDao.merge(exportRule);
                        exportRuleFormat.addExportRule(exportRule);
                        exportRuleDao.merge(exportRule);
                        questionDao.merge(question);
                        exportTemplateDao.merge(exportTemplate);
                    }
                }
                // remove all remaining export rules because those are the
                // ones which are no longer assigned
                for (String exportField : exportFields) {
                    ExportRule exportRule = fieldRuleMap.get(exportField);
                    ExportRuleFormat format = exportRule.getExportRuleFormat();
                    format.removeExportRule(exportRule);
                    exportTemplate.removeExportRule(exportRule);
                    question.removeExportRule((ExportRuleQuestion) exportRule);
                    exportRuleDao.merge(exportRule);
                    questionDao.merge(question);
                    exportTemplateDao.merge(exportTemplate);
                }
                // the export rule belongs to an enocunter field
            } else if (exportRuleDTO.getEncounterField() != null) {
                // get all existing export rules for the given export
                // template and encounter field
                Set<ExportRuleEncounter> exportRules = exportTemplate.getExportRulesByEncounterField(
                    exportRuleDTO.getEncounterField());
                // create a map between the export field and export rule
                Map<String, ExportRuleEncounter> fieldRuleMap = new HashMap<>();
                for (ExportRuleEncounter exportRule : exportRules) {
                    fieldRuleMap.put(exportRule.getExportField(), exportRule);
                }
                final List<String> newExportFields = exportRuleDTO.getExportField();
                if (newExportFields == null || newExportFields.isEmpty()) {
                    // remove all export rules for this encounter field
                    for (String exportField : fieldRuleMap.keySet()) {
                        ExportRule exportRule = fieldRuleMap.get(exportField);
                        exportTemplate.removeExportRule(exportRule);
                        exportRule.removeExportTemplate();
                        exportRuleDao.remove(exportRule);
                        exportTemplateDao.merge(exportTemplate);
                    }
                    continue;
                }
                Set<String> exportFields = exportTemplate.getExportFieldsByEncounterField(
                    exportRuleDTO.getEncounterField());
                for (String exportField : newExportFields) {
                    // we always have to get an updated export template
                    // representation
                    // to get all recently added export rules and the formats
                    exportTemplate = exportTemplateDao.getElementById(exportTemplate.getId());
                    ExportRuleFormat exportRuleFormat = exportTemplate.getExportRuleFormatFromEncounterField(
                        exportRuleDTO.getEncounterField());
                    if (fieldRuleMap.containsKey(exportField)) {
                        // an exportRule already exists
                        // update the rule, format and move on
                        ExportRule exportRule = fieldRuleMap.get(exportField);
                        exportRule.setExportField(exportField);
                        exportRule.getExportRuleFormat()
                            .setDateFormat(exportRuleDTO.getEncounterDateFormat());
                        exportRuleDao.merge(exportRule);
                        exportTemplateDao.merge(exportTemplate);
                        exportFields.remove(exportField);
                    } else {
                        // no exportRule exists
                        // create a new export rule and get format from other
                        // export rules if available
                        // otherwise create an empty format
                        if (exportRuleFormat == null) {
                            exportRuleFormat = new ExportRuleFormat();
                        }
                        ExportRule exportRule = new ExportRuleEncounter(exportTemplate, exportField,
                            exportRuleDTO.getEncounterField());
                        // Persist the new rule to be able to attach the
                        // already existing format
                        exportRuleDao.merge(exportRule);
                        exportRuleFormat.setDateFormat(exportRuleDTO.getEncounterDateFormat());
                        exportRule.setExportRuleFormat(exportRuleFormat);
                        exportRuleDao.merge(exportRule);
                        exportTemplateDao.merge(exportTemplate);
                    }
                }
                // remove all remaining export rules
                // because those are the ones which are no longer assigned
                for (String exportField : exportFields) {
                    ExportRule exportRule = fieldRuleMap.get(exportField);
                    exportRule.removeExportRuleFormat();
                    exportTemplate.removeExportRule(exportRule);
                    exportRule.removeExportTemplate();
                    exportRuleDao.remove(exportRule);
                    exportTemplateDao.merge(exportTemplate);
                }
                // the export rule belongs to an questionnaire field
            } else if (exportRuleDTO.getScoreId() != null) {
                // get the corresponding format dto for later use
                ExportRuleFormatDTO formatDTO = new ExportRuleFormatDTO();
                if (exportRulesDTO.getExportRuleScoreFormats() != null) {
                    formatDTO = exportRulesDTO.getExportRuleScoreFormats()
                        .get(exportRuleDTO.getTempExportFormatId());
                }

                Score score = scoreDao.getElementById(exportRuleDTO.getScoreId());
                // get all existing export rules for the given score,
                // export template and questionnaire field
                Set<ExportRuleScore> exportRules = exportTemplate.getExportRulesByScoreField(
                    exportRuleDTO.getScoreField(), score.getId());
                // create a map between the export field and export rule
                Map<String, ExportRuleScore> fieldRuleMap = new HashMap<>();
                for (ExportRuleScore exportRule : exportRules) {
                    fieldRuleMap.put(exportRule.getExportField(), exportRule);
                }
                final List<String> newExportFields = exportRuleDTO.getExportField();
                if (newExportFields == null || newExportFields.isEmpty()) {
                    // remove all export rules for this score field
                    for (String exportField : fieldRuleMap.keySet()) {
                        ExportRule exportRule = fieldRuleMap.get(exportField);
                        exportTemplate.removeExportRule(exportRule);
                        exportRule.removeExportTemplate();
                        score.removeExportRule((ExportRuleScore) exportRule);
                        exportRuleDao.remove(exportRule);
                        scoreDao.merge(score);
                        exportTemplateDao.merge(exportTemplate);
                    }
                    continue;
                }

                Set<String> exportFields = exportTemplate.getExportFieldsByScoreField(
                    exportRuleDTO.getScoreField(), score.getId());
                for (String exportField : newExportFields) {
                    // we always have to get an updated export template
                    // representation
                    // to get all recently added export rules and the formats
                    exportTemplate = exportTemplateDao.getElementById(exportTemplate.getId());
                    ExportRuleFormat exportRuleFormat = exportTemplate.getExportRuleFormatFromScoreField(
                        exportRuleDTO.getScoreField(), score.getId());
                    if (fieldRuleMap.containsKey(exportField)) {
                        // an exportRule already exists
                        // update the rule, format and move on
                        ExportRule exportRule = fieldRuleMap.get(exportField);
                        exportRule.setExportField(exportField);
                        exportRuleFormat = exportRule.getExportRuleFormat();
                        // set the new format settings based on the users input
                        exportRuleFormat.setDateFormat(formatDTO.getDateFormat());
                        exportRuleFormat.setDecimalDelimiter(formatDTO.getDecimalDelimiter());
                        if (formatDTO.getDecimalPlaces() != null) {
                            exportRuleFormat.setDecimalPlaces(
                                Integer.parseInt(formatDTO.getDecimalPlaces()));
                        }
                        exportRuleFormat.setNumberType(formatDTO.getNumberType());
                        exportRuleFormat.setRoundingStrategy(formatDTO.getRoundingStrategy());
                        exportRuleDao.merge(exportRule);
                        scoreDao.merge(score);
                        exportFields.remove(exportField);
                    } else {
                        // no exportRule exists
                        // create a new export rule
                        // and get format from other export rules if available
                        // otherwise create an empty format
                        if (exportRuleFormat == null) {
                            exportRuleFormat = new ExportRuleFormat();
                        }
                        // set the new format settings based on the users input
                        exportRuleFormat.setDateFormat(formatDTO.getDateFormat());
                        exportRuleFormat.setDecimalDelimiter(formatDTO.getDecimalDelimiter());
                        if (formatDTO.getDecimalPlaces() != null) {
                            exportRuleFormat.setDecimalPlaces(
                                Integer.parseInt(formatDTO.getDecimalPlaces()));
                        }
                        exportRuleFormat.setNumberType(formatDTO.getNumberType());
                        exportRuleFormat.setRoundingStrategy(formatDTO.getRoundingStrategy());

                        ExportRule exportRule = new ExportRuleScore(exportTemplate, exportField,
                            score, exportRuleDTO.getScoreField());
                        // Persist the new rule to be able to attach the
                        // already existing format
                        exportRuleDao.merge(exportRule);
                        exportRuleFormat.addExportRule(exportRule);
                        exportRuleDao.merge(exportRule);
                        scoreDao.merge(score);
                        exportTemplateDao.merge(exportTemplate);
                    }
                }
                // remove all remaining export rules
                // because those are the ones which are no longer assigned
                for (String exportField : exportFields) {
                    ExportRule exportRule = fieldRuleMap.get(exportField);
                    ExportRuleFormat format = exportRule.getExportRuleFormat();
                    format.removeExportRule(exportRule);
                    score.removeExportRule((ExportRuleScore) exportRule);
                    exportTemplate.removeExportRule(exportRule);
                    exportRuleFormatDao.merge(format);
                    exportTemplateDao.merge(exportTemplate);
                    scoreDao.merge(score);
                    exportTemplateDao.merge(exportTemplate);
                }

            }
        }

    }
}
