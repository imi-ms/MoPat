package de.imi.mopat.io.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import de.imi.mopat.controller.ExportMappingController;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.ExportRuleDTO;
import de.imi.mopat.model.dto.ExportRuleFormatDTO;
import de.imi.mopat.model.dto.ExportRulesDTO;
import de.imi.mopat.model.dto.export.JsonCompleteQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonExportRuleAnswerDTO;
import de.imi.mopat.model.dto.export.JsonExportRuleFormatDTO;
import de.imi.mopat.model.dto.export.JsonExportTemplateDTO;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
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
import org.apache.commons.io.FilenameUtils;
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

    public Questionnaire importQuestionnaire(MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        JsonQuestionnaireDTO jsonQuestionnaireDTO = mapper.readValue(
            file.getInputStream(), JsonQuestionnaireDTO.class);
        Map<Long, Question> questions = new HashMap<>();
        Map<Long, Answer> answers = new HashMap<>(); //old id <> new answer object with uuid
        Map<Long, Score> scores = new HashMap<>();

        Questionnaire questionnaire = createQuestionnaire(jsonQuestionnaireDTO, questions,
            answers, scores);

        if(jsonQuestionnaireDTO instanceof JsonCompleteQuestionnaireDTO jsonCompleteQuestionnaireDTO){
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

                exportMappingController.updateExportMapping(exportRulesDTO);
            }

        }

    }

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

        for (JsonExportRuleAnswerDTO jsonRuleDTO : exportTemplateDTO.getExportRuleDTOs().values()) {
            // Find the new answer ID using UUID

//            oldid <> new object with uuid

            Long oldAnswerId = jsonRuleDTO.getAnswerId();
            Long newAnswerId = answers.get(oldAnswerId).getId();
            if (newAnswerId == null) {
                continue;
            }

            ExportRuleDTO ruleDTO = new ExportRuleDTO();
            ruleDTO.setAnswerId(newAnswerId);
            ruleDTO.setExportField(Collections.singletonList(jsonRuleDTO.getExportField()));
            ruleDTO.setUseFreetextValue(jsonRuleDTO.getUseFreetextValue());

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
}