package de.imi.mopat.helper.model;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.ExportRule;
import de.imi.mopat.model.ExportRuleAnswer;
import de.imi.mopat.model.ExportRuleEncounter;
import de.imi.mopat.model.ExportRuleQuestion;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.dto.export.JsonAnswerDTO;
import de.imi.mopat.model.dto.export.JsonCompleteQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonConditionDTO;
import de.imi.mopat.model.dto.export.JsonExportRuleDTO;
import de.imi.mopat.model.dto.export.JsonExportRuleFormatDTO;
import de.imi.mopat.model.dto.export.JsonExportTemplateDTO;
import de.imi.mopat.model.dto.export.JsonQuestionDTO;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonScoreDTO;
import de.imi.mopat.model.score.Score;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

/**
 * Helper for (de)serializing questionnaires to/from JSON DTOs. Populates export DTOs from domain
 * objects (questions, answers, scores, export templates/rules), including optional Base64-encoded
 * assets loaded from storage.
 */
@Component
public class JSONHelper {

    /**
     * Initializes the given {@link JsonQuestionnaireDTO} from the provided {@link Questionnaire}.
     * Populates questionnaire metadata, embeds the logo as Base64 when available, and adds DTOs for
     * all questions (including their answers) and scores.
     *
     * @param jsonQuestionnaireDTO target DTO to populate
     * @param questionnaire        source questionnaire
     * @param configurationDao     used to resolve storage paths for optional assets (e.g., logo)
     */
    public void initializeJsonQuestionnaireDTO(JsonQuestionnaireDTO jsonQuestionnaireDTO,
        final Questionnaire questionnaire, ConfigurationDao configurationDao) {
        jsonQuestionnaireDTO.setId(questionnaire.getId());
        jsonQuestionnaireDTO.setName(questionnaire.getName());
        jsonQuestionnaireDTO.setDescription(questionnaire.getDescription());
        jsonQuestionnaireDTO.setLocalizedWelcomeText(questionnaire.getLocalizedWelcomeText());
        jsonQuestionnaireDTO.setLocalizedFinalText(questionnaire.getLocalizedFinalText());
        jsonQuestionnaireDTO.setLocalizedDisplayName(questionnaire.getLocalizedDisplayName());

        if (questionnaire.getLogo()
            != null) {
            try {
                jsonQuestionnaireDTO.setLogoBase64(StringUtilities.convertImageToBase64String(
                    (configurationDao.getImageUploadPath()
                        + "/questionnaire/"
                        + questionnaire.getId()
                        + "/"
                        + questionnaire.getLogo()
                    ),
                    questionnaire.getLogo()));
            } catch (Exception e) {
            }
        }

        for (Question question : questionnaire.getQuestions()) {
            JsonQuestionDTO jsonQuestionDTO = new JsonQuestionDTO();
            this.initializeJsonQuestionDTO(jsonQuestionDTO, question, configurationDao);
            jsonQuestionnaireDTO.setQuestionDTO(question.getId(), jsonQuestionDTO);
            jsonQuestionDTO.setJsonQuestionnaireDTO(jsonQuestionnaireDTO);
        }

        for (Score score : questionnaire.getScores()) {
            JsonScoreDTO jsonScoreDTO = new JsonScoreDTO(score);
            jsonQuestionnaireDTO.setScoreDTO(
                score.getId(),
                jsonScoreDTO);
        }
    }

    /**
     * Initializes the given {@link JsonQuestionDTO} from the provided {@link Question}. Copies
     * question metadata and populates answer DTOs for all answers of the question.
     *
     * @param jsonQuestionDTO  target DTO to populate
     * @param question         source question
     * @param configurationDao used for resolving optional answer assets (e.g., images)
     */
    public void initializeJsonQuestionDTO(JsonQuestionDTO jsonQuestionDTO, final Question question,
        ConfigurationDao configurationDao) {
        jsonQuestionDTO.setId(question.getId());
        jsonQuestionDTO.setLocalizedQuestionText(question.getLocalizedQuestionText());
        jsonQuestionDTO.setIsRequired(question.getIsRequired());
        jsonQuestionDTO.setIsEnabled(question.getIsEnabled());
        jsonQuestionDTO.setQuestionType(question.getQuestionType());
        jsonQuestionDTO.setMaxNumberAnswers(question.getMaxNumberAnswers());
        jsonQuestionDTO.setMinNumberAnswers(question.getMinNumberAnswers());
        jsonQuestionDTO.setCodedValueType(question.getCodedValueType());
        jsonQuestionDTO.setPosition(question.getPosition());

        for (Answer answer : question.getAnswers()) {
            JsonAnswerDTO jsonAnswerDTO = new JsonAnswerDTO();
            jsonAnswerDTO = this.initializeJsonAnswerDTO(jsonAnswerDTO, answer, configurationDao);
            jsonQuestionDTO.setAnswers(answer.getId(), jsonAnswerDTO);
            jsonAnswerDTO.setJsonQuestionDTO(jsonQuestionDTO);
        }
    }


