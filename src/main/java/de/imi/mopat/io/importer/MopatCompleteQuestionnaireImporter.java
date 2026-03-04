package de.imi.mopat.io.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.controller.ExportMappingController;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.ExportRule;
import de.imi.mopat.model.ExportRuleAnswer;
import de.imi.mopat.model.ExportRuleEncounter;
import de.imi.mopat.model.ExportRuleFormat;
import de.imi.mopat.model.ExportRuleQuestion;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.ExportRuleDTO;
import de.imi.mopat.model.dto.ExportRuleFormatDTO;
import de.imi.mopat.model.dto.ExportRulesDTO;
import de.imi.mopat.model.dto.export.JsonCompleteQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonExportRuleDTO;
import de.imi.mopat.model.dto.export.JsonExportRuleFormatDTO;
import de.imi.mopat.model.dto.export.JsonExportTemplateDTO;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
import de.imi.mopat.model.enumeration.ExportRuleType;
import de.imi.mopat.model.score.Score;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MopatCompleteQuestionnaireImporter extends MoPatQuestionnaireImporter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        MopatCompleteQuestionnaireImporter.class);


    @Autowired
    private ExportMappingController exportMappingController;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private ExportTemplateDao exportTemplateDao;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ScoreDao scoreDao;

    /**
     * Imports a {@link Questionnaire} from a JSON {@link MultipartFile}.
     * Also imports export templates/mappings when the JSON is a {@link JsonCompleteQuestionnaireDTO}.
     *
     * @param file uploaded JSON file
     * @return imported questionnaire
     * @throws IOException on read/parse errors
     */
    public Questionnaire importQuestionnaire(MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        JsonQuestionnaireDTO jsonQuestionnaireDTO = mapper.readValue(
            file.getInputStream(), JsonQuestionnaireDTO.class);
        Map<Long, Question> questions = new HashMap<>();
        Map<Long, Answer> answers = new HashMap<>(); //old id <> new answer object with uuid
        Map<Long, Score> scores = new HashMap<>();

        Questionnaire questionnaire = createQuestionnaire(jsonQuestionnaireDTO, questions,
            answers, scores);

        if (jsonQuestionnaireDTO instanceof JsonCompleteQuestionnaireDTO jsonCompleteQuestionnaireDTO) {
            // Import export_templates and their mappings
            if (jsonCompleteQuestionnaireDTO.getExportDTOs() != null) {
                for (JsonExportTemplateDTO exportTemplateDTO : jsonCompleteQuestionnaireDTO.getExportDTOs()
                    .values()) {

                    importExportTemplate(exportTemplateDTO, questionnaire,
                        questions, answers, scores);
                }
            }
        }

        return questionnaire;
    }

    /**
     * Imports a single export template from JSON into the given {@link Questionnaire}.
     * Creates the template(s), uploads the associated template file, and persists export rules/formats
     * with IDs remapped via the provided lookup maps.
     *
     * @param exportTemplateDTO exported template data to import
     * @param questionnaire target questionnaire to attach templates to
     * @param questions map of old question IDs to newly created {@link Question} instances
     * @param answers map of old answer IDs to newly created {@link Answer} instances
     * @param scores map of old score IDs to newly created {@link Score} instances
     */
    private void importExportTemplate(JsonExportTemplateDTO exportTemplateDTO,
        Questionnaire questionnaire,
        Map<Long, Question> questions,
        Map<Long, Answer> answers, //old id <> new answer object with uuid
        Map<Long, Score> scores) {

        List<ExportTemplate> exportTemplates = ExportTemplate.createExportTemplates(
            exportTemplateDTO.getName(),
            exportTemplateDTO.getExportTemplateType(), null, configurationGroupDao,
            exportTemplateDao);

        uploadExportFile(questionnaire, exportTemplates, exportTemplateDTO);

        // Now import the export rules using the updateExportMapping function
        if (exportTemplateDTO.getExportRuleDTOs() != null && !exportTemplateDTO.getExportRuleDTOs()
            .isEmpty()) {

            for (ExportTemplate exportTemplate : exportTemplates) {
                ExportRulesDTO exportRulesDTO = convertToExportRulesDTO(
                    exportTemplateDTO,
                    exportTemplate.getId(),
                    questions,
                    answers,
                    scores
                );

                persistRulesAndFormats(exportRulesDTO, exportTemplate);
            }
        }
    }

    /**
     * Converts a {@link JsonExportTemplateDTO} into an {@link ExportRulesDTO} for persistence.
     * Remaps old question/answer/score IDs to newly created entities and collects optional rule formats.
     *
     * @param exportTemplateDTO source template DTO containing exported rules
     * @param newExportTemplateId ID of the newly created export template
     * @param questions old question ID -> new {@link Question}
     * @param answers old answer ID -> new {@link Answer}
     * @param scores old score ID -> new {@link Score}
     * @return populated {@link ExportRulesDTO} for the given export template
     */
    private ExportRulesDTO convertToExportRulesDTO(JsonExportTemplateDTO exportTemplateDTO,
        Long newExportTemplateId,
        Map<Long, Question> questions,
        Map<Long, Answer> answers, //old id <> new answer object with uuid
        Map<Long, Score> scores) {

        ExportRulesDTO exportRulesDTO = new ExportRulesDTO();
        exportRulesDTO.setExportTemplateId(newExportTemplateId);

        List<ExportRuleDTO> exportRuleDTOs = new ArrayList<>();
        Map<Long, ExportRuleFormatDTO> formatDTOs = new HashMap<>();
        Long tempFormatIdCounter = 0L;

        for (JsonExportRuleDTO jsonRuleDTO : exportTemplateDTO.getExportRuleDTOs().values()) {

            ExportRuleDTO ruleDTO = new ExportRuleDTO();

            matchNewIdsFromOldTemplates(ruleDTO, jsonRuleDTO, questions, answers, scores);

            ruleDTO.setExportField(Collections.singletonList(jsonRuleDTO.getExportField()));
            ruleDTO.setUseFreetextValue(jsonRuleDTO.getUseFreetextValue());
            ruleDTO.setEncounterField(jsonRuleDTO.getEncounterField());
            ruleDTO.setScoreField(jsonRuleDTO.getScoreField());
            ruleDTO.setType(jsonRuleDTO.getType());

            // Handle format if present
            if (jsonRuleDTO.getExportRuleFormat() != null) {
                Long tempFormatId = tempFormatIdCounter++;
                ruleDTO.setTempExportFormatId(tempFormatId);

                ExportRuleFormatDTO formatDTO = new ExportRuleFormatDTO();
                JsonExportRuleFormatDTO jsonFormatDTO = jsonRuleDTO.getExportRuleFormat();

                formatDTO.setDateFormat(jsonFormatDTO.getDateFormat());
                formatDTO.setDecimalDelimiter(jsonFormatDTO.getDecimalDelimiter());
                formatDTO.setDecimalPlaces(
                    jsonFormatDTO.getDecimalPlaces() != null ?
                        jsonFormatDTO.getDecimalPlaces().toString() : null
                );
                formatDTO.setNumberType(jsonFormatDTO.getNumberType());
                formatDTO.setRoundingStrategy(jsonFormatDTO.getRoundingStrategy());

                formatDTOs.put(tempFormatId, formatDTO);
            }

            exportRuleDTOs.add(ruleDTO);
        }

        exportRulesDTO.setExportRules(exportRuleDTOs);
        exportRulesDTO.setExportRuleFormats(formatDTOs);

        return exportRulesDTO;
    }

    /**
     * Helper function that matches the ids from the new questionnaire to the old ids from the json
     * import
     *
     * @param ruleDTO     new ExportRuleDTO to match ids for
     * @param jsonRuleDTO importet JsonExportRuleDTO to match ids from
     * @param questions   to use for matching
     * @param answers     to use for matching
     * @param scores      to use for matching
     */
    private void matchNewIdsFromOldTemplates(
        ExportRuleDTO ruleDTO, JsonExportRuleDTO jsonRuleDTO,
        Map<Long, Question> questions, Map<Long, Answer> answers, Map<Long, Score> scores
    ) {
        Long oldAnswerId = jsonRuleDTO.getAnswerId();
        if (oldAnswerId != null) {
            Long newAnswerId = answers.get(oldAnswerId).getId();
            if (newAnswerId != null) {
                ruleDTO.setAnswerId(newAnswerId);
            }
        }

        Long oldScoreId = jsonRuleDTO.getScoreId();
        if (oldScoreId != null) {
            Long newScoreId = scores.get(oldScoreId).getId();
            if (newScoreId != null) {
                ruleDTO.setScoreId(newScoreId);
            }
        }

        Long oldQuestionId = jsonRuleDTO.getQuestionId();
        if (oldQuestionId != null) {
            Long newQuestionId = questions.get(oldQuestionId).getId();
            if (newQuestionId != null) {
                ruleDTO.setQuestionId(newQuestionId);
            }
        }
    }

    /**
     * Stores the export template file from the given DTO and attaches the created templates to the questionnaire.
     * Decodes the Base64 payload, writes the file to object storage, updates filenames, and persists templates.
     * On I/O errors, attempts to clean up created templates/files.
     *
     * @param questionnaire target questionnaire
     * @param exportTemplates templates to update and persist
     * @param exportTemplateDTO source DTO providing filename and Base64-encoded file contents
     */
    private void uploadExportFile(Questionnaire questionnaire, List<ExportTemplate> exportTemplates,
        JsonExportTemplateDTO exportTemplateDTO) {

        //Create second list to avoid ConcurrentModificationException
        List<ExportTemplate> templates = new ArrayList<>();
        templates.addAll(exportTemplates);

        byte[] fileByteArray = Base64.decodeBase64(exportTemplateDTO.getFileByteArrayEncoded());

        for (ExportTemplate template : templates) {
            template.setOriginalFilename(exportTemplateDTO.getOriginalFilename());

            String newFilename =
                template.getId() + "_imported_" + exportTemplateDTO.getOriginalFilename();
            template.setFilename(newFilename);

            try {

                String objectStoragePath = configurationDao.getObjectStoragePath();
                // Save uploaded file and update xml filename in template
                String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
                File uploadDir = new File(contextPath);
                if (!uploadDir.isDirectory()) {
                    uploadDir.mkdirs();
                }
                File uploadFile = new File(contextPath, newFilename);
                uploadFile.createNewFile();

                FileUtils.writeByteArrayToFile(new File(contextPath, newFilename),
                    fileByteArray);

                template.setQuestionnaire(questionnaire);
                questionnaire.addExportTemplate(template);
                exportTemplateDao.merge(template);
            } catch (IOException e) {
                // delete export template on error
                LOGGER.error("error while uploading a new export template {}", e);
                for (ExportTemplate exportTemplate : exportTemplates) {
                    File exportFile = new File(newFilename);
                    if (exportFile.isFile()) {
                        exportFile.delete();
                    }
                    exportTemplateDao.remove(exportTemplate);
                }
            }

            questionnaireDao.merge(questionnaire);
        }
    }


    /**
     * Persists export rules for the given template and applies optional rule formats.
     * Resolves referenced entities (answer/score/question) by ID, creates the corresponding rule type,
     * attaches it to the template, and finally merges the template.
     *
     * @param exportRulesDTO rules/formats to persist
     * @param exportTemplate target export template
     */
    private void persistRulesAndFormats(
        ExportRulesDTO exportRulesDTO,
        ExportTemplate exportTemplate
    ) {
        for (ExportRuleDTO exportRuleDTO : exportRulesDTO.getExportRules()) {
            for (String mapping : exportRuleDTO.getExportField()) {
                ExportRule newExportRule = null;

                if (exportRuleDTO.getType() == ExportRuleType.ANSWER) {
                    Answer answer = answerDao.getElementById(exportRuleDTO.getAnswerId());
                    if (answer != null) {
                        newExportRule = new ExportRuleAnswer(
                            exportTemplate, mapping,
                            answer
                        );
                    }

                } else if (exportRuleDTO.getType() == ExportRuleType.SCORE) {
                    Score score = scoreDao.getElementById(exportRuleDTO.getScoreId());
                    if (score != null) {
                        newExportRule = new ExportRuleScore(
                            exportTemplate, mapping, score,
                            exportRuleDTO.getScoreField()
                        );
                    }
                } else if (exportRuleDTO.getType() == ExportRuleType.QUESTION) {
                    Question question = questionDao.getElementById(exportRuleDTO.getQuestionId());
                    if (question != null) {
                        newExportRule = new ExportRuleQuestion(
                            exportTemplate, mapping, question
                        );
                    }
                } else if (exportRuleDTO.getType() == ExportRuleType.ENCOUNTER) {
                    newExportRule = new ExportRuleEncounter(
                        exportTemplate, mapping, exportRuleDTO.getEncounterField()
                    );
                }

                if (newExportRule != null) {
                    copyExportRuleFormatOntoExportRule(
                        exportRulesDTO.getExportRuleFormats(), exportRuleDTO, newExportRule
                    );
                    exportTemplate.addExportRule(newExportRule);
                }

            }
        }

        exportTemplateDao.merge(exportTemplate);
    }

    /**
     * Copies the referenced format from the DTO map onto the given export rule, if present.
     *
     * @param exportRuleFormatDTOMap temp format ID -> format DTO
     * @param exportRuleDTO rule DTO referencing an optional temp format ID
     * @param newExportRule target rule to receive the copied format
     */
    private void copyExportRuleFormatOntoExportRule(
        Map<Long, ExportRuleFormatDTO> exportRuleFormatDTOMap,
        ExportRuleDTO exportRuleDTO,
        ExportRule newExportRule
    ) {
        ExportRuleFormatDTO existingExportRuleFormat = exportRuleFormatDTOMap.get(
            exportRuleDTO.getTempExportFormatId()
        );

        if (existingExportRuleFormat != null) {
            ExportRuleFormat newExportRuleFormat = new ExportRuleFormat();
            newExportRuleFormat.setDateFormat(existingExportRuleFormat.getDateFormat());
            newExportRuleFormat.setNumberType(existingExportRuleFormat.getNumberType());
            newExportRuleFormat.setRoundingStrategy(existingExportRuleFormat.getRoundingStrategy());
            newExportRuleFormat.setDecimalDelimiter(existingExportRuleFormat.getDecimalDelimiter());
            try {
                newExportRuleFormat.setDecimalPlaces(
                    Integer.parseInt(existingExportRuleFormat.getDecimalPlaces()));
            } catch (Exception e) {
                //Do Nothing
            }

            newExportRule.setExportRuleFormat(newExportRuleFormat);
        }
    }
}