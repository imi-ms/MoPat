package de.imi.mopat.io.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.QuestionnaireVersionGroupService;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.dto.export.JsonAnswerDTO;
import de.imi.mopat.model.dto.export.JsonConditionDTO;
import de.imi.mopat.model.dto.export.JsonQuestionDTO;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonScoreDTO;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.score.Operator;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.user.User;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class MoPatQuestionnaireImporter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MoPatQuestionnaireImporter.class);

    @Autowired
    OperatorDao operatorDao;

    @Autowired
    QuestionnaireDao questionnaireDao;

    @Autowired
    QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Autowired
    ConfigurationDao configurationDao;

    public Questionnaire importQuestionnaire(MultipartFile file) throws IOException {
        Questionnaire questionnaire;

        ObjectMapper mapper = new ObjectMapper();
        JsonQuestionnaireDTO jsonQuestionnaireDTO = mapper.readValue(file.getInputStream(), JsonQuestionnaireDTO.class);
        questionnaire = jsonQuestionnaireDTO.convertToQuestionnaire();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        questionnaire.setChangedBy(currentUser.getId());
        // Collect all questions and answers in a map to access those
        // ones who are target and trigger of a condition easily
        Map<Long, Question> questions = new HashMap<>();
        Map<Long, Answer> answers = new HashMap<>();

        // Convert all jsonQuestionDTOs and all jsonAnswerDTOs to
        // their database model counterparts and collect them in a
        // map for conversion of conditions
        for (Long questionId : jsonQuestionnaireDTO.getQuestionDTOs().keySet()) {
            JsonQuestionDTO jsonQuestionDTO = jsonQuestionnaireDTO.getQuestionDTOs().get(questionId);
            Question question = jsonQuestionDTO.convertToQuestion();
            questions.put(questionId, question);
            // If the question is of type multiple choice or drop
            // down we have to make sure that the freetext answer is
            // added as the last answer
            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                || question.getQuestionType() == QuestionType.DROP_DOWN) {
                Long freetextAnswerId = null;
                for (Long answerId : jsonQuestionDTO.getAnswers().keySet()) {
                    JsonAnswerDTO jsonAnswerDTO = jsonQuestionDTO.getAnswers().get(answerId);
                    // If the current answer has at least one
                    // localized label it is a select answer
                    if (jsonAnswerDTO.getLocalizedLabel() != null && !jsonAnswerDTO.getLocalizedLabel().isEmpty()) {
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
                    JsonAnswerDTO jsonAnswerDTO = jsonQuestionDTO.getAnswers().get(freetextAnswerId);
                    answers.put(freetextAnswerId, jsonAnswerDTO.convertToAnswer(question));
                }
            } else {
                // For all other questiontypes just convert the
                // answers and add them
                for (Long answerId : jsonQuestionDTO.getAnswers().keySet()) {
                    JsonAnswerDTO jsonAnswerDTO = jsonQuestionDTO.getAnswers().get(answerId);
                    answers.put(answerId, jsonAnswerDTO.convertToAnswer(question));
                }
            }

        }

        questionnaire.addQuestions(questions.values());

        Map<JsonAnswerDTO, JsonQuestionDTO> imageAnswerQuestions = new HashMap<>();
        // Convert all jsonConditionDTOs to their database model
        // counterparts
        for (JsonQuestionDTO jsonQuestionDTO : jsonQuestionnaireDTO.getQuestionDTOs().values()) {
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
                    if (jsonConditionDTO.getTargetClass().equals("de.imi.mopat.model" + ".Question")) {
                        condition.setTarget(questions.get(jsonConditionDTO.getTargetId()));
                    } else if (jsonConditionDTO.getTargetClass().equals("de.imi.mopat" + ".model" + ".SelectAnswer")
                        || jsonConditionDTO.getTargetClass().equals("de.imi.mopat.model.ImageAnswer")
                        || jsonConditionDTO.getTargetClass().equals("de.imi.mopat.model.SliderAnswer")
                        || jsonConditionDTO.getTargetClass().equals("de.imi.mopat.model.SliderFreetextAnswer")
                        || jsonConditionDTO.getTargetClass().equals("de.imi.mopat.model.DateAnswer")
                        || jsonConditionDTO.getTargetClass().equals("de.imi.mopat.model.FreetextAnswer")
                        || jsonConditionDTO.getTargetClass().equals("de.imi.mopat.model.NumberInputAnswer")) {
                        condition.setTarget(answers.get(jsonConditionDTO.getTargetId()));
                        condition.setTargetAnswerQuestion(questions.get(jsonConditionDTO.getTargetAnswerQuestionId()));
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
            Score score = jsonScoreDTO.convertToScore(operators, questions, scoreIdExpressions);
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
            questionnaire.setName(questionnaire.getName() + " " + new Timestamp(new Date().getTime()));
        }

        questionnaireDao.merge(questionnaire);

        QuestionnaireVersionGroup questionnaireVersionGroup = questionnaireVersionGroupService.createQuestionnaireGroup(
            questionnaire.getName());
        questionnaire.setQuestionnaireVersionGroup(questionnaireVersionGroup);
        questionnaireVersionGroup.addQuestionnaire(questionnaire);
        questionnaireVersionGroupService.add(questionnaireVersionGroup);

        //Loop through all persisted questions to get the
        // imageAnswers and save the images
        for (Question question : questionnaire.getQuestions()) {
            if (question.getQuestionType() == QuestionType.IMAGE) {
                ImageAnswer answer = (ImageAnswer) question.getAnswers().get(0);
                for (JsonAnswerDTO answerDTO : imageAnswerQuestions.keySet()) {
                    if (answer.getImagePath().equals(answerDTO.getImagePath())) {
                        try {
                            String imageBase64 = answerDTO.getImageBase64();
                            String imagePath = (configurationDao.getImageUploadPath() + "/questionnaire/"
                                + questionnaire.getId());
                            String fileName =
                                "question" + question.getId() + "." + StringUtilities.getMimeTypeFromBase64String(
                                    imageBase64);
                            answer.setImagePath(questionnaire.getId() + "/" + fileName);
                            StringUtilities.convertAndWriteBase64StringToImage(answerDTO.getImageBase64(), imagePath,
                                fileName);
                        } catch (Exception e) {
                            LOGGER.info("Converting image failed. " + "Following " + "error " + "occurred: {}",
                                e.getMessage());
                        }
                    }
                }
            }
        }

        if (jsonQuestionnaireDTO.getLogoBase64() != null) {
            try {
                String logoBase64 = jsonQuestionnaireDTO.getLogoBase64();
                String imagePath = (configurationDao.getImageUploadPath() + "/questionnaire/" + questionnaire.getId());
                String fileName = Constants.LOGO_PROPERTY + "." + logoBase64.substring("data:image/".length(),
                    logoBase64.lastIndexOf(";base64,"));
                questionnaire.setLogo(fileName);
                StringUtilities.convertAndWriteBase64StringToImage(jsonQuestionnaireDTO.getLogoBase64(), imagePath,
                    fileName);
            } catch (IOException e) {
                LOGGER.info("Converting logo failed. Following error " + "occurred: {}", e.getMessage());
            }
        }

        questionnaireDao.merge(questionnaire);
        return questionnaire;
    }

}