    /**
     * Initializes the given {@link JsonAnswerDTO} from the provided {@link Answer}. Copies common
     * fields, exports applicable conditions, and fills type-specific properties (e.g.,
     * select/slider/number/date/image/body-part). Images may be embedded as Base64 when available.
     *
     * @param jsonAnswerDTO    target DTO to populate
     * @param answer           source answer
     * @param configurationDao used to resolve storage paths for optional assets (e.g., images)
     * @return the populated {@link JsonAnswerDTO}
     */
    public JsonAnswerDTO initializeJsonAnswerDTO(JsonAnswerDTO jsonAnswerDTO, Answer answer,
        ConfigurationDao configurationDao) {
        jsonAnswerDTO.setId(answer.getId());
        jsonAnswerDTO.setIsEnabled(answer.getIsEnabled());

        for (Condition condition : answer.getConditions()) {
            if (!condition.getTargetClass()
                .equals("de.imi.mopat.model.Questionnaire")) {
                JsonConditionDTO jsonConditionDTO =
                    new JsonConditionDTO(condition);
                jsonAnswerDTO.addCondition(jsonConditionDTO);
                jsonConditionDTO.setTriggerId(condition.getTrigger()
                    .getId());
                jsonConditionDTO.setTargetId(condition.getTarget()
                    .getId());
            }
        }

        if (answer instanceof SelectAnswer selectAnswer) {
            jsonAnswerDTO.setLocalizedLabel(selectAnswer.getLocalizedLabel());
            jsonAnswerDTO.setValue(selectAnswer.getValue());
            jsonAnswerDTO.setCodedValue(selectAnswer.getCodedValue());
            jsonAnswerDTO.setIsOther(selectAnswer.getIsOther());
        }
        if (answer instanceof SliderAnswer sliderAnswer) {
            jsonAnswerDTO.setMinValue(sliderAnswer.getMinValue());
            jsonAnswerDTO.setMaxValue(sliderAnswer.getMaxValue());
            jsonAnswerDTO.setStepsize(sliderAnswer.getStepsize()
                .toString());
            jsonAnswerDTO.setLocalizedMinimumText(sliderAnswer.getLocalizedMinimumText());
            jsonAnswerDTO.setLocalizedMaximumText(sliderAnswer.getLocalizedMaximumText());
            jsonAnswerDTO.setVertical(sliderAnswer.getVertical());
            jsonAnswerDTO.setShowValueOnButton(sliderAnswer.getShowValueOnButton());
            jsonAnswerDTO.setShowIcons(sliderAnswer.getShowIcons());
            jsonAnswerDTO.setIcons(sliderAnswer.getIcons());

        }
        if (answer instanceof NumberInputAnswer numberInputAnswer) {
            jsonAnswerDTO.setMinValue(numberInputAnswer.getMinValue());
            jsonAnswerDTO.setMaxValue(numberInputAnswer.getMaxValue());
            if (numberInputAnswer.getStepsize()
                != null) {
                jsonAnswerDTO.setStepsize(numberInputAnswer.getStepsize()
                    .toString());
            }
        }
        if (answer instanceof DateAnswer dateAnswer) {
            if (dateAnswer.getStartDate()
                != null) {
                jsonAnswerDTO.setStartDate(Constants.DATE_FORMAT.format(dateAnswer.getStartDate()));
            }
            if (dateAnswer.getEndDate()
                != null) {
                jsonAnswerDTO.setEndDate(Constants.DATE_FORMAT.format(dateAnswer.getEndDate()));
            }
        }
        if (answer instanceof SliderFreetextAnswer sliderFreetextAnswer) {
            jsonAnswerDTO.setLocalizedFreetextLabel(
                sliderFreetextAnswer.getLocalizedFreetextLabel());
            jsonAnswerDTO.setLocalizedMaximumText(sliderFreetextAnswer.getLocalizedMaximumText());
            jsonAnswerDTO.setLocalizedMinimumText(sliderFreetextAnswer.getLocalizedMinimumText());
            jsonAnswerDTO.setMaxValue(sliderFreetextAnswer.getMaxValue());
            jsonAnswerDTO.setMinValue(sliderFreetextAnswer.getMinValue());
            jsonAnswerDTO.setStepsize(sliderFreetextAnswer.getStepsize()
                .toString());
        }
        if (answer instanceof ImageAnswer imageAnswer) {
            jsonAnswerDTO.setImagePath(imageAnswer.getImagePath());
            // Try to load the image from the disk as a BufferedImage and get
            // the Base64 representation
            try {
                String imagePath = (configurationDao.getImageUploadPath() + "/question/"
                    + jsonAnswerDTO.getImagePath());
                String fileName = jsonAnswerDTO.getImagePath()
                    .substring(imageAnswer.getImagePath()
                        .lastIndexOf("/"));
                jsonAnswerDTO.setImageBase64(StringUtilities.convertImageToBase64String(
                    imagePath,
                    fileName));
            } catch (IOException e) {
            }
        }
        if (answer instanceof BodyPartAnswer bodyPartAnswer) {
            jsonAnswerDTO.setBodyPart(bodyPartAnswer.getBodyPart());
        }
        return jsonAnswerDTO;
    }

