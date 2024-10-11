package de.imi.mopat.controller;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.FHIRHelper;
import de.imi.mopat.helper.controller.FHIRToMoPatConverter;
import de.imi.mopat.helper.controller.GraphicsUtilities;
import de.imi.mopat.helper.controller.ImportQuestionnaireResult;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.ODMProcessingBean;
import de.imi.mopat.helper.controller.ODMv132ToMoPatConverter;
import de.imi.mopat.helper.controller.QuestionnaireService;
import de.imi.mopat.helper.controller.AuthService;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.io.impl.MetadataExporterFactory;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonAnswerDTO;
import de.imi.mopat.model.dto.export.JsonConditionDTO;
import de.imi.mopat.model.dto.export.JsonQuestionDTO;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonScoreDTO;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.enumeration.MetadataFormat;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.score.Operator;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.user.User;
import de.imi.mopat.validator.MoPatValidator;
import de.imi.mopat.validator.QuestionValidator;
import de.imi.mopat.validator.QuestionnaireDTOValidator;
import de.unimuenster.imi.org.cdisc.odm.v132.ODM;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMetaDataVersion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.xml.sax.SAXException;

/**
 *
 */
@Controller
public class QuestionnaireController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        QuestionnaireController.class);
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
    private ODMProcessingBean odmReader;

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
        for (Questionnaire questionnaire : allQuestionnaires) {
            // Get the question texts grouped by country from the current
            // question
            SortedMap<String, Map<String, String>> groupedLocalizedDisplayNameByCountry = questionnaire.getLocalizedDisplayNamesGroupedByCountry();
            // And add the grouped-by-country-map to the map for all questions
            // of the current questionnaire
            localizedDisplayNamesForQuestionnaire.put(questionnaire.getId(),
                groupedLocalizedDisplayNameByCountry);
            availableLanguagesInQuestionForQuestionnaires.put(questionnaire.getId(),
                questionnaire.getAvailableQuestionLanguages());
            // Check if the questionnaire has any conditions and set the boolean
            questionnaire.setHasConditions(conditionDao.isConditionTarget(questionnaire));
        }
        model.addAttribute("allQuestionnaires", allQuestionnaires);
        model.addAttribute("availableLanguagesInQuestionForQuestionnaires",
            availableLanguagesInQuestionForQuestionnaires);
        model.addAttribute("localizedDisplayNamesForQuestionnaire",
            localizedDisplayNamesForQuestionnaire);
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
        final HttpServletRequest request, final Model model) {
        QuestionnaireDTO questionnaireDTO = questionnaireService.getQuestionnaireDTOById(questionnaireId)
                .orElse(new QuestionnaireDTO());
        Pair<Boolean, String> canEditWithReason = questionnaireService.canEditQuestionnaireWithReason(questionnaireDTO);

        model.addAttribute("canEdit", canEditWithReason.getLeft());
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
    public String edit(@RequestParam final String action,
        @RequestParam(value = "logoFile", required = false) final MultipartFile logo,
        @ModelAttribute("questionnaireDTO") @Valid final QuestionnaireDTO questionnaireDTO,
        final BindingResult result, final Model model, final HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (action.equalsIgnoreCase("cancel")) {
            return "redirect:/questionnaire/list";
        }

        questionnaireService.processLocalizedText(questionnaireDTO);

        questionnaireService.validateQuestionnaire(questionnaireDTO, logo, result);
        if (result.hasErrors()) {
            return handleValidationErrors(questionnaireDTO, model);
        }

        Long principalId = authService.getAuthenticatedUserId();
        Questionnaire questionnaire = questionnaireService.saveOrUpdateQuestionnaire(questionnaireDTO, logo, principalId);
        Boolean hasQuestionnaireConditions = questionnaireService.hasQuestionnaireConditions(questionnaireDao.getElementById(questionnaireDTO.getId()));
        redirectAttributes.addFlashAttribute("hasQuestionnaireConditions", hasQuestionnaireConditions);
        if (action.equals("saveEditButton")) {
            return "redirect:/question/list?id=" + questionnaire.getId();
        } else {
            return "redirect:/questionnaire/list";
        }
    }

    private String handleValidationErrors(QuestionnaireDTO questionnaireDTO, Model model) {
        if (questionnaireDTO.getId() != null) {
            questionnaireDTO.setLogo(
                    questionnaireDao.getElementById(questionnaireDTO.getId()).getLogo());
        }
        model.addAttribute("questionnaireDTO", questionnaireDTO);
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        return "questionnaire/edit";
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
    public String removeQuestionnaire(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);
        if (questionnaire != null) {
            if (questionnaire.isDeletable()) {
                // Delete the associated conditions
                for (Condition condition : conditionDao.getConditionsByTarget(questionnaire)) {
                    if (condition instanceof SelectAnswerCondition
                        || condition instanceof SliderAnswerThresholdCondition) {
                        // Refresh the trigger so that multiple conditions of
                        // the same trigger will be deleted correctly
                        ConditionTrigger conditionTrigger = answerDao.getElementById(
                            condition.getTrigger().getId());
                        conditionTrigger.removeCondition(condition);
                        answerDao.merge((Answer) conditionTrigger);
                    }
                    conditionDao.remove(condition);
                }

                // Collect all scores in an array list to make sure they will
                // be removed in correct order
                List<Score> scoresToDelete = new ArrayList<>();
                for (Score scoreToDelete : questionnaire.getScores()) {
                    List<Score> dependingScores = scoreToDelete.getDependingScores();
                    // Sort depending scores by amount of their depending
                    // scores to prevent database errors
                    Collections.sort(dependingScores,
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
                    //Update the position of all following bundleQuestionnaires
                    for (BundleQuestionnaire bundleQuestionnaireToChangePosition : bundle.getBundleQuestionnaires()) {
                        if (bundleQuestionnaireToChangePosition.getPosition()
                            > bundleQuestionnaire.getPosition()) {
                            bundleQuestionnaireToChangePosition.setPosition(
                                bundleQuestionnaireToChangePosition.getPosition() - 1);
                        }
                    }
                    bundleDao.merge(bundle);
                }
                questionnaire.removeAllBundleQuestionnaires();
                questionnaireDao.remove(questionnaire);
                model.addAttribute("messageSuccess",
                    messageSource.getMessage("questionnaire.error" + ".deleteQuestionnairePossible",
                        new Object[]{questionnaire.getName()}, LocaleContextHolder.getLocale()));
            } else {
                model.addAttribute("messageFail", messageSource.getMessage(
                    "questionnaire.error" + ".deleteQuestionnaireNotPossible",
                    new Object[]{questionnaire.getName()}, LocaleContextHolder.getLocale()));
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
        @RequestParam(value = "type", required = true) final String type, final Model model) {
        // Get the selected questionnaire
        Questionnaire questionnaire = questionnaireDao.getElementById(id);

        if (questionnaire == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "list");
            return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
        }

        // Get the bytearray of the selected questionnaire in the selected type
        MetadataExporter exporter = MetadataExporterFactory.getMetadataExporter(
            MetadataFormat.valueOf(type));

        for (Question question : questionnaire.getQuestions()) {
            question.setHasConditionsAsTarget(conditionDao.isConditionTarget(question));
        }
        byte[] data = exporter.export(questionnaire, messageSource, configurationDao,
            configurationGroupDao, exportTemplateDao, questionnaireDao, questionDao, scoreDao);

        // Create a windows compliant path/filename and return the download
        Path path = Paths.get(
            questionnaire.getName().replaceAll("[\\\\/:;*?\"<>|]", "").replaceAll(" ", "_") + "_"
                + type + MetadataFormat.valueOf(type).getFileExtension());
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + path.getFileName().toString()).contentLength(data.length)
            .body(resource);
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
        return "questionnaire/import/upload";
    }

    /**
     * Controls the HTTP requests for the URL
     * <i>questionnaire/import/upload</i>. Imports a given file into MoPat.
     *
     * @param file               The file which should be imported.
     * @param url                The server url to the questionnaire for import.
     * @param questionnaire      The current {@link Questionnaire} object.
     * @param request            The current request.
     * @param result             The result, which holds errors for the view.
     * @param model              The model, which holds the information for the view.
     * @param redirectAttributes Stores the information for a redirect scenario.
     * @return Redirect to the <i>questionnaire/import/result</i> website.
     */
    @RequestMapping(value = "/questionnaire/import/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String postImportUpload(
        @RequestParam(value = "file", required = true) final MultipartFile file,
        @RequestParam(value = "url", required = false) final String url,
        @ModelAttribute("questionnaire") Questionnaire questionnaire,
        final HttpServletRequest request, final BindingResult result, final Model model,
        final RedirectAttributes redirectAttributes) {
        // Get the current locale
        Locale locale = LocaleContextHolder.getLocale();
        // Flag for import errors
        boolean importError = false;

        // Build document from a xml file and check in which standard the
        // file is formatted
        ExportTemplateType exportTemplateType = null;

        // If url is null or empty check the file type
        if ((url == null || url.trim().isEmpty()) && !file.getOriginalFilename()
            .contains(".json")) {
            model.addAttribute("fileUpload", true);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = null;
            Document document = null;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
                document = documentBuilder.parse(file.getInputStream());

                //Check the nodes if it corresponds to a standards specification
                NodeList nodes = document.getElementsByTagName("ODM");
                if (nodes == null || nodes.getLength() < 1) {
                    //Try to also use our custom format odm:ODM
                    nodes = document.getElementsByTagName("odm:ODM");
                }

                if (nodes != null && nodes.getLength() > 0) {
                    exportTemplateType = ExportTemplateType.ODM;
                }

                nodes = document.getElementsByTagName("Questionnaire");
                if (nodes != null && nodes.getLength() > 0
                    && exportTemplateType != ExportTemplateType.ODM) {
                    exportTemplateType = ExportTemplateType.FHIR;
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                LOGGER.info("ERROR while getting the ExportTemplateType: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("failure",
                    messageSource.getMessage("import.error.fileNotSupported", new Object[]{},
                        LocaleContextHolder.getLocale()));
                return "redirect:/questionnaire/import/upload";
            }
            //otherwise it's always fhir import
        } else if (file.getOriginalFilename().contains(".json")) {
            try {
                model.addAttribute("fileUpload", true);
                ObjectMapper mapper = new ObjectMapper();
                JsonQuestionnaireDTO jsonQuestionnaireDTO = mapper.readValue(file.getInputStream(),
                    JsonQuestionnaireDTO.class);
                questionnaire = jsonQuestionnaireDTO.convertToQuestionnaire();
                User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
                questionnaire.setChangedBy(currentUser.getId());
                // Collect all questions and answers in a map to access those
                // ones who are target and trigger of a condition easily
                Map<Long, Question> questions = new HashMap<>();
                Map<Long, Answer> answers = new HashMap<>();

                // Convert all jsonQuestionDTOs and all jsonAnswerDTOs to
                // their database model counterparts and collect them in a
                // map for conversion of conditions
                for (Long questionId : jsonQuestionnaireDTO.getQuestionDTOs().keySet()) {
                    JsonQuestionDTO jsonQuestionDTO = jsonQuestionnaireDTO.getQuestionDTOs()
                        .get(questionId);
                    Question question = jsonQuestionDTO.convertToQuestion();
                    questions.put(questionId, question);
                    // If the question is of type multiple choice or drop
                    // down we have to make sure that the freetext answer is
                    // added as the last answer
                    if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                        || question.getQuestionType() == QuestionType.DROP_DOWN) {
                        Long freetextAnswerId = null;
                        for (Long answerId : jsonQuestionDTO.getAnswers().keySet()) {
                            JsonAnswerDTO jsonAnswerDTO = jsonQuestionDTO.getAnswers()
                                .get(answerId);
                            // If the current answer has at least one
                            // localized label it is a select answer
                            if (jsonAnswerDTO.getLocalizedLabel() != null
                                && !jsonAnswerDTO.getLocalizedLabel().isEmpty()) {
                                // Add this direclty to the list of answers
                                answers.put(answerId, jsonAnswerDTO.convertToAnswer(question));
                            } else {
                                // Otherwise it is a freetext answer and we
                                // have to save the answer Id
                                freetextAnswerId = answerId;
                            }
                        }
                        // If any answer Id of a freetext answer was saved,
                        // add this answer as the last one of this question
                        if (freetextAnswerId != null) {
                            JsonAnswerDTO jsonAnswerDTO = jsonQuestionDTO.getAnswers()
                                .get(freetextAnswerId);
                            answers.put(freetextAnswerId, jsonAnswerDTO.convertToAnswer(question));
                        }
                    } else {
                        // For all other questiontypes just convert the
                        // answers and add them
                        for (Long answerId : jsonQuestionDTO.getAnswers().keySet()) {
                            JsonAnswerDTO jsonAnswerDTO = jsonQuestionDTO.getAnswers()
                                .get(answerId);
                            answers.put(answerId, jsonAnswerDTO.convertToAnswer(question));
                        }
                    }

                }

                questionnaire.addQuestions(questions.values());

                Map<JsonAnswerDTO, JsonQuestionDTO> imageAnswerQuestions = new HashMap<>();
                // Convert all jsonConditionDTOs to their database model
                // counterparts
                for (JsonQuestionDTO jsonQuestionDTO : jsonQuestionnaireDTO.getQuestionDTOs()
                    .values()) {
                    for (JsonAnswerDTO jsonAnswerDTO : jsonQuestionDTO.getAnswers().values()) {
                        if (jsonQuestionDTO.getQuestionType() == QuestionType.IMAGE) {
                            imageAnswerQuestions.put(jsonAnswerDTO, jsonQuestionDTO);
                        }
                        // Allocate the trigger and target objects (question
                        // and answers) to the conditions
                        for (JsonConditionDTO jsonConditionDTO : jsonAnswerDTO.getConditions()) {
                            Condition condition = jsonConditionDTO.convertToCondition();
                            ConditionTrigger trigger = answers.get(jsonConditionDTO.getTriggerId());
                            trigger.addCondition(condition);
                            condition.setTrigger(trigger);
                            if (jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat.model" + ".Question")) {
                                condition.setTarget(questions.get(jsonConditionDTO.getTargetId()));
                            } else if (jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat" + ".model" + ".SelectAnswer")
                                || jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat.model.ImageAnswer")
                                || jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat.model.SliderAnswer")
                                || jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat.model.SliderFreetextAnswer")
                                || jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat.model.DateAnswer")
                                || jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat.model.FreetextAnswer")
                                || jsonConditionDTO.getTargetClass()
                                .equals("de.imi.mopat.model.NumberInputAnswer")) {
                                condition.setTarget(answers.get(jsonConditionDTO.getTargetId()));
                                condition.setTargetAnswerQuestion(
                                    questions.get(jsonConditionDTO.getTargetAnswerQuestionId()));
                            }
                        }
                    }
                }

                // Get all operators to allocate them to the expressions of
                // scores
                Map<Long, Operator> operators = new HashMap<>();
                for (Operator operator : operatorDao.getAllElements()) {
                    operators.put(operator.getId(), operator);
                }
                // Collect all scoreIds to allocate them to all
                // unaryExpressions, whose operator is valueOfScore
                Map<Long, UnaryExpression> scoreIdExpressions = new HashMap<>();
                // Collect all scores to easily allocate the upper
                // valueOfScore unaryExpressions
                Map<Long, Score> scores = new HashMap<>();
                for (JsonScoreDTO jsonScoreDTO : jsonQuestionnaireDTO.getScoreDTOs().values()) {
                    // Convert the score and collect it
                    Score score = jsonScoreDTO.convertToScore(operators, questions,
                        scoreIdExpressions);
                    scores.put(jsonScoreDTO.getId(), score);
                    questionnaire.addScore(score);
                }

                // Allocate the unaryExpressions containing valueOfScore
                // operator
                for (Map.Entry<Long, UnaryExpression> entry : scoreIdExpressions.entrySet()) {
                    scoreIdExpressions.get(entry.getKey()).setScore(scores.get(entry.getKey()));
                }

                // Add timestamp to the questionnaire's name if it's used
                // already
                if (!questionnaireDao.isQuestionnaireNameUnique(questionnaire.getName(), 0L)) {
                    questionnaire.setName(
                        questionnaire.getName() + " " + new Timestamp(new Date().getTime()));
                }

                questionnaireDao.merge(questionnaire);

                //Loop through all persisted questions to get the
                // imageAnswers and save the images
                for (Question question : questionnaire.getQuestions()) {
                    if (question.getQuestionType() == QuestionType.IMAGE) {
                        ImageAnswer answer = (ImageAnswer) question.getAnswers().get(0);
                        for (JsonAnswerDTO answerDTO : imageAnswerQuestions.keySet()) {
                            if (answer.getImagePath().equals(answerDTO.getImagePath())) {
                                try {
                                    String imageBase64 = answerDTO.getImageBase64();
                                    String imagePath = (configurationDao.getImageUploadPath()
                                        + "/questionnaire/" + questionnaire.getId());
                                    String fileName = "question" + question.getId() + "."
                                        + StringUtilities.getMimeTypeFromBase64String(imageBase64);
                                    answer.setImagePath(questionnaire.getId() + "/" + fileName);
                                    StringUtilities.convertAndWriteBase64StringToImage(
                                        answerDTO.getImageBase64(), imagePath, fileName);
                                } catch (Exception e) {
                                    LOGGER.info(
                                        "Converting image failed. " + "Following " + "error "
                                            + "occurred: {}", e.getMessage());
                                }
                            }
                        }
                    }
                }

                if (jsonQuestionnaireDTO.getLogoBase64() != null) {
                    try {
                        String logoBase64 = jsonQuestionnaireDTO.getLogoBase64();
                        String imagePath = (configurationDao.getImageUploadPath()
                            + "/questionnaire/" + questionnaire.getId());
                        String fileName = Constants.LOGO_PROPERTY + "." + logoBase64.substring(
                            "data:image/".length(), logoBase64.lastIndexOf(";base64,"));
                        questionnaire.setLogo(fileName);
                        StringUtilities.convertAndWriteBase64StringToImage(
                            jsonQuestionnaireDTO.getLogoBase64(), imagePath, fileName);
                    } catch (IOException e) {
                        LOGGER.info("Converting logo failed. Following error " + "occurred: {}",
                            e.getMessage());
                    }
                }

                questionnaireDao.merge(questionnaire);
            } catch (IOException e) {
                LOGGER.info("ERROR: Importing json formatted MoPat questionnaire "
                    + "failed. The following error occurred: {}", e.getLocalizedMessage());
                redirectAttributes.addFlashAttribute("failure",
                    messageSource.getMessage("import.error.fileNotSupported", new Object[]{},
                        LocaleContextHolder.getLocale()));
                return "redirect:/questionnaire/import/upload";
            }
            return "redirect:/questionnaire/fill?id=" + questionnaire.getId();
        } else {
            model.addAttribute("fileUpload", false);
            exportTemplateType = ExportTemplateType.FHIR;
        }

        if (exportTemplateType == null) {
            redirectAttributes.addFlashAttribute("failure",
                messageSource.getMessage("import.error.fileNotSupported", new Object[]{},
                    LocaleContextHolder.getLocale()));
            return "redirect:/questionnaire/import/upload";
        }

        if (exportTemplateType.equals(ExportTemplateType.ODM)) {
            // [bt] Approach:
            // Check if the file is null or is greater than zero
            // Look at the file ending
            // Depending on that, choose the parser
            // if it's XML, convert it to String
            // and then let JAXB convert it to Java objects
            if (file != null && file.getSize() != 0) {
                LOGGER.debug("File was not null and size is greater than 0");
                String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
                if (fileExtension.equalsIgnoreCase("xml")) {
                    LOGGER.debug("File extension discovered: xml");
                    try {
                        ODM importedODM = odmReader.unmarshal(file.getInputStream());
                        List<ODMcomplexTypeDefinitionStudy> studyList = importedODM.getStudy();
                        if (studyList == null || studyList.isEmpty()) {
                            LOGGER.debug("The imported ODM did not contain "
                                + "any Study elements. Will reject" + " it.");
                            importError = true;
                            redirectAttributes.addFlashAttribute("failure",
                                messageSource.getMessage(
                                    "import.odm.v132" + ".content" + ".noStudy", new Object[]{},
                                    LocaleContextHolder.getLocale()));
                        } else {
                            LOGGER.debug("At least one Study element in the "
                                + "imported ODM. Will take the " + "first one (1. implementation "
                                + "version).");
                            ODMcomplexTypeDefinitionStudy study = studyList.get(0);
                            List<ODMcomplexTypeDefinitionMetaDataVersion> metaDataVersionList = study.getMetaDataVersion();
                            if (metaDataVersionList == null || metaDataVersionList.isEmpty()) {
                                LOGGER.debug(
                                    "The imported ODM, first Study " + "element, did not contain "
                                        + "any MetaDataVersion " + "elements. Will reject it.");
                                importError = true;
                                redirectAttributes.addFlashAttribute("failure",
                                    messageSource.getMessage(
                                        "import.odm.v132" + ".content" + ".noMetaDataVersion",
                                        new Object[]{}, LocaleContextHolder.getLocale()));
                            } else {
                                LOGGER.debug(
                                    "At least one MetaDataVersion " + "element in the imported "
                                        + "ODM, first Study element. "
                                        + "Will take the first one (1."
                                        + " implementation version).");
                                ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion = metaDataVersionList.get(
                                    0);
                                List<ODMcomplexTypeDefinitionFormDef> formDefList = metaDataVersion.getFormDef();
                                if (formDefList == null || formDefList.isEmpty()) {
                                    LOGGER.debug(
                                        "The imported ODM, first " + "Study element, first "
                                            + "MetaDataVersion, did " + "not contain any FormDef"
                                            + " elements. Will reject " + "it.");
                                    importError = true;
                                    redirectAttributes.addFlashAttribute("failure",
                                        messageSource.getMessage(
                                            "import.odm" + ".v132" + ".content" + ".noFormDef",
                                            new Object[]{}, LocaleContextHolder.getLocale()));
                                } else {
                                    LOGGER.debug(
                                        "At least one FormDef " + "element in the " + "imported"
                                            + " ODM, " + "first Study " + "element, first "
                                            + "MetaDataVersion. " + "Will " + "take the "
                                            + "first one (1. " + "implementation " + "version)"
                                            + ".");
                                    ODMcomplexTypeDefinitionFormDef formDef = formDefList.get(0);
                                    Authentication authentication = SecurityContextHolder.getContext()
                                        .getAuthentication();
                                    User principal = (User) authentication.getPrincipal();
                                    Long changedBy = principal.getId();
                                    List<ExportTemplate> exportTemplates = ExportTemplate.createExportTemplates(
                                        "Automatically Generated " + "Exporttemplate",
                                        ExportTemplateType.ODM, file, configurationGroupDao,
                                        exportTemplateDao);

                                    // Convert the ODM into questionnaire
                                    // including the export templates
                                    ImportQuestionnaireResult odmQuestionnaireResult = ODMv132ToMoPatConverter.convertToQuestionnaire(
                                        file, formDef, changedBy, metaDataVersion, exportTemplates,
                                        messageSource);
                                    questionnaire = odmQuestionnaireResult.getQuestionnaire();

                                    // If the questionnaire name is already
                                    // taken within MoPat
                                    if (!questionnaireDao.isQuestionnaireNameUnique(
                                        questionnaire.getName(), 0L)) {
                                        // Add the current timestamp to the
                                        // questionnaire name
                                        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                                            DateFormat.LONG, DateFormat.LONG, locale);
                                        Calendar calendar = Calendar.getInstance();
                                        questionnaire.setName(
                                            questionnaire.getName() + " " + dateFormat.format(
                                                calendar.getTime()));
                                    }
                                    questionnaireDao.merge(questionnaire);
                                    for (ExportTemplate exportTemplate : exportTemplates) {
                                        exportTemplate.setQuestionnaire(questionnaire);
                                        exportTemplate.setName(questionnaire.getName());
                                        questionnaire.addExportTemplate(exportTemplate);

                                        LOGGER.debug(
                                            "This is the questionnaire " + "that " + "has been "
                                                + "imported: {}", questionnaire);

                                        // Replace umlauts and whitespace
                                        String filename = stringUtilityHelper.replaceGermanUmlauts(
                                            file.getOriginalFilename());
                                        String uploadFilename =
                                            exportTemplate.getId() + "_" + filename;

                                        try {
                                            String objectStoragePath = configurationDao.getObjectStoragePath();
                                            // Save uploaded file and update
                                            // xml filename in template
                                            String contextPath = objectStoragePath
                                                + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
                                            File uploadDir = new File(contextPath);
                                            if (!uploadDir.isDirectory()) {
                                                uploadDir.mkdirs();
                                            }
                                            FileUtils.writeByteArrayToFile(
                                                new File(contextPath, uploadFilename),
                                                IOUtils.toByteArray(file.getInputStream()));
                                            exportTemplate.setFilename(uploadFilename);
                                        } catch (IOException e) {
                                            // Delete export template on error
                                            LOGGER.error(
                                                "error while uploading a " + "new " + "export "
                                                    + "template {}", e);
                                            exportTemplateDao.remove(exportTemplate);
                                        }
                                        exportTemplateDao.merge(exportTemplate);
                                    }
                                    questionnaireDao.merge(questionnaire);
                                    model.addAttribute("importQuestionnaireResult",
                                        odmQuestionnaireResult);
                                }
                            }
                        }
                        LOGGER.debug("This is the imported ODM: {}", importedODM);
                    } catch (Exception e) {
                        LOGGER.error("An error occured during importing of ODM: {}", e);
                        redirectAttributes.addFlashAttribute("failure",
                            messageSource.getMessage("questionnaire.import" + ".failure",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                        return "redirect:/questionnaire/import/upload";
                    }
                } else {
                    LOGGER.debug("File hat some other extension: {}", fileExtension);
                    importError = true;
                    redirectAttributes.addFlashAttribute("failure",
                        messageSource.getMessage("import.odm.v132.content" + ".noXML",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
            } else {
                importError = true;
                redirectAttributes.addFlashAttribute("failure",
                    messageSource.getMessage("import.odm.v132.content" + ".nullOrSizeZero",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        } else if (exportTemplateType.equals(ExportTemplateType.FHIR)) {

            // Create list of uploadFiles to collect ExportTemplate files for
            // each configured export configuration group.
            List<File> uploadFiles = new ArrayList<>();
            // In case of import fails, collect all ExportTemplate files that
            // has been created to delete those ones.
            List<File> deletableFiles = new ArrayList<>();
            List<ExportTemplate> exportTemplates = new ArrayList<>();
            FHIRHelper.setParserValidator(new LenientErrorHandler());
            try {
                String objectStoragePath = configurationDao.getObjectStoragePath();
                // Save uploaded file and update xml filename in template
                String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
                String filename = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "HH:mm:ss " + "dd" + ".MM" + ".yyyy");
                org.hl7.fhir.dstu3.model.Questionnaire fhirQuestionnaire = new org.hl7.fhir.dstu3.model.Questionnaire();

                if (file != null && !file.isEmpty() && file.getSize() > 0) {
                    filename = stringUtilityHelper.replaceGermanUmlauts(file.getOriginalFilename());
                    String validationSchemaFilePath =
                        request.getSession().getServletContext().getRealPath("") + "/"
                            + Constants.FHIR_VALIDATION_SCHEMA_SUB_DIRECTORY;
                    // Validate the questionnaire against a xml schema
                    // definition to check if it's conform with fhir
                    // specification
                    FHIRHelper.validateFileAgainstSchema(file, validationSchemaFilePath,
                        Constants.SCHEMA_QUESTIONNAIRE_FILE, result, messageSource);
                    if (result.hasErrors()) {
                        return getImportUpload(model);
                    }
                    exportTemplates = ExportTemplate.createExportTemplates(
                        "Automatically Generated Exporttemplate", ExportTemplateType.FHIR, file,
                        configurationGroupDao, exportTemplateDao);
                    fhirQuestionnaire = (org.hl7.fhir.dstu3.model.Questionnaire) FHIRHelper.parseResourceFromFile(
                        file.getInputStream());

                } else if (url != null && !url.trim().isEmpty()) {

                    // Check if the url contains "/" otherwise it cannot be
                    // resolved
                    if (!url.contains("/")) {
                        result.reject("import.error.invalidUrl", new Object[]{},
                            "Input url is not valid");
                        return getImportUpload(model);
                    }

                    // Get the serverBase adress to create connection to the
                    // server
                    String serverBase = url.substring(0,
                        url.substring(0, url.lastIndexOf("/")).lastIndexOf("/"));
                    IGenericClient client = FHIRHelper.getContext()
                        .newRestfulGenericClient(serverBase);
                    fhirQuestionnaire = client.read()
                        .resource(org.hl7.fhir.dstu3.model.Questionnaire.class).withUrl(url)
                        .execute();
                    fhirQuestionnaire.setUrl(
                        url.substring(url.indexOf("Questionnaire")));

                    if (fhirQuestionnaire.getTitle() == null || fhirQuestionnaire.getTitle().trim()
                        .isEmpty()) {
                        filename = "Questionnaire default title.xml";
                        fhirQuestionnaire.setTitle(
                            "Questionnaire default " + "title " + dateFormat.format(new Date()));
                    } else {
                        filename = fhirQuestionnaire.getTitle() + ".xml";
                    }
                    exportTemplates = ExportTemplate.createExportTemplates(
                        "Automatically Generated Exporttemplate", ExportTemplateType.FHIR, null,
                        configurationGroupDao, exportTemplateDao);
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
                ImportQuestionnaireResult fhirQuestionnaireResult = FHIRToMoPatConverter.convertFHIRQuestionnaireToMoPatQuestionnaire(
                    fhirQuestionnaire, exportTemplates, messageSource);
                questionnaire = fhirQuestionnaireResult.getQuestionnaire();

                // Write the questionnaire in each upload file
                for (File uploadFile : uploadFiles) {
                    uploadFile.createNewFile();
                    FHIRHelper.writeResourceToFile(fhirQuestionnaire, uploadFile);
                }

                // Just append the current date if the questionnaire's name
                // is already in use
                if (!questionnaireDao.isQuestionnaireNameUnique(questionnaire.getName(), null)) {
                    questionnaire.setName(
                        questionnaire.getName() + " " + dateFormat.format(new Date()));
                }

                // Merge questionnaire
                questionnaireDao.merge(questionnaire);

                // Merge the export templates
                for (ExportTemplate exportTemplate : exportTemplates) {
                    exportTemplate.setQuestionnaire(questionnaire);
                    questionnaire.addExportTemplate(exportTemplate);
                    exportTemplateDao.merge(exportTemplate);
                }

                // Merge questionnaire second time
                questionnaireDao.merge(questionnaire);

                model.addAttribute("importQuestionnaireResult", fhirQuestionnaireResult);
            } catch (IOException | IllegalStateException | ConfigurationException |
                     ResourceNotFoundException | FhirClientConnectionException e) {
                for (ExportTemplate template : exportTemplates) {
                    exportTemplateDao.remove(template);
                }

                for (File fileToDelete : deletableFiles) {
                    fileToDelete.delete();
                }

                result.reject("import.fhir.error.message", new Object[]{e.getLocalizedMessage()},
                    "The following error occurred: " + e.getMessage());
                return getImportUpload(model);
            }
        }
        // TODO go on implementing here
        LOGGER.debug("Leaving public String postImportUpload(MultipartFile, "
            + "Questionnaire, BindingResult, HttpServletRequest, " + "Model)");
        // Add current language to the model
        model.addAttribute("currentLanguage", locale.toString());
        if (importError) {
            return "redirect:/questionnaire/import/upload";
        }
        return "questionnaire/import/result";
    }
}
