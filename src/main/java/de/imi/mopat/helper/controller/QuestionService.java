package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.SliderIcon;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.ConditionDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.model.enumeration.BodyPart;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);
    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private ConditionDao conditionDao;


    /**
     * Converts this {@link Question} object to an {@link QuestionDTO} object.
     *
     * @return An {@link QuestionDTO} object based on this {@link Question}
     * object.
     */
    @JsonIgnore
    public QuestionDTO toQuestionDTO(Question question) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(question.getId());
        questionDTO.setQuestionType(question.getQuestionType());
        questionDTO.setLocalizedQuestionText(new TreeMap<>(question.getLocalizedQuestionText()));
        questionDTO.setIsRequired(question.getIsRequired());
        questionDTO.setIsEnabled(question.getIsEnabled());
        questionDTO.setMinNumberAnswers(question.getMinNumberAnswers());
        questionDTO.setMaxNumberAnswers(question.getMaxNumberAnswers());
        questionDTO.setCodedValueType(question.getCodedValueType());
        questionDTO.setPosition(question.getPosition());
        questionDTO.setQuestionnaireId(question.getQuestionnaire()
            .getId());
        SortedMap<Long, AnswerDTO> answerDTOs = new TreeMap<>();

        List<String> images = new ArrayList<>();
        for (Answer answer : question.getAnswers()) {
            AnswerDTO answerDTO = new AnswerDTO();
            answerDTO.setId(answer.getId());
            answerDTO.setIsEnabled(answer.getIsEnabled());
            if (answer instanceof SelectAnswer) {
                answerDTO.setLocalizedLabel(new TreeMap<>(((SelectAnswer) answer).getLocalizedLabel()));

                if (((SelectAnswer) answer).getValue()
                    != null) {
                    answerDTO.setValue(((SelectAnswer) answer).getValue());
                }
                answerDTO.setIsOther(((SelectAnswer) answer).getIsOther());
                answerDTO.setCodedValue(((SelectAnswer) answer).getCodedValue());
            } else if (answer instanceof SliderFreetextAnswer) {
                answerDTO.setMinValue(((SliderFreetextAnswer) answer).getMinValue());
                answerDTO.setMaxValue(((SliderFreetextAnswer) answer).getMaxValue());
                answerDTO.setVertical(((SliderFreetextAnswer) answer).getVertical());

                // Format the stepsize
                DecimalFormat decimalFormat = new DecimalFormat(
                    "0",
                    DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                decimalFormat.setMaximumFractionDigits(340); //340 =
                // DecimalFormat.DOUBLE_FRACTION_DIGITS
                String formattedStepsize =
                    decimalFormat.format(((SliderAnswer) answer).getStepsize());
                answerDTO.setStepsize(formattedStepsize);

                answerDTO.setLocalizedMinimumText(new TreeMap<>(((SliderFreetextAnswer) answer).getLocalizedMinimumText()));
                answerDTO.setLocalizedMaximumText(new TreeMap<>(((SliderFreetextAnswer) answer).getLocalizedMaximumText()));
                answerDTO.setLocalizedFreetextLabel(new TreeMap<>(((SliderFreetextAnswer) answer).getLocalizedFreetextLabel()));
            } else if (answer instanceof SliderAnswer) {
                answerDTO.setMinValue(((SliderAnswer) answer).getMinValue());
                answerDTO.setMaxValue(((SliderAnswer) answer).getMaxValue());
                answerDTO.setVertical(((SliderAnswer) answer).getVertical());
                // Format the stepsize
                DecimalFormat decimalFormat = new DecimalFormat(
                    "0",
                    DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                decimalFormat.setMaximumFractionDigits(340); //340 =
                // DecimalFormat.DOUBLE_FRACTION_DIGITS
                String formattedStepsize =
                    decimalFormat.format(((SliderAnswer) answer).getStepsize());
                answerDTO.setStepsize(formattedStepsize);

                answerDTO.setLocalizedMinimumText(new TreeMap<>(((SliderAnswer) answer).getLocalizedMinimumText()));
                answerDTO.setLocalizedMaximumText(new TreeMap<>(((SliderAnswer) answer).getLocalizedMaximumText()));
                answerDTO.setShowValueOnButton(((SliderAnswer) answer).getShowValueOnButton());

                answerDTO.setShowIcons(((SliderAnswer) answer).getShowIcons());

                List<SliderIconDTO> iconList = new ArrayList<>();
                for (SliderIcon icon : ((SliderAnswer) answer).getIcons()) {
                    SliderIconDTO newSliderIconDTO = new SliderIconDTO();
                    newSliderIconDTO.setIcon(icon.getIcon());
                    newSliderIconDTO.setPosition(icon.getPosition());
                    newSliderIconDTO.setAnswerId(answer.getId());
                    iconList.add(newSliderIconDTO);
                }

                answerDTO.setIcons(iconList);

            } else if (answer instanceof DateAnswer) {
                Date startDate = ((DateAnswer) answer).getStartDate();
                Date endDate = ((DateAnswer) answer).getEndDate();
                SimpleDateFormat dateFormat = Constants.DATE_FORMAT;
                if (startDate
                    != null) {
                    answerDTO.setStartDate(dateFormat.format(startDate));
                }
                if (endDate
                    != null) {
                    answerDTO.setEndDate(dateFormat.format(endDate));
                }
            } else if (answer instanceof NumberInputAnswer) {
                answerDTO.setMinValue(((NumberInputAnswer) answer).getMinValue());
                answerDTO.setMaxValue(((NumberInputAnswer) answer).getMaxValue());
                if (((NumberInputAnswer) answer).getStepsize()
                    != null) {
                    answerDTO.setStepsize(((NumberInputAnswer) answer).getStepsize()
                        .toString());
                }
            } else if (answer instanceof ImageAnswer) {
                ImageAnswer imageAnswer = (ImageAnswer) answer;
                answerDTO.setImagePath(configurationDao.getImageUploadPath() + "/question/"+ imageAnswer.getImagePath());
                try {
                    //Navigate out of classpath root and WEB-INF
                    String realPath = answerDTO.getImagePath();
                    String fileName = answerDTO.getImagePath().substring(answerDTO.getImagePath().lastIndexOf("/"));
                    answerDTO.setImageBase64(StringUtilities.convertImageToBase64String(realPath, fileName));
                } catch (IOException e) {
                    LOGGER.error("Image of answer with id "
                        + imageAnswer.getId()
                        + " and path "
                        + imageAnswer.getImagePath()
                        + " was not readable!");
                }
            } else if (answer instanceof BodyPartAnswer) {
                BodyPartAnswer bodyPartAnswer = (BodyPartAnswer) answer;
                BodyPart bodyPart = bodyPartAnswer.getBodyPart();
                answerDTO.setBodyPartPath(bodyPart.getPath());
                answerDTO.setBodyPartMessageCode(bodyPart.getMessageCode());
                answerDTO.setBodyPartImage(bodyPart.getImagePath());
                if (!images.contains(bodyPart.getImagePath())) {
                    images.add(bodyPart.getImagePath());
                }
            }
            questionDTO.setBodyPartImages(images);

            if (images.contains(Constants.BODY_FRONT)
                && !images.contains(Constants.BODY_BACK)) {
                questionDTO.setImageType(Constants.BODY_PART_IMAGE_TYPES[0]);
            } else if (!images.contains(Constants.BODY_FRONT)
                && images.contains(Constants.BODY_BACK)) {
                questionDTO.setImageType(Constants.BODY_PART_IMAGE_TYPES[1]);
            } else if (images.contains(Constants.BODY_FRONT)
                && images.contains(Constants.BODY_BACK)) {
                questionDTO.setImageType(Constants.BODY_PART_IMAGE_TYPES[2]);
            }

            answerDTO.setHasResponse(!answer.getResponses()
                .isEmpty());
            answerDTO.setHasConditionsAsTrigger(!answer.getConditions()
                .isEmpty());
            List<ConditionDTO> conditionDTOs = new ArrayList<>();
            if (!answer.getConditions()
                .isEmpty()) {
                for (Condition condition : answer.getConditions()) {
                    conditionDTOs.add(condition.toConditionDTO());
                }
            }
            answerDTO.setConditions(conditionDTOs);

            answerDTO.setHasExportRule(!answer.getExportRules()
                .isEmpty());
            answerDTOs.put(
                Long.valueOf(answerDTOs.size()),
                answerDTO);
        }

        questionDTO.setAnswers(answerDTOs);
        return questionDTO;
    }

    public Set<Question> copyQuestionsToQuestionnaire(Set<Question> originalQuestions, Questionnaire questionnaire) {
        Set<Question> copiedQuestions = new HashSet<>();
        for (Question question : originalQuestions) {
            Question newQuestion = question.cloneWithAnswersAndReferenceToQuestionnaire(questionnaire);
            copiedQuestions.add(newQuestion);
            newQuestion.setQuestionnaire(questionnaire);
        }
        return copiedQuestions;
    }

    public void cloneConditions(Set<Question> originalQuestions, Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap, Map<Question, Question> questionMap){
        for(Question originalQuestion : originalQuestions){
            for(Answer answer : originalQuestion.getAnswers()){
                Set<Condition> conditions = answer.getConditions();
                Set<Condition> newConditions = new HashSet<>();
                for(Condition condition: conditions){
                    Condition newCondition;
                    if(condition.getTrigger().getClass() == answer.getClass() && condition.getTarget().getClass() == originalQuestion.getClass()){
                    newCondition = condition.cloneCondition(oldQuestionToNewAnswerMap.get(originalQuestion).get(answer), questionMap.get((Question) condition.getTarget()));
                    }
                    else if(condition.getTrigger().getClass() == answer.getClass() && condition.getTarget().getClass() == answer.getClass()){
                        Question taq = condition.getTargetAnswerQuestion();
                    newCondition = condition.cloneCondition(oldQuestionToNewAnswerMap.get(originalQuestion).get(answer), oldQuestionToNewAnswerMap.get(taq).get((Answer) condition.getTarget()));
                    }
                    else {
                        newCondition = null;
                    }
                newConditions.add(newCondition);
                conditionDao.merge(newCondition);
                }
            }
        }
    }

    Map<Question, Question> duplicateQuestionsToNewQuestionnaire(Set<Question> originalQuestions, Questionnaire newQuestionnaire) {
        Map<Question, Question> questionMap = new HashMap<>();
        Set<Question> copiedQuestions = new HashSet<>();
        Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap = new HashMap<>();
        for (Question originalQuestion : originalQuestions) {
            Question newQuestion = new Question(new HashMap<>(originalQuestion.getLocalizedQuestionText()),
                originalQuestion.getIsRequired(), originalQuestion.getIsEnabled(), originalQuestion.getQuestionType(), originalQuestion.getPosition(), newQuestionnaire);
            newQuestion.setMinMaxNumberAnswers(originalQuestion.getMinNumberAnswers(), originalQuestion.getMaxNumberAnswers());
            Map<Answer, Answer> answerMap = new HashMap<>();
            for (Answer answer : originalQuestion.getAnswers()) {
                Answer newAnswer = answer.cloneWithoutReferences();
                answerMap.put(answer, newAnswer);
                newQuestion.addAnswer(newAnswer);
            }
            newQuestion.setQuestionnaire(newQuestionnaire);
            questionMap.put(originalQuestion, newQuestion);
            oldQuestionToNewAnswerMap.put(originalQuestion, answerMap);
            copiedQuestions.add(newQuestion);
            questionDao.merge(newQuestion);
        }
            cloneConditions(originalQuestions, oldQuestionToNewAnswerMap, questionMap);

            newQuestionnaire.setQuestions(copiedQuestions);
        return questionMap;
    }
}
