package de.imi.mopat.controller;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.AuthService;
import de.imi.mopat.helper.controller.FhirVersionHelper;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.QuestionnaireService;
import de.imi.mopat.helper.controller.QuestionnaireVersionGroupService;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.io.impl.MetadataExporterFactory;
import de.imi.mopat.io.importer.ImportQuestionnaireResult;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.io.importer.MoPatQuestionnaireImporter;
import de.imi.mopat.io.importer.MopatCompleteQuestionnaireImporter;
import de.imi.mopat.io.importer.fhir.FhirImporter;
import de.imi.mopat.io.importer.odm.ODMProcessingBean;
import de.imi.mopat.io.importer.odm.OdmQuestionnaireImporter;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.enumeration.FhirVersion;
import de.imi.mopat.model.enumeration.MetadataFormat;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.validator.QuestionValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 */
@Controller
public class QuestionnaireController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(QuestionnaireController.class);

    @Autowired
    MoPatQuestionnaireImporter moPatQuestionnaireImporter;

    @Autowired
    MopatCompleteQuestionnaireImporter mopatCompleteQuestionnaireImporter;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private BundleDao bundleDao;

    @Autowired
    private ConditionDao conditionDao;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ScoreDao scoreDao;

    @Autowired
    private OperatorDao operatorDao;

    @Autowired
    private QuestionValidator questionValidator;

    @Autowired
    private ExportTemplateDao exportTemplateDao;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private StringUtilities stringUtilityHelper;

    @Autowired
    private LocaleHelper localeHelper;

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private AuthService authService;

    @Autowired
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Autowired
    private FhirImporter fhirImporter;

    @Autowired
    private ODMProcessingBean odmReader;

    @Autowired
    private OdmQuestionnaireImporter odmQuestionnaireImporter;

    @Autowired
    private FhirVersionHelper fhirVersionHelper;

    @Autowired
    private MetadataExporterFactory metadataExporterFactory;

    /**
     * Controls the HTTP GET requests for the URL <i>/questionnaire/list</i>. Shows the list of
     * questionnaires.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>questionnaire/list</i> website.
     */
    @RequestMapping(value = "/questionnaire/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String listQuestionnaires(final Model model) {
        List<Questionnaire> allQuestionnaires = questionnaireDao.getAllElements();
        // This map contians a questionnaire id as key and a set with all
        // languages
        // which are available for all questions in this questionnaire.
        Map<Long, List<String>> availableLanguagesInQuestionForQuestionnaires = new HashMap<>();

        // Create a map where the key is a question id and the value a sorted
        // map, which
        // contains the question texts grouped by the country and languages.
        Map<Long, SortedMap<String, Map<String, String>>> localizedDisplayNamesForQuestionnaire = new HashMap<>();

        Set<Long> questionnaireIds = questionnaireService.getUniqueQuestionnaireIds(allQuestionnaires);
        Set<Long> questionnaireTargetIds =
                conditionDao.findConditionTargetIds(questionnaireIds.stream().toList(), "Questionnaire");

        for (Questionnaire questionnaire : allQuestionnaires) {
            // Get the question texts grouped by country from the current
            // question
            SortedMap<String, Map<String, String>> groupedLocalizedDisplayNameByCountry =
                    questionnaire.getLocalizedDisplayNamesGroupedByCountry();
            // And add the grouped-by-country-map to the map for all questions
            // of the current questionnaire
            localizedDisplayNamesForQuestionnaire.put(questionnaire.getId(), groupedLocalizedDisplayNameByCountry);
            availableLanguagesInQuestionForQuestionnaires.put(
                    questionnaire.getId(), questionnaire.getAvailableQuestionLanguages());
            // Check if the questionnaire has any conditions and set the boolean
            questionnaire.setHasConditions(questionnaireTargetIds.contains(questionnaire.getId()));
        }

        model.addAttribute("allQuestionnaires", questionnaireService.sortQuestionnairesByNameAsc(allQuestionnaires));
        model.addAttribute(
                "availableLanguagesInQuestionForQuestionnaires", availableLanguagesInQuestionForQuestionnaires);
        model.addAttribute("localizedDisplayNamesForQuestionnaire", localizedDisplayNamesForQuestionnaire);
        return "questionnaire/list";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/questionnaire/fill</i>. Shows the page
     * containing the form fields for a new {@link Questionnaire} object.
     *
     * @param questionnaireId The id of a {@link Questionnaire} object.
     * @param model           The model, which holds the information for the view.
     * @param request         The current request.
     * @return The <i>questionnaire/fill</i> website.
     */
    @RequestMapping(value = "/questionnaire/fill", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String fillQuestionnaire(
            @RequestParam(value = "id", required = false) final Long questionnaireId,
            final HttpServletRequest request,
            final Model model) {
        QuestionnaireDTO questionnaireDTO =
                questionnaireService.getQuestionnaireDTOById(questionnaireId).orElse(new QuestionnaireDTO());
        Pair<Boolean, String> canEditWithReason = questionnaireService.canEditQuestionnaireWithReason(questionnaireDTO);

        model.addAttribute("isEditableState", canEditWithReason.getLeft());
        model.addAttribute("infoMessage", canEditWithReason.getRight());
        model.addAttribute("questionnaireDTO", questionnaireDTO);
        model.addAttribute("localeHelper", localeHelper);
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        return "questionnaire/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/questionnaire/edit</i>. Provides the ability
     * to create a new {@link Questionnaire} object.
     *
     * @param action           The name of the submit button which has been clicked.
     * @param questionnaireDTO The {@link QuestionnaireDTO} object from the view.
     * @param result           The result for validation of the {@link Questionnaire} object.
     * @param model            The model, which holds the information for the view.
     * @param logo             The logo for the questionnaire
     * @param request          The current request.
     * @return Redirect to the <i>questionnaire/list</i> website.
     */
    @RequestMapping(value = "/questionnaire/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String edit(
            @RequestParam final String action,
            @RequestParam(value = "logoFile", required = false) final MultipartFile logo,
            @ModelAttribute("questionnaireDTO") @Valid final QuestionnaireDTO questionnaireDTO,
            final BindingResult result,
            final Model model,
            final HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (action.equalsIgnoreCase("cancel")) {
            return "redirect:/questionnaire/list";
        }

        questionnaireService.processLocalizedText(questionnaireDTO);

        questionnaireService.validateQuestionnaire(questionnaireDTO, logo, result);
        if (result.hasErrors()) {
            fillModelForValidationErrors(questionnaireDTO, model);
            return "questionnaire/edit";
        }

        Long principalId = authService.getAuthenticatedUserId();
        Questionnaire questionnaire =
                questionnaireService.saveOrUpdateQuestionnaire(questionnaireDTO, logo, principalId);
        Boolean hasQuestionnaireConditions = questionnaireService.hasQuestionnaireConditions(
                questionnaireDao.getElementById(questionnaireDTO.getId()));
        redirectAttributes.addFlashAttribute("hasQuestionnaireConditions", hasQuestionnaireConditions);
        if (action.equals("saveEditButton")) {
            return "redirect:/question/list?id=" + questionnaire.getId();
        } else {
            return "redirect:/questionnaire/list";
        }
    }

    /**
     * Populates the MVC {@link Model} with attributes required to re-render the questionnaire form
     * after validation errors, including editability state, the current DTO, and available
     * locales.
     *
     * @param questionnaireDTO form-backing DTO containing the submitted values
     * @param model            model to fill for view rendering
     */
    private void fillModelForValidationErrors(QuestionnaireDTO questionnaireDTO, Model model) {
        boolean isEditableState = true;

        if (questionnaireDTO.getId() != null) {
            Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());
            if (existingQuestionnaire != null) {
                questionnaireDTO.setLogo(existingQuestionnaire.getLogo());
                isEditableState = questionnaireService.editingQuestionnaireAllowed(questionnaireDTO);
            }
        }
        model.addAttribute("isEditableState", isEditableState);
        model.addAttribute("questionnaireDTO", questionnaireDTO);
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
    }

    /**
     * Controls the HTTP requests for the URL <i>questionnaire/remove</i>. Removes a
     * {@link Questionnaire Questionnaire} object by a given id and redirects to the list of
     * questionnaires.
     *
     * @param id    Id of the {@link Questionnaire} object, which should be removed.
     * @param model The model, which holds the information for the view.
     * @return Redirect to the <i>questionnaire/list</i> website.
     */
    @RequestMapping(value = "/questionnaire/remove")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String removeQuestionnaire(@RequestParam(value = "id", required = true) final Long id, final Model model) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);
        if (questionnaire != null) {
            if (questionnaire.isDeletable()) {
                // Delete the associated conditions
                for (Condition condition : conditionDao.getConditionsByTarget(questionnaire)) {
                    if (condition instanceof SelectAnswerCondition
                            || condition instanceof SliderAnswerThresholdCondition) {
                        // Refresh the trigger so that multiple conditions of
                        // the same trigger will be deleted correctly
                        ConditionTrigger conditionTrigger =
                                answerDao.getElementById(condition.getTrigger().getId());
                        conditionTrigger.removeCondition(condition);
                        answerDao.merge((Answer) conditionTrigger);
                    }
                    conditionDao.remove(condition);
                }

                for (ExportTemplate exportTemplate : questionnaire.getExportTemplates()) {
                    // Remove ExportTemplates manually to prevent integrity clashes with scores
                    exportTemplateDao.remove(exportTemplate);
                }

                // Collect all scores in an array list to make sure they will
                // be removed in correct order
                List<Score> scoresToDelete = new ArrayList<>();
                for (Score scoreToDelete : questionnaire.getScores()) {
                    List<Score> dependingScores = scoreToDelete.getDependingScores();
                    // Sort depending scores by amount of their depending
                    // scores to prevent database errors
                    Collections.sort(
                            dependingScores,
                            (Score o1, Score o2) -> o1.getDependingScores().size()
                                    - o2.getDependingScores().size());
                    // First add all depending scores
                    for (Score dependingScore : dependingScores) {
                        if (!scoresToDelete.contains(dependingScore)) {
                            scoresToDelete.add(dependingScore);
                        }
                    }
                    // Add the score that actually has to be deleted
                    if (!scoresToDelete.contains(scoreToDelete)) {
                        scoresToDelete.add(scoreToDelete);
                    }
                }

                // Delete the associated scores
                Iterator<Score> iterator = scoresToDelete.iterator();
                while (iterator.hasNext()) {
                    Score scoreToDelete = iterator.next();
                    iterator.remove();
                    scoreDao.remove(scoreToDelete);
                }

                // Delete connection to the bundles
                for (BundleQuestionnaire bundleQuestionnaire : questionnaire.getBundleQuestionnaires()) {
                    Bundle bundle = bundleQuestionnaire.getBundle();
                    bundle.removeBundleQuestionnaire(bundleQuestionnaire);
                    // Update the position of all following bundleQuestionnaires
                    for (BundleQuestionnaire bundleQuestionnaireToChangePosition : bundle.getBundleQuestionnaires()) {
                        if (bundleQuestionnaireToChangePosition.getPosition() > bundleQuestionnaire.getPosition()) {
                            bundleQuestionnaireToChangePosition.setPosition(
                                    bundleQuestionnaireToChangePosition.getPosition() - 1);
                        }
                    }
                    bundleDao.merge(bundle);
                }
                questionnaire.removeAllBundleQuestionnaires();
                questionnaireVersionGroupService.removeQuestionnaire(
                        questionnaire.getQuestionnaireVersionGroupId(), questionnaire);
                questionnaireDao.remove(questionnaire);
                model.addAttribute(
                        "messageSuccess",
                        messageSource.getMessage(
                                "questionnaire.error" + ".deleteQuestionnairePossible",
                                new Object[] {questionnaire.getName()},
                                LocaleContextHolder.getLocale()));
            } else {
                model.addAttribute(
                        "messageFail",
                        messageSource.getMessage(
                                "questionnaire.error" + ".deleteQuestionnaireNotPossible",
                                new Object[] {questionnaire.getName()},
                                LocaleContextHolder.getLocale()));
            }
        }
        return listQuestionnaires(model);
    }

    /**
     * Controls the HTTP requests for the URL <i>questionnaire/download</i>. Downloads a
     * {@link Questionnaire} object by a given id in a given type and redirects to the list of
     * questionnaires.
     *
     * @param id    Id of the {@link Questionnaire} object, which should be downloaded.
     * @param type  Exporttype of the file, which should be downloaded.
     * @param model The model, which holds the information for the view.
     * @return Returns the requested download file and redirects to the
     * <i>questionnaire/list</i> website.
     */
    @RequestMapping(value = "/questionnaire/download")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public ResponseEntity<ByteArrayResource> downloadQuestionnaire(
            @RequestParam(value = "id", required = true) final Long id,
            @RequestParam(value = "type", required = true) final List<String> types,
            final Model model) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);

        if (questionnaire == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "list");
            return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
        }

        List<Path> paths = new ArrayList<>();
        List<byte[]> dataList = new ArrayList<>();

        for (String type : types) {
            MetadataExporter exporter = metadataExporterFactory.getMetadataExporter(MetadataFormat.valueOf(type));

            for (Question question : questionnaire.getQuestions()) {
                question.setHasConditionsAsTarget(conditionDao.isConditionTarget(question));
            }

            byte[] data = exporter.export(
                    questionnaire,
                    messageSource,
                    configurationDao,
                    configurationGroupDao,
                    exportTemplateDao,
                    questionnaireDao,
                    questionDao,
                    scoreDao);

            // Create a windows-compliant path/filename
            Path path = Paths.get(
                    questionnaire.getName().replaceAll("[\\\\/:;*?\"<>|]", "").replaceAll(" ", "_") + "_" + type
                            + MetadataFormat.valueOf(type).getFileExtension());

            paths.add(path);
            dataList.add(data);
        }

        if (paths.size() == 1) {
            Path path = paths.get(0);
            byte[] data = dataList.get(0);
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment;filename=" + path.getFileName().toString())
                    .contentLength(data.length)
                    .body(resource);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (int i = 0; i < paths.size(); i++) {
                Path path = paths.get(i);
                byte[] data = dataList.get(i);

                ZipEntry entry = new ZipEntry(path.getFileName().toString());
                zos.putNextEntry(entry);
                zos.write(data);
                zos.closeEntry();
            }

            zos.finish();
            ByteArrayResource zipResource = new ByteArrayResource(baos.toByteArray());

            String zipName =
                    questionnaire.getName().replaceAll("[\\\\/:;*?\"<>|]", "").replaceAll(" ", "_") + "_exports.zip";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipName)
                    .contentLength(baos.size())
                    .body(zipResource);

        } catch (IOException e) {
            throw new RuntimeException("Error creating ZIP file", e);
        }
    }

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/questionnaire/import/upload</i> Shows the page to upload/import a
     * {@link Questionnaire} from a file.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>questionnaire/import/upload</i> website.
     */
    @RequestMapping(value = "/questionnaire/import/upload", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String getImportUpload(final Model model) {
        List<String> templateTypes = new ArrayList<>();
        templateTypes.add("ODM");
        templateTypes.add("FHIR");

        model.addAttribute("templateTypes", templateTypes);
        model.addAttribute("questionnaire", new Questionnaire());
        return "questionnaire/import/upload";
    }

    /**
     * Processes the questionnaire import request by delegating to the appropriate handler
     * based on the selected upload type.
     * <p>
     * This method acts as a dispatcher for various import formats including MoPat (JSON),
     * ODM (XML), and different FHIR versions (DSTU3, R4B, R5). It supports both file
     * uploads and, in the case of FHIR, remote URL references.
     * </p>
     *
     * @param file          The multipart file containing the questionnaire data.
     * @param url           An optional remote URL for importing FHIR questionnaires.
     * @param uploadType    The format of the import (e.g., "MoPat", "ODM", "FHIRR4B").
     * @param questionnaire The {@link Questionnaire} model attribute from the form.
     * @param result        The {@link BindingResult} for error reporting across handlers.
     * @param model         The {@link Model} to supply data for the subsequent view.
     * @return A string representing the view name or a redirect, depending on the handler's output.
     */
    @RequestMapping(value = "/questionnaire/import/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String postImportUpload(
            @RequestParam(value = "file", required = true) final MultipartFile file,
            @RequestParam(value = "url", required = false) final String url,
            @RequestParam(value = "uploadType", required = true) final String uploadType,
            @ModelAttribute("questionnaire") Questionnaire questionnaire,
            final BindingResult result,
            final Model model) {

        switch (uploadType) {
            case "MoPat": {
                return handleMopatUpload(file, result, model);
            }

            case "ODM": {
                return handleOdmUpload(file, model, result);
            }

            case "FHIRDSTU3", "FHIRR4B", "FHIRR5": {
                return handleFhirUpload(file, uploadType, model, result, url);
            }
        }
        return "questionnaire/import/upload";
    }

    /**
     * Handles the upload and import of a MoPat-native JSON questionnaire.
     * <p>
     * This method attempts to parse a {@link MultipartFile} using the {@link MopatCompleteQuestionnaireImporter}.
     * If successful, it redirects the user to the questionnaire configuration page.
     * In case of an {@link IOException}, it logs the error and returns the user to the upload
     * page with a validation error message.
     * </p>
     *
     * @param file   The multipart file containing the JSON questionnaire data.
     * @param result The {@link BindingResult} used to store and display import errors.
     * @param model  The {@link Model} to supply data to the view.
     * @return A redirect string to the fill/edit page on success, or the upload view name on failure.
     */
    private String handleMopatUpload(MultipartFile file, BindingResult result, Model model) {
        Questionnaire questionnaire = null;
        try {
            model.addAttribute("fileUpload", true);
            questionnaire = mopatCompleteQuestionnaireImporter.importQuestionnaire(file);
        } catch (IOException e) {
            LOGGER.info(
                    "ERROR: Importing json formatted MoPat questionnaire " + "failed. The following error occurred: {}",
                    e.getLocalizedMessage());

            result.reject("import.error.fileNotSupported");

            return "questionnaire/import/upload";
        }
        return "redirect:/questionnaire/fill?id=" + questionnaire.getId();
    }

    /**
     * Handles the upload and processing of an Operational Data Model (ODM) XML file.
     * <p>
     * This method validates the file's ODM structure and uses the {@link OdmQuestionnaireImporter}
     * to extract questionnaire data. If the file is invalid or validation errors occur during
     * processing, it returns the user to the upload page with specific error messages.
     * On success, the import results are added to the model and the user is directed to the
     * result summary page.
     * </p>
     *
     * @param file   The XML file in ODM format to be imported.
     * @param model  The {@link Model} used to pass the {@link ImportQuestionnaireResult} and
     *               locale information to the view.
     * @param result The {@link BindingResult} used to capture and display validation or
     *               file-format errors.
     * @return The path to the import result view on success, or the upload view if errors are found.
     */
    private String handleOdmUpload(MultipartFile file, Model model, BindingResult result) {
        List<String> validationErrors = new ArrayList<>();
        Locale locale = LocaleContextHolder.getLocale();
        ImportQuestionnaireResult odmQuestionnaireResult = null;

        if (!checkValidOdmFile(file)) {
            result.reject("import.error.fileNotSupported");
            return "questionnaire/import/upload";
        }

        try {
            odmQuestionnaireResult = odmQuestionnaireImporter.importOdmQuestionnaire(file, validationErrors);
        } catch (Exception e) {
            LOGGER.error("An error occured during importing of ODM: {}", e);
            result.reject("questionnaire.import.failure");
        }

        if (!validationErrors.isEmpty()) {
            validationErrors.forEach(error -> result.reject(null, error));
            return "questionnaire/import/upload";
        } else {
            model.addAttribute("importQuestionnaireResult", odmQuestionnaireResult);
        }

        model.addAttribute("currentLanguage", locale.toString());
        return "questionnaire/import/result";
    }

    private boolean checkValidOdmFile(MultipartFile file) {
        if (Objects.requireNonNull(file.getOriginalFilename()).contains(".xml")) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);

                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(file.getInputStream());

                NodeList standardNodes = document.getElementsByTagName("ODM");
                NodeList customNodes = document.getElementsByTagName("odm:ODM");

                return (standardNodes != null && standardNodes.getLength() > 0)
                        || (customNodes != null && customNodes.getLength() > 0);

            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Imports a FHIR questionnaire from an uploaded file or URL and prepares the import result view.
     * <p>
     * Marks whether the upload looks like a supported file type (JSON/XML) via the {@code fileUpload}
     * model attribute. The FHIR version is derived from the provided {@code uploadType}.
     * </p>
     * <p>
     * If the import yields validation errors, they are translated into {@link BindingResult} rejections
     * and the upload page is rendered again. If the import succeeds, the import result is added to the
     * model and the result page is returned.
     * </p>
     * <p>
     * If an unexpected error occurs during import, a localized failure message is stored as flash
     * attribute and the user is redirected back to the upload page.
     * </p>
     *
     * @param file               the uploaded FHIR questionnaire file (JSON or XML)
     * @param uploadType         frontend upload type used to determine the {@code FhirVersion}
     * @param model              MVC model used to expose upload context, import result and current language
     * @param result             binding/validation result used to report validation errors back to the view
     * @param url                optional URL to a questionnaire resource used by the importer
     * @return the upload page view when validation errors exist, a redirect to the upload page on
     * unexpected failure, or {@code questionnaire/import/result} on success
     */
    private String handleFhirUpload(
            MultipartFile file, String uploadType, Model model, BindingResult result, String url) {
        Locale locale = LocaleContextHolder.getLocale();

        boolean isSupportedFile = file.getOriginalFilename().contains(".json")
                || file.getOriginalFilename().contains(".xml");
        model.addAttribute("fileUpload", isSupportedFile);

        FhirVersion fhirVersion = fhirVersionHelper.mapFrontendFhirStringToVersion(uploadType);

        try {
            ImportQuestionnaireValidation importResult =
                    fhirImporter.importFhirQuestionnaire(file, url, fhirVersion, locale.toString());

            if (importResult.hasErrors()) {
                importResult.getValidationErrors().forEach(error -> {
                    if (error.getErrorArguments() != null && error.getDefaultErrorMessage() != null) {
                        result.reject(error.getErrorCode(), error.getErrorArguments(), error.getDefaultErrorMessage());
                    } else {
                        result.reject(error.getErrorCode());
                    }
                });
                return "questionnaire/import/upload";
            } else {
                model.addAttribute("importQuestionnaireResult", importResult.getImportResult());
            }
        } catch (Exception e) {
            LOGGER.error("Could not upload FHIR questionnaire: ", e);
            result.reject("import.error.fileNotSupported");
            return "questionnaire/import/upload";
        }
        model.addAttribute("currentLanguage", locale.toString());
        return "questionnaire/import/result";
    }
}
