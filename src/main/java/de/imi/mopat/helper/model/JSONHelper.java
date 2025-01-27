package de.imi.mopat.helper.model;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.dto.export.JsonAnswerDTO;
import de.imi.mopat.model.dto.export.JsonConditionDTO;
import de.imi.mopat.model.dto.export.JsonQuestionDTO;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonScoreDTO;
import de.imi.mopat.model.score.Score;
import java.io.IOException;

public class JSONHelper{

    private final ConfigurationDao configurationDao;

    public JSONHelper(final ConfigurationDao configurationDao){
        this.configurationDao = configurationDao;
    }

    public void initializeJsonQuestionnaireDTO(JsonQuestionnaireDTO jsonQuestionnaireDTO, final Questionnaire questionnaire){
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
            this.initializeJsonQuestionDTO(jsonQuestionDTO,question);
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

    public void initializeJsonQuestionDTO(JsonQuestionDTO jsonQuestionDTO,final Question question){
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
            jsonAnswerDTO = this.initializeJsonAnswerDTO(jsonAnswerDTO, answer);
            jsonQuestionDTO.setAnswers(answer.getId(), jsonAnswerDTO);
            jsonAnswerDTO.setJsonQuestionDTO(jsonQuestionDTO);
        }
    }

    public JsonAnswerDTO initializeJsonAnswerDTO(JsonAnswerDTO jsonAnswerDTO, Answer answer){
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

        if (answer instanceof SelectAnswer) {
            SelectAnswer selectAnswer = (SelectAnswer) answer;
            jsonAnswerDTO.setLocalizedLabel(selectAnswer.getLocalizedLabel());
            jsonAnswerDTO.setValue(selectAnswer.getValue());
            jsonAnswerDTO.setCodedValue(selectAnswer.getCodedValue());
            jsonAnswerDTO.setIsOther(selectAnswer.getIsOther());
        }
        if (answer instanceof SliderAnswer) {
            SliderAnswer sliderAnswer = (SliderAnswer) answer;
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
        if (answer instanceof NumberInputAnswer) {
            NumberInputAnswer numberInputAnswer = (NumberInputAnswer) answer;
            jsonAnswerDTO.setMinValue(numberInputAnswer.getMinValue());
            jsonAnswerDTO.setMaxValue(numberInputAnswer.getMaxValue());
            if (numberInputAnswer.getStepsize()
                != null) {
                jsonAnswerDTO.setStepsize(numberInputAnswer.getStepsize()
                    .toString());
            }
        }
        if (answer instanceof DateAnswer) {
            DateAnswer dateAnswer = (DateAnswer) answer;
            if (dateAnswer.getStartDate()
                != null) {
                jsonAnswerDTO.setStartDate(Constants.DATE_FORMAT.format(dateAnswer.getStartDate()));
            }
            if (dateAnswer.getEndDate()
                != null) {
                jsonAnswerDTO.setEndDate(Constants.DATE_FORMAT.format(dateAnswer.getEndDate()));
            }
        }
        if (answer instanceof SliderFreetextAnswer) {
            SliderFreetextAnswer sliderFreetextAnswer =
                (SliderFreetextAnswer) answer;
            jsonAnswerDTO.setLocalizedFreetextLabel(sliderFreetextAnswer.getLocalizedFreetextLabel());
            jsonAnswerDTO.setLocalizedMaximumText(sliderFreetextAnswer.getLocalizedMaximumText());
            jsonAnswerDTO.setLocalizedMinimumText(sliderFreetextAnswer.getLocalizedMinimumText());
            jsonAnswerDTO.setMaxValue(sliderFreetextAnswer.getMaxValue());
            jsonAnswerDTO.setMinValue(sliderFreetextAnswer.getMinValue());
            jsonAnswerDTO.setStepsize(sliderFreetextAnswer.getStepsize()
                .toString());
        }
        if (answer instanceof ImageAnswer) {
            ImageAnswer imageAnswer = (ImageAnswer) answer;
            jsonAnswerDTO.setImagePath(imageAnswer.getImagePath());
            // Try to load the image from the disk as a BufferedImage and get
            // the Base64 representation
            try {
                String imagePath = (configurationDao.getImageUploadPath() + "/question/" +jsonAnswerDTO.getImagePath());
                String fileName = jsonAnswerDTO.getImagePath()
                    .substring(imageAnswer.getImagePath()
                        .lastIndexOf("/"));
                jsonAnswerDTO.setImageBase64(StringUtilities.convertImageToBase64String(
                    imagePath,
                    fileName));
            } catch (IOException e) {
            }
        }
        if (answer instanceof BodyPartAnswer) {
            BodyPartAnswer bodyPartAnswer = (BodyPartAnswer) answer;
            jsonAnswerDTO.setBodyPart(bodyPartAnswer.getBodyPart());
        }
        return jsonAnswerDTO;
    }

}