    /**
     * Adds export templates (including template files and export rules) from the given
     * {@link Questionnaire} to the provided {@link JsonCompleteQuestionnaireDTO}.
     *
     * @param jsonCompleteQuestionnaireDTO target DTO to populate
     * @param questionnaire                source questionnaire
     * @param configurationDao             used to resolve the export template file location for
     *                                     Base64 embedding
     */
    public void initializeJsonExportTemplateDTO(
        JsonCompleteQuestionnaireDTO jsonCompleteQuestionnaireDTO,
        final Questionnaire questionnaire, ConfigurationDao configurationDao) {

        for (ExportTemplate exportTemplate : questionnaire.getExportTemplates()) {

            JsonExportTemplateDTO jsonExportTemplateDTO = new JsonExportTemplateDTO();
            jsonExportTemplateDTO.setId(exportTemplate.getId());
            jsonExportTemplateDTO.setUuid(exportTemplate.getUuid());
            jsonExportTemplateDTO.setName(exportTemplate.getName());
            jsonExportTemplateDTO.setFilename(exportTemplate.getFilename());
            jsonExportTemplateDTO.setOriginalFilename(exportTemplate.getOriginalFilename());
            jsonExportTemplateDTO.setExportTemplateType(exportTemplate.getExportTemplateType());
            jsonExportTemplateDTO.setConfigurationGroupLabelCode(
                exportTemplate.getConfigurationGroup().getLabelMessageCode());

            try {
                // construct the context path based on the object storage path
                // and export template directory
                // and read the xml file from this directory
                String objectStoragePath = configurationDao.getObjectStoragePath();
                String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
                File file = new File(contextPath, exportTemplate.getFilename());
                FileInputStream inputStream = new FileInputStream(file);

                jsonExportTemplateDTO.setFileByteArrayEncoded(
                    Base64.encodeBase64(IOUtils.toByteArray(inputStream)));

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            for (ExportRule exportRule : exportTemplate.getExportRules()) {
                JsonExportRuleDTO jsonExportRuleDTO = new JsonExportRuleDTO();

                jsonExportRuleDTO.setId(exportRule.getId());
                jsonExportRuleDTO.setUuid(exportRule.getUuid());
                jsonExportRuleDTO.setExportField(exportRule.getExportField());
                jsonExportRuleDTO.setType(exportRule.getType());

                if (exportRule.getExportRuleFormat() != null) {
                    jsonExportRuleDTO.setExportRuleFormat(getJsonExportRuleFormatDTO(exportRule));
                }

                if (exportRule instanceof ExportRuleAnswer exportRuleAnswer) {
                    jsonExportRuleDTO.setAnswerId(exportRuleAnswer.getAnswer().getId());
                    jsonExportRuleDTO.setUseFreetextValue(exportRuleAnswer.getUseFreetextValue());
                } else if (exportRule instanceof ExportRuleEncounter exportRuleEncounter) {
                    jsonExportRuleDTO.setEncounterField(exportRuleEncounter.getEncounterField());
                } else if (exportRule instanceof ExportRuleScore exportRuleScore) {
                    jsonExportRuleDTO.setScoreId(exportRuleScore.getScore().getId());
                    jsonExportRuleDTO.setScoreField(exportRuleScore.getScoreField());
                } else if (exportRule instanceof ExportRuleQuestion exportRuleQuestion) {
                    jsonExportRuleDTO.setQuestionId(exportRuleQuestion.getQuestion().getId());
                }

                jsonExportTemplateDTO.addExportRuleDTOs(exportRule.getId(), jsonExportRuleDTO);
            }

            jsonCompleteQuestionnaireDTO.addExportDTOs(exportTemplate.getId(),
                jsonExportTemplateDTO);
        }
    }

    /**
     * Builds a {@link JsonExportRuleFormatDTO} from the format associated with the given {@link ExportRule}.
     *
     * @param exportRule source rule providing the format
     * @return DTO containing the export rule format settings
     */
    private JsonExportRuleFormatDTO getJsonExportRuleFormatDTO(ExportRule exportRule) {
        JsonExportRuleFormatDTO jsonExportRuleFormatDTO = new JsonExportRuleFormatDTO();
        jsonExportRuleFormatDTO.setId(exportRule.getExportRuleFormat().getId());
        jsonExportRuleFormatDTO.setUuid(exportRule.getExportRuleFormat().getUuid());
        jsonExportRuleFormatDTO.setDateFormat(exportRule.getExportRuleFormat().getDateFormat());
        jsonExportRuleFormatDTO.setDecimalDelimiter(
            exportRule.getExportRuleFormat().getDecimalDelimiter());
        jsonExportRuleFormatDTO.setDecimalPlaces(
            exportRule.getExportRuleFormat().getDecimalPlaces());
        jsonExportRuleFormatDTO.setNumberType(exportRule.getExportRuleFormat().getNumberType());
        jsonExportRuleFormatDTO.setRoundingStrategy(
            exportRule.getExportRuleFormat().getRoundingStrategy());
        return jsonExportRuleFormatDTO;
    }
}