package de.imi.mopat.controller;

import de.imi.mopat.dao.*;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.model.QuestionDTOMapper;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.enumeration.BodyPart;
import de.imi.mopat.model.enumeration.CodedValueType;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.validator.MoPatValidator;
import de.imi.mopat.validator.QuestionDTOValidator;
import de.imi.mopat.validator.QuestionValidator;
import de.imi.mopat.validator.SliderAnswerValidator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 *
 */
@Controller
public class QuestionController {

    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private ConditionDao conditionDao;
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private QuestionValidator questionValidator;
    @Autowired
    private QuestionDTOValidator questionDTOValidator;
    @Autowired
    private SliderAnswerValidator sliderAnswerValidator;
    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private SliderIconDao sliderIconDao;

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private QuestionDTOMapper questionDTOMapper;
    /**
     * @param id (<i>optional</i>) Id of the {@link Question} object
     * @return Returns a new {@link Question} object to the attribute
     * <i>question</i> in the model, if no id is given. Otherwise the
     * {@link Question} object associated with the id is returned.
     */
    @ModelAttribute("question")
    public Question getQuestion(final Long id) {
        if (id == null || id <= 0) {
            return new Question();
        } else {
            Question question = questionDao.getElementById(id);
            return (question == null) ? new Question() : question;
        }
    }

    /**
     * @return Returns all {@link QuestionType} objects to the attribute
     * <i>questionTypeList</i> in the model.
     */
    @ModelAttribute("questionTypeList")
    public ArrayList<QuestionType> getQuestionTypeList() {
        return new ArrayList<>(Arrays.asList(QuestionType.values()));
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/question/list</i>. Shows the list of
     * questions.
     *
     * @param id    Show all questions from the given {@link Questionnaire} object Id.
     * @param model The model, which holds the information for the view.
     * @return The <i>question/question</i> website.
     */
    @RequestMapping(value = "/question/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String showQuestions(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);

        if (questionnaire == null) {
            //clear the models attributes that are set in the @ModelAttribute
            // methods
            model.addAttribute("question", null);
            model.addAttribute("questionTypeList", null);
            return "redirect:/questionnaire/list";
        }

        // Create a map where the key is a question id and the value a sorted
        // map, which
        // contains the question texts grouped by the country and languages.
        Map<Long, SortedMap<String, Map<String, String>>> localizedQuestionTextsForQuestion = new HashMap<>();
        for (Question question : questionnaire.getQuestions()) {
            // Get the question texts grouped by country from the current
            // question
            SortedMap<String, Map<String, String>> groupedLocalizedQuestionTextByCountry = question.getLocalizedQuestionTextGroupedByCountry();
            // And add the grouped-by-country-map to the map for all questions
            // of the current questionnaire
            localizedQuestionTextsForQuestion.put(question.getId(),
                groupedLocalizedQuestionTextByCountry);
            // Check if the question has any conditions and set the boolean
            question.setHasConditionsAsTarget(conditionDao.isConditionTarget(question));
            // Check if the question has any scores and set the boolean
            question.setHasScores(scoreDao.hasScore(question));
        }
        model.addAttribute("localizedQuestionTextsForQuestion", localizedQuestionTextsForQuestion);
        model.addAttribute("questionnaire", questionnaire);
        return "question/list";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/question/fill</i>. Shows the page containing
     * the form fields for editing a {@link Question} object.
     *
     * @param questionnaireId The id of a {@link Questionnaire} object to which the question
     *                        belongs.
     * @param questionId      The id of the current {@link Question}
     * @param model           The model, which holds the information for the view.
     * @return The <i>question/edit</i> website.
     */
    @RequestMapping(value = "/question/fill")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String editQuestion(
        @RequestParam(value = "questionnaireId", required = false) final Long questionnaireId,
        @RequestParam(value = "id", required = false) final Long questionId, final Model model) {
        // Create new questionDTO
        QuestionDTO questionDTO = new QuestionDTO();
        if (questionId != null && questionId > 0) {
            // If there is a existing question with given id, transform it to
            // a questionDTO
            Question question = questionDao.getElementById(questionId);
            if (question != null) {
                questionDTO = questionDTOMapper.apply(question);
                questionDTO.setHasScores(scoreDao.hasScore(question));
                // If the question is a multiple choice or drop down question
                // check if there are any conditions associated to the
                // answers and set the boolean
                if (questionDTO.getQuestionType() == QuestionType.DROP_DOWN
                    || questionDTO.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                    for (Map.Entry<Long, AnswerDTO> answerEntry : questionDTO.getAnswers()
                        .entrySet()) {
                        answerEntry.getValue().setHasConditionsAsTarget(
                            conditionDao.isConditionTarget(
                                answerDao.getElementById(answerEntry.getValue().getId())));
                    }
                }
            }
        }

        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        model.addAttribute("questionDTO", questionDTO);
        model.addAttribute("questionnaireId", questionnaireId);
        model.addAttribute("imagePathBodyPartMap", BodyPart.getImagePathBodyPartMap());
        model.addAttribute("imageTypes", Constants.BODY_PART_IMAGE_TYPES);
        model.addAttribute("codedValueTypes", CodedValueType.values());
        return "question/edit";
    }

    /**
     * Controls the HTTP requests for the URL <i>/question/duplicate</i>. Creates a new
     * {@link Question} instance using the
     * {@link Question#cloneWithAnswersAndReferenceToQuestionnaire()} method of the existing
     * {@link Question} from the {@link Model}.
     *
     * @param questionnaireId The id of a {@link Questionnaire} object to which the duplication
     *                        should belong to.
     * @param model           The model, which holds the information for the view.
     * @return The <i>question/edit</i> web site with the new {@link Question} attributes, if
     * duplicating the {@link Question} was successful. If not,
     * <i>question/edit</i> is returned with a newly created question.
     */
    @RequestMapping(value = "/question/duplicate")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String duplicateQuestion(
        @RequestParam(value = "questionnaireId", required = false) final Long questionnaireId,
        final Model model) {
        // get original question from the model
        Object questionObject = model.asMap().get("question");
        Question oldQuestion = (Question) questionObject;
        Question newQuestion;
        if (oldQuestion.getId() != null) { // [bt] getting the old Question
            // from the model was successful, thus a new Question instance
            // with the old Question's attributes can be created
            // create a new question with the old question's attributes
            newQuestion = oldQuestion.cloneWithAnswersAndReferenceToQuestionnaire();
        } else { // [bt] getting the old Question from the model did not work
            // . But a new "oldQuestion" (see getQuestion(Long id) method)
            // was created.
            newQuestion = oldQuestion;
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(newQuestion,
                "question");
            errors.reject("errormessage",
                messageSource.getMessage("question.error" + ".duplicateQuestionMissing",
                    new Object[]{}, LocaleContextHolder.getLocale()));
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "question", errors);
        }

        // override old question in model with the new one
        model.addAttribute(
            "question",
            newQuestion);
        model.addAttribute(
            "questionDTO",
            questionDTOMapper.apply(newQuestion));
        model.addAttribute(
            "questionnaireId",
            questionnaireId);
        // Let the jsp know, that this is a duplicated question
        model.addAttribute("duplicate", true);
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        model.addAttribute("imagePathBodyPartMap", BodyPart.getImagePathBodyPartMap());
        model.addAttribute("imageTypes", Constants.BODY_PART_IMAGE_TYPES);
        model.addAttribute("codedValueTypes", CodedValueType.values());
        return "question/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/question/edit</i>. Provides the ability to
     * update a {@link Question} object.
     *
     * @param action      The name of the submit button which has been clicked.
     * @param questionDTO The forwarded {@link QuestionDTO} object from the form.
     * @param result      The result for validation of the questionDTO object.
     * @param model       The model, which holds the information for the view.
     * @param request     The current request
     * @return Redirect to the <i>question/question</i> website.
     */
    @PostMapping(value = "/question/edit")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String editQuestion(@RequestParam final String action,
        @ModelAttribute("questionDTO") final QuestionDTO questionDTO, final BindingResult result,
        final Model model, final HttpServletRequest request) {
        if (action.equalsIgnoreCase("cancel")) {
            // Clear the model before redirect, to avoid unnecessary
            // attributes in the url.
            model.asMap().clear();
            return "redirect:/question/list?id=" + questionDTO.getQuestionnaireId();
        }

        Question question = null;

        // If the question already exists and is not modifiable, return to
        // edit page
        if (questionDTO.getId() != null) {
            question = questionDao.getElementById(questionDTO.getId());
            if (questionDTO.getQuestionType()
                           .equals(QuestionType.IMAGE) && question.getQuestionType()
                                                                  .equals(QuestionType.IMAGE)) {
                questionDTO.getAnswers()
                           .get(0L)
                           .setImagePath(((ImageAnswer) question.getAnswers()
                                                                .get(0)).getImagePath());
                try {
                    String realPath = configurationDao.getImageUploadPath() + "/question/"+ ((ImageAnswer) question.getAnswers().get(0)).getImagePath();
                    String fileName = realPath.substring(realPath.lastIndexOf("/"));
                    questionDTO.getAnswers()
                        .get(0L)
                        .setImageBase64(
                            StringUtilities.convertImageToBase64String(realPath, fileName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
            if (!question.isModifiable()) {
                return editQuestion(null, questionDTO.getId(), model);
            }
        }

        // Update the answerDTOs localizedLabels with the bodyPart label
        if (questionDTO.getQuestionType() == QuestionType.BODY_PART) {
            if (questionDTO.getAnswers() != null && !questionDTO.getAnswers().isEmpty()) {
                List<String> bodyPartImages = new ArrayList<>();
                for (AnswerDTO answerDTO : questionDTO.getAnswers().values()) {
                    //update images used in this question
                    if (!bodyPartImages.contains(answerDTO.getBodyPartImage())) {
                        bodyPartImages.add(answerDTO.getBodyPartImage());
                    }
                    if (answerDTO.getBodyPartMessageCode() != null) {
                        //update localized label of select answer
                        SortedMap<String, String> localizedLabel = new TreeMap<>();
                        for (String locale : questionDTO.getLocalizedQuestionText().keySet()) {
                            localizedLabel.put(locale,
                                messageSource.getMessage(answerDTO.getBodyPartMessageCode(),
                                    new Object[]{}, LocaleHelper.getLocaleFromString(locale)));
                        }
                        answerDTO.setLocalizedLabel(localizedLabel);
                    }
                }
            }
        }

        // Validate the question dto first
        questionDTOValidator.validate(questionDTO, result);

        if (result.hasErrors()) {
            if (question != null) {
                questionDTO.setHasScores(scoreDao.hasScore(question));
            } else {
                questionDTO.setHasScores(false);
            }
            model.addAttribute("questionnaireId", questionDTO.getQuestionnaireId());
            model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
            model.addAttribute("imagePathBodyPartMap", BodyPart.getImagePathBodyPartMap());
            model.addAttribute("imageTypes", Constants.BODY_PART_IMAGE_TYPES);
            model.addAttribute("codedValueTypes", CodedValueType.values());
            return "question/edit";
        }

        Questionnaire questionnaire = questionnaireDao.getElementById(
            questionDTO.getQuestionnaireId());
        Integer minNumberAnswers = questionDTO.getMinNumberAnswers();
        Integer maxNumberAnswers = questionDTO.getMaxNumberAnswers();
        // If Question already exists
        if (questionDTO.getId() != null) {
            // Check if the associated scores have to be deleted
            if (scoreDao.hasScore(question)) {
                List<QuestionType> validScoreQuestionTypes = new ArrayList<>(
                    Arrays.asList(QuestionType.MULTIPLE_CHOICE, QuestionType.SLIDER,
                        QuestionType.NUMBER_CHECKBOX, QuestionType.NUMBER_CHECKBOX_TEXT,
                        QuestionType.DROP_DOWN, QuestionType.NUMBER_INPUT));
                // The associated scores have to be deleted if the question
                // type does not support scores or the min or max number of
                // answers is different to 1 when a multiple choice question
                // is chosen
                if (!validScoreQuestionTypes.contains(questionDTO.getQuestionType())
                    || questionDTO.getQuestionType().equals(QuestionType.MULTIPLE_CHOICE) && (
                    questionDTO.getMinNumberAnswers() != 1
                        || questionDTO.getMaxNumberAnswers() != 1)) {
                    List<Score> scoresToDelete = scoreDao.getScores(question);
                    for (Score scoreToDelete : scoresToDelete) {
                        scoreDao.remove(scoreToDelete);
                    }
                }
            }

            if (question.getQuestionType().equals(QuestionType.IMAGE)) {
                // Only delete image if it has changed or the question type
                // has changed
                if (!(questionDTO.getQuestionType().equals(QuestionType.IMAGE)
                    && !questionDTO.getAnswers().get(0L).getImageFile().isEmpty())) {
                    File deleteFile = new File(
                        ((ImageAnswer) question.getAnswers().get(0)).getImagePath());
                    deleteFile.delete();
                    ((ImageAnswer) question.getAnswers().get(0)).setImagePath(null);
                }
            }

            QuestionType[] equalQuestiontypesSlider = {QuestionType.SLIDER,
                QuestionType.NUMBER_CHECKBOX};
            QuestionType[] equalQuestiontypesSelect = {QuestionType.MULTIPLE_CHOICE,
                QuestionType.DROP_DOWN};
            List<QuestionType> equalQuestiontypesSliderList = Arrays.asList(
                equalQuestiontypesSlider);
            List<QuestionType> equalQuestiontypesSelectList = Arrays.asList(
                equalQuestiontypesSelect);
            if (equalQuestiontypesSliderList.contains(question.getQuestionType())) {
                if (!equalQuestiontypesSliderList.contains(questionDTO.getQuestionType())) {
                    question.removeAllAnswers();
                }
            } else if (equalQuestiontypesSelectList.contains(question.getQuestionType())) {
                if (!equalQuestiontypesSelectList.contains(questionDTO.getQuestionType())) {
                    question.removeAllAnswers();
                }
            } else if (question.getQuestionType() != questionDTO.getQuestionType()) {
                question.removeAllAnswers();
            }
            // Update properties
            question.setIsRequired(questionDTO.getIsRequired());
            question.setIsEnabled(questionDTO.getIsEnabled());
            question.setQuestionType(questionDTO.getQuestionType());

            //Remove <p>-Tags set by summernote
            SortedMap<String, String> localizedQuestionText = new TreeMap<>();
            for (Map.Entry<String, String> entry : questionDTO.getLocalizedQuestionText()
                .entrySet()) {
                String formattedValue = entry.getValue();
                if (formattedValue.startsWith("<p>")) {
                    formattedValue = formattedValue.substring(3);
                }
                if (formattedValue.endsWith("</p>")) {
                    formattedValue = formattedValue.substring(0, formattedValue.length() - 4);
                }
                localizedQuestionText.put(entry.getKey(), formattedValue);
            }
            question.setLocalizedQuestionText(localizedQuestionText);

            // If multiple choice or dropdown question set min/max number of
            // answers
            // otherwise reset to null
            if (equalQuestiontypesSelectList.contains(question.getQuestionType())
                || question.getQuestionType() == QuestionType.BODY_PART) {
                // If dropdown question set min/max number of answers to 1
                if (question.getQuestionType() == QuestionType.DROP_DOWN) {
                    minNumberAnswers = 1;
                    maxNumberAnswers = 1;
                } else if (questionDTO.getMaxNumberAnswers() == 0) {
                    maxNumberAnswers = 1;
                }
                question.setMinMaxNumberAnswers(minNumberAnswers, maxNumberAnswers);
                question.setCodedValueType(questionDTO.getCodedValueType());
            } else {
                question.setMinMaxNumberAnswers(null, null);
            }
        } else {
            //Remove <p>-Tags set by summernote
            SortedMap<String, String> localizedQuestionText = new TreeMap<>();
            for (Map.Entry<String, String> entry : questionDTO.getLocalizedQuestionText()
                .entrySet()) {
                String formattedValue = entry.getValue();
                if (formattedValue.startsWith("<p>")) {
                    formattedValue = formattedValue.substring(3);
                }
                if (formattedValue.endsWith("</p>")) {
                    formattedValue = formattedValue.substring(0, formattedValue.length() - 4);
                }
                localizedQuestionText.put(entry.getKey(), formattedValue);
            }

            // Question is new
            question = new Question(localizedQuestionText, questionDTO.getIsRequired(),
                questionDTO.getIsEnabled(), questionDTO.getQuestionType(),
                (questionnaire.getQuestions().size() + 1), questionnaire);
            // If dropdown question set min/max number of answers to 1
            if (question.getQuestionType() == QuestionType.DROP_DOWN) {
                minNumberAnswers = 1;
                maxNumberAnswers = 1;
            } else if (questionDTO.getMaxNumberAnswers() == null
                || questionDTO.getMaxNumberAnswers() == 0) {
                maxNumberAnswers = 1;
            }
            question.setMinMaxNumberAnswers(minNumberAnswers, maxNumberAnswers);
            question.setCodedValueType(questionDTO.getCodedValueType());
            questionnaire.addQuestion(question);
        }

        List<Answer> removalList = new ArrayList<>();
        // Creating or updating the answers for the question, depending on
        // the QuestionType
        switch (questionDTO.getQuestionType()) {
            case BODY_PART:
                // Create a list to collect all answers which should be
                // removed from the question
                for (int i = 0; i < question.getAnswers().size(); i++) {
                    BodyPartAnswer bodyPartAnswer = (BodyPartAnswer) question.getAnswers().get(i);
                    AnswerDTO relatedAnswerDTO = null;
                    for (AnswerDTO answerDTO : questionDTO.getAnswers().values()) {
                        if (answerDTO.getId() != null && answerDTO.getId()
                            .equals(bodyPartAnswer.getId())) {
                            relatedAnswerDTO = answerDTO;
                            break;
                        }
                    }
                    if (relatedAnswerDTO == null) {
                        // Add to answer removal list
                        removalList.add(bodyPartAnswer);
                    } else {
                        bodyPartAnswer.setBodyPart(
                            BodyPart.fromString(relatedAnswerDTO.getBodyPartMessageCode()));
                        bodyPartAnswer.setIsEnabled(relatedAnswerDTO.getIsEnabled());
                    }
                }
                // Remove answers from question
                for (Answer removeAnswer : removalList) {
                    // If the deleted answer has any associated conditions
                    if (conditionDao.isConditionTarget(removeAnswer)) {
                        // Delete the associated conditions
                        for (Condition condition : conditionDao.getConditionsByTarget(
                            removeAnswer)) {
                            if (condition instanceof SelectAnswerCondition
                                || condition instanceof SliderAnswerThresholdCondition) {
                                // Refresh the trigger so that multiple
                                // conditions of the same trigger will be
                                // deleted correctly
                                ConditionTrigger conditionTrigger = answerDao.getElementById(
                                    condition.getTrigger().getId());
                                conditionTrigger.removeCondition(condition);
                                answerDao.merge((Answer) conditionTrigger);
                            }
                            conditionDao.remove(condition);
                        }
                    }
                    question.removeAnswer(removeAnswer);
                }
                for (Long i : questionDTO.getAnswers().keySet()) {
                    AnswerDTO answerDTO = questionDTO.getAnswers().get(i);
                    // If answer existed before, do nothing
                    if (answerDTO.getId() != null) {
                        continue;
                    }
                    // Otherwise set localized labelsfor the answers
                    if (answerDTO.getBodyPartMessageCode() != null) {
                        SortedMap<String, String> localizedLabel = new TreeMap<>();
                        for (String locale : questionDTO.getLocalizedQuestionText().keySet()) {
                            localizedLabel.put(locale,
                                messageSource.getMessage(answerDTO.getBodyPartMessageCode(),
                                    new Object[]{}, LocaleHelper.getLocaleFromString(locale)));
                        }
                        answerDTO.setLocalizedLabel(localizedLabel);
                    }

                    // Create new answer
                    BodyPartAnswer bodyPartAnswer = new BodyPartAnswer(
                        BodyPart.fromString(answerDTO.getBodyPartMessageCode()), question,
                        answerDTO.getIsEnabled());
                }
                break;
            case MULTIPLE_CHOICE:
            case DROP_DOWN: {
                // Create a list to collect all answers which should be
                // removed from the question
                boolean hasIsOtherFlag = false;
                boolean isIsOtherEnabled = true;
                boolean freetextAnswerExists = false;
                FreetextAnswer existingFreetextAnswer = null;

                for (int i = 0; i < question.getAnswers().size(); i++) {
                    if (question.getAnswers().get(i) instanceof SelectAnswer selectAnswer) {
                        AnswerDTO relatedAnswerDTO = null;
                        for (AnswerDTO answerDTO : questionDTO.getAnswers().values()) {
                            if (answerDTO.getId() != null && answerDTO.getId()
                                .equals(selectAnswer.getId())) {
                                relatedAnswerDTO = answerDTO;
                                break;
                            }
                        }
                        if (relatedAnswerDTO == null) {
                            // Add to answer removal list
                            removalList.add(selectAnswer);
                        } else {
                            // Update answer
                            selectAnswer.setLocalizedLabel(relatedAnswerDTO.getLocalizedLabel());
                            selectAnswer.setIsEnabled(relatedAnswerDTO.getIsEnabled());
                            selectAnswer.setIsOther(relatedAnswerDTO.getIsOther());
                            selectAnswer.setValue(relatedAnswerDTO.getValue());
                            selectAnswer.setCodedValue(relatedAnswerDTO.getCodedValue());
                            // Check if one existing answer is marked as other
                            if (selectAnswer.getIsOther()) {
                                hasIsOtherFlag = true;
                                isIsOtherEnabled = selectAnswer.getIsEnabled();
                            }
                        }
                    } else {
                        // If the current answer is no select answer, there
                        // must be a freetext answer
                        freetextAnswerExists = true;
                        existingFreetextAnswer = (FreetextAnswer) question.getAnswers().get(i);
                    }
                }

                // Remove answers from question
                for (Answer removeAnswer : removalList) {
                    // If the deleted answer has any associated conditions
                    if (conditionDao.isConditionTarget(removeAnswer)) {
                        // Delete the associated conditions
                        for (Condition condition : conditionDao.getConditionsByTarget(
                            removeAnswer)) {
                            if (condition instanceof SelectAnswerCondition
                                || condition instanceof SliderAnswerThresholdCondition) {
                                // Refresh the trigger so that multiple
                                // conditions of the same trigger will be
                                // deleted correctly
                                ConditionTrigger conditionTrigger = answerDao.getElementById(
                                    condition.getTrigger().getId());
                                conditionTrigger.removeCondition(condition);
                                answerDao.merge((Answer) conditionTrigger);
                            }
                            conditionDao.remove(condition);
                        }
                    }
                    question.removeAnswer(removeAnswer);
                }
                for (Long i : questionDTO.getAnswers().keySet()) {
                    AnswerDTO answerDTO = questionDTO.getAnswers().get(i);

                    if (answerDTO.getId() != null) {
                        continue;
                    }

                    // Create new answer
                    SelectAnswer selectAnswer = new SelectAnswer(question, answerDTO.getIsEnabled(),
                        answerDTO.getLocalizedLabel(), answerDTO.getIsOther());
                    selectAnswer.setValue(answerDTO.getValue());
                    selectAnswer.setCodedValue(answerDTO.getCodedValue());
                    // Check if one new answer is marked as other
                    if (selectAnswer.getIsOther()) {
                        hasIsOtherFlag = true;
                        isIsOtherEnabled = selectAnswer.getIsEnabled();
                    }
                }

                // Check if a freetext answer has to be created or removed or
                // updated
                if (hasIsOtherFlag && !freetextAnswerExists) {
                    // Create new freetext answer
                    FreetextAnswer freetextAnswer = new FreetextAnswer(question, isIsOtherEnabled);
                } else if (!hasIsOtherFlag && freetextAnswerExists) {
                    // Remove existing freetext answer
                    question.removeAnswer(existingFreetextAnswer);
                } else if (hasIsOtherFlag && freetextAnswerExists) {
                    existingFreetextAnswer.setIsEnabled(isIsOtherEnabled);
                }
                break;
            }
            case SLIDER:
            case NUMBER_CHECKBOX: {
                // [bt] TODO add additional validation, since we know here
                //  the question type (and at least one answer has to be
                //  given, e.g.)
                AnswerDTO answerDTO = questionDTO.getAnswers().get(0L);
                SliderAnswer sliderAnswer;
                Boolean vertical = answerDTO.getVertical();
                // Slider cannot be vertical, only NumberCheckboxes can be
                // displayed vertical
                if (questionDTO.getQuestionType() == QuestionType.SLIDER) {
                    vertical = false;
                }
                Boolean isEnabled = answerDTO.getIsEnabled();
                Double minValue = answerDTO.getMinValue();
                Double maxValue = answerDTO.getMaxValue();
                Double stepsize = Double.parseDouble(answerDTO.getStepsize().replace(',', '.'));
                Boolean showIcons = answerDTO.getShowIcons();
                if (!question.getAnswers().isEmpty()) {
                    // Update answer
                    sliderAnswer = (SliderAnswer) question.getAnswers().get(0);
                    sliderAnswer.setMinValue(minValue);
                    sliderAnswer.setMaxValue(maxValue);
                    sliderAnswer.setStepsize(stepsize);
                    sliderAnswer.setVertical(vertical);
                    sliderAnswer.setIsEnabled(isEnabled);
                    sliderAnswer.setShowIcons(showIcons);
                    //
                    Set<SliderIcon> iconSet = new HashSet<>();
                    for (SliderIconDTO icon : answerDTO.getIcons()) {
                        SliderIcon newIcon = new SliderIcon(icon.getPosition(), icon.getIcon(),
                            sliderAnswer);
                        iconSet.add(newIcon);
                    }
                    sliderAnswer.setIcons(iconSet);
                } else {
                    // Create new answer
                    sliderAnswer = new SliderAnswer(question, isEnabled, minValue, maxValue,
                        stepsize, vertical);
                    sliderAnswer.setShowIcons(showIcons);
                    Set<SliderIcon> iconSet = new HashSet<>();
                    for (SliderIconDTO icon : answerDTO.getIcons()) {
                        SliderIcon newIcon = new SliderIcon(icon.getPosition(), icon.getIcon(),
                            sliderAnswer);
                        iconSet.add(newIcon);
                    }
                    sliderAnswer.setIcons(iconSet);
                }
                if (answerDTO.getLocalizedMinimumText() != null) {
                    for (Map.Entry<String, String> entry : answerDTO.getLocalizedMinimumText()
                        .entrySet()) {
                        if (entry.getValue() == null || entry.getValue().trim().isEmpty()
                            || Pattern.matches("<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>",
                            entry.getValue())) {
                            answerDTO.getLocalizedMinimumText().put(entry.getKey(), "");
                        }
                    }
                    sliderAnswer.setLocalizedMinimumText(answerDTO.getLocalizedMinimumText());
                } else {
                    sliderAnswer.setLocalizedMinimumText(null);
                }
                if (answerDTO.getLocalizedMaximumText() != null) {
                    for (Map.Entry<String, String> entry : answerDTO.getLocalizedMaximumText()
                        .entrySet()) {
                        if (entry.getValue() == null || entry.getValue().trim().isEmpty()
                            || Pattern.matches("<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>",
                            entry.getValue())) {
                            answerDTO.getLocalizedMaximumText().put(entry.getKey(), "");
                        }
                    }
                    sliderAnswer.setLocalizedMaximumText(answerDTO.getLocalizedMaximumText());
                } else {
                    sliderAnswer.setLocalizedMaximumText(null);
                }
                if (answerDTO.getShowValueOnButton() != null) {
                    sliderAnswer.setShowValueOnButton(answerDTO.getShowValueOnButton());
                } else {
                    sliderAnswer.setShowValueOnButton(false);
                }
                break;
            }
            case NUMBER_CHECKBOX_TEXT: {
                AnswerDTO answerDTO = questionDTO.getAnswers().get(0L);
                SliderFreetextAnswer sliderFreetextAnswer;
                // Numbercheckbox with freetext is always horizontal
                Boolean vertical = false;
                Boolean isEnabled = answerDTO.getIsEnabled();
                Double minValue = answerDTO.getMinValue();
                Double maxValue = answerDTO.getMaxValue();
                Double stepsize = Double.parseDouble(answerDTO.getStepsize().replace(',', '.'));
                if (!question.getAnswers().isEmpty()) {
                    // Create new answer
                    sliderFreetextAnswer = (SliderFreetextAnswer) question.getAnswers().get(0);
                    sliderFreetextAnswer.setQuestion(question);
                    sliderFreetextAnswer.setMinValue(minValue);
                    sliderFreetextAnswer.setMaxValue(maxValue);
                    sliderFreetextAnswer.setStepsize(stepsize);
                    sliderFreetextAnswer.setVertical(vertical);
                    sliderFreetextAnswer.setIsEnabled(isEnabled);
                } else {
                    // Update answer
                    sliderFreetextAnswer = new SliderFreetextAnswer(question, isEnabled, minValue,
                        maxValue, stepsize, answerDTO.getLocalizedFreetextLabel(), vertical);
                }
                // Set or update minimum and maximum text
                if (answerDTO.getLocalizedMinimumText() != null) {
                    for (Map.Entry<String, String> entry : answerDTO.getLocalizedMinimumText()
                        .entrySet()) {
                        if (entry.getValue() == null || entry.getValue().trim().isEmpty()
                            || Pattern.matches("<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>",
                            entry.getValue())) {
                            answerDTO.getLocalizedMinimumText().put(entry.getKey(), "");
                        }
                    }
                    sliderFreetextAnswer.setLocalizedMinimumText(
                        answerDTO.getLocalizedMinimumText());
                } else {
                    sliderFreetextAnswer.setLocalizedMinimumText(null);
                }
                if (answerDTO.getLocalizedMaximumText() != null) {
                    for (Map.Entry<String, String> entry : answerDTO.getLocalizedMaximumText()
                        .entrySet()) {
                        if (entry.getValue() == null || entry.getValue().trim().isEmpty()
                            || Pattern.matches("<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>",
                            entry.getValue())) {
                            answerDTO.getLocalizedMaximumText().put(entry.getKey(), "");
                        }
                    }
                    sliderFreetextAnswer.setLocalizedMaximumText(
                        answerDTO.getLocalizedMaximumText());
                } else {
                    sliderFreetextAnswer.setLocalizedMaximumText(null);
                }
                sliderFreetextAnswer.setLocalizedFreetextLabel(
                    answerDTO.getLocalizedFreetextLabel());
                break;
            }
            case FREE_TEXT:
            case BARCODE: {
                if (!question.getAnswers().isEmpty()) {
                    // Update freetext answer
                    FreetextAnswer freetextAnswer = (FreetextAnswer) question.getAnswers().get(0);
                    freetextAnswer.setQuestion(question);
                    freetextAnswer.setIsEnabled(true);
                } else {
                    // Create new answer
                    FreetextAnswer freetextAnswer = new FreetextAnswer(question, true);
                }
                break;
            }
            case INFO_TEXT: {
                // Nothing to do here, since the info text is within question
                // .questionText
                break;
            }
            case DATE: {
                AnswerDTO answerDTO = questionDTO.getAnswers().get(0L);
                SimpleDateFormat dateFormat = Constants.DATE_FORMAT;
                Boolean isEnabled = answerDTO.getIsEnabled();
                Date startDate = null;
                Date endDate = null;
                try {
                    // Parse start and end date
                    if (answerDTO.getStartDate() != null && !answerDTO.getStartDate().isEmpty()) {
                        startDate = dateFormat.parse(answerDTO.getStartDate());
                    }
                    if (answerDTO.getEndDate() != null && !answerDTO.getEndDate().isEmpty()) {
                        endDate = dateFormat.parse(answerDTO.getEndDate());
                    }
                } catch (ParseException ex) {
                    // Exception already caught in validator
                }
                if (!question.getAnswers().isEmpty()) {
                    // Update answer
                    DateAnswer dateAnswer = (DateAnswer) question.getAnswers().get(0);
                    dateAnswer.setQuestion(question);
                    dateAnswer.setStartDate(startDate);
                    dateAnswer.setEndDate(endDate);
                    dateAnswer.setIsEnabled(isEnabled);
                } else {
                    // Create new answer
                    DateAnswer dateAnswer = new DateAnswer(question, isEnabled, startDate, endDate);
                }
                break;
            }
            case NUMBER_INPUT: {
                AnswerDTO answerDTO = questionDTO.getAnswers().get(0L);
                Boolean isEnabled = answerDTO.getIsEnabled();
                Double minValue = answerDTO.getMinValue();
                Double maxValue = answerDTO.getMaxValue();
                Double stepsize = null;
                if (answerDTO.getStepsize() != null && !answerDTO.getStepsize().isEmpty()) {
                    stepsize = Double.parseDouble(answerDTO.getStepsize().replace(',', '.'));
                }

                if (!question.getAnswers().isEmpty()) {
                    // Update answer
                    NumberInputAnswer numberInputAnswer = (NumberInputAnswer) question.getAnswers()
                        .get(0);
                    numberInputAnswer.setQuestion(question);
                    numberInputAnswer.setMinValue(minValue);
                    numberInputAnswer.setMaxValue(maxValue);
                    numberInputAnswer.setStepsize(stepsize);
                    numberInputAnswer.setIsEnabled(isEnabled);
                } else {
                    // Create new answer
                    NumberInputAnswer numberInputAnswer = new NumberInputAnswer(question, isEnabled,
                        minValue, maxValue, stepsize);
                }
                break;
            }
            case IMAGE: {
                // Merge the question if it's new to get its ID to save the
                // image with this specific ID
                if (questionDTO.getId() == null) {
                    questionDao.merge(question);
                }
                AnswerDTO answerDTO = questionDTO.getAnswers().get(0L);
                Boolean isEnabled = answerDTO.getIsEnabled();

                String storagePath;

                // Upload the new Image if the question is new or the image
                // has changed
                if (!answerDTO.getImageFile().isEmpty()) {
                    // Store the extension of the image and the path with the
                    // questionnaire ID
                    String imageExtension =
                        FilenameUtils.getExtension(answerDTO.getImageFile()
                                                            .getOriginalFilename());
                    String imagePath =
                        (configurationDao.getImageUploadPath()
                            + "/question/" + questionnaire.getId());

                    // Check if the upload dir exists. If not, create it
                    File uploadDir = new File(imagePath);
                    if (!uploadDir.isDirectory()) {
                        uploadDir.mkdirs();
                    }
                    // Set the upload filename to question and its ID
                    File uploadFile = new File(imagePath,
                        "question" + question.getId() + "." + imageExtension);
                    try {
                        // Write the image to disk
                        BufferedImage uploadImage = ImageIO.read(
                            answerDTO.getImageFile().getInputStream());
                        ImageIO.write(uploadImage, imageExtension, uploadFile);
                    } catch (IOException ex) {
                        // If an error occures while uploading, write it down
                        // and delete the question if it was new
                        result.pushNestedPath("answers[0]");
                        result.rejectValue("imageFile", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                            messageSource.getMessage("imageAnswer.error.upload", new Object[]{},
                                LocaleContextHolder.getLocale()));
                        result.popNestedPath();
                        if (questionDTO.getId() == null) {
                            questionDao.remove(question);
                        }
                    }
                    // Store the full storage path with name and extension
                    storagePath =
                        questionnaire.getId()
                            + "/question" + question.getId()
                            + "." + imageExtension;
                } else {
                    // If the image has not changed use the old image path
                    storagePath = answerDTO.getImagePath();
                }

                if (!question.getAnswers().isEmpty()) {
                    // Update answer
                    ImageAnswer imageAnswer = (ImageAnswer) question.getAnswers().get(0);
                    imageAnswer.setIsEnabled(isEnabled);
                    imageAnswer.setImagePath(storagePath);
                } else {
                    // Create new answer
                    ImageAnswer imageAnswer = new ImageAnswer(question, isEnabled, storagePath);
                }
                break;
            }
            default:
                break;
        }

        // Validate the question
        questionValidator.validate(question, result);

        if (result.hasErrors()) {
            model.addAttribute("questionnaireId", questionDTO.getQuestionnaireId());
            model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
            model.addAttribute("imagePathBodyPartMap", BodyPart.getImagePathBodyPartMap());
            model.addAttribute("imageTypes", Constants.BODY_PART_IMAGE_TYPES);
            model.addAttribute("codedValueTypes", CodedValueType.values());
            return "question/edit";
        }
        // Merge in any case, because the questionnaire is already persisted
        questionDao.merge(question);
        questionnaireDao.merge(questionnaire);

        // Since the next step is a redirect to a controller which loads a
        // new model,
        // the model can be cleared here, so that the model attributes are
        // not part
        // of the URL
        model.asMap().clear();
        return "redirect:/question/list?id=" + question.getQuestionnaire().getId();
    }

    /**
     * Controls the HTTP requests for the URL <i>question/remove</i>. Removes a {@link Question}
     * object by a given id and redirects to the list of questions.
     *
     * @param id    Id of the {@link Question} object, which should be removed.
     * @param model The model, which holds the information for the view.
     * @return Redirect to the <i>question/question</i> website.
     */
    @RequestMapping(value = "/question/remove")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String removeQuestion(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {

        Question questionToDelete = questionDao.getElementById(id);
        List<String> errorMessages = new ArrayList<>();
        // If question is not deletable, show an error message
        if (!questionToDelete.isDeletable()) {
            errorMessages.add(
                messageSource.getMessage("question.label.deleteQuestionNotPossible", new Object[]{},
                    LocaleContextHolder.getLocale()));
            model.addAttribute("errorMessages", errorMessages);
            return showQuestions(questionToDelete.getQuestionnaire().getId(), model);
        }
        // If question is target of a condition
        if (conditionDao.isConditionTarget(questionToDelete)) {
            // Delete the associated conditions
            for (Condition condition : conditionDao.getConditionsByTarget(questionToDelete)) {
                if (condition instanceof SelectAnswerCondition
                    || condition instanceof SliderAnswerThresholdCondition) {
                    // Refresh the trigger so that multiple conditions of the
                    // same trigger will be correctly deleted
                    ConditionTrigger conditionTrigger = answerDao.getElementById(
                        condition.getTrigger().getId());
                    conditionTrigger.removeCondition(condition);
                    answerDao.merge((Answer) conditionTrigger);
                }
                conditionDao.remove(condition);
            }
        }

        Questionnaire questionnaire = questionToDelete.getQuestionnaire();

        // If question has any scores
        if (scoreDao.hasScore(questionToDelete)) {
            for (Score score : scoreDao.getScores(questionToDelete)) {
                questionnaire.removeScore(score);
                scoreDao.remove(score);
            }
        }

        Integer oldPosition = questionToDelete.getPosition();
        questionnaire.removeQuestion(questionToDelete);
        // Update the position of the remaining questions
        Set<Question> questions = questionnaire.getQuestions();
        for (Question question : questions) {
            if (oldPosition < question.getPosition()) {
                question.setPosition(question.getPosition() - 1);
            }
        }

        // Delete the image if it is an image question
        if (questionToDelete.getQuestionType().equals(QuestionType.IMAGE)) {
            File deleteFile = new File(
                ((ImageAnswer) questionToDelete.getAnswers().get(0)).getImagePath());
            deleteFile.delete();
        }

        questionnaireDao.merge(questionnaire);

        // Since the next step is a redirect to a controller which loads a
        // new model,
        // the model can be cleared here, so that the model attributes are
        // not part
        // of the URL
        model.asMap().clear();
        return "redirect:/question/list?id=" + questionnaire.getId();
    }

    /**
     * Controls the HTTP requests for the URL <i>question/reposition</i>.
     *
     * @param questionIds     List of question Id's representing the new order of questions.
     * @param questionnaireId Id of the {@link Questionnaire} object, whose questions are
     *                        repositioned.
     * @return An empty String
     */
    @RequestMapping(value = "/question/reposition")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public @ResponseBody String repositionQuestion(
        @RequestParam(value = "questionIds", required = true) final List<Long> questionIds,
        @RequestParam(value = "questionnaireId", required = true) final Long questionnaireId) {
        Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireId);

        // Check if any condition trigger is after its target
        for (int i = 0; i < questionIds.size(); i++) {
            Question question = questionDao.getElementById(questionIds.get(i));
            for (Answer answer : question.getAnswers()) {
                for (Condition condition : answer.getConditions()) {
                    for (int j = 0; j < i; j++) {
                        // Check if the question is a target of this condition
                        if (Objects.equals(condition.getTarget().getId(), questionIds.get(j))) {
                            return messageSource.getMessage(
                                "questionnaire.questions.reposition" + ".conditionError",
                                new Object[]{}, LocaleContextHolder.getLocale());
                        }
                        // Check if any of this questions answers is a target
                        // of this condition
                        for (Answer otherAnswer : questionDao.getElementById(questionIds.get(j))
                            .getAnswers()) {
                            if (Objects.equals(condition.getTarget().getId(),
                                otherAnswer.getId())) {
                                return messageSource.getMessage(
                                    "questionnaire.questions.reposition" + ".conditionError",
                                    new Object[]{}, LocaleContextHolder.getLocale());
                            }
                        }
                    }
                }
            }
        }

        for (Question question : questionnaire.getQuestions()) {
            question.setPosition((questionIds.indexOf(question.getId()) + 1));
        }
        questionnaireDao.merge(questionnaire);

        return "";
    }
}
