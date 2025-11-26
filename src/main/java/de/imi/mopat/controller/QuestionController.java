package de.imi.mopat.controller;

import de.imi.mopat.controller.util.SaveAndEditNextInOrderUtil;
import de.imi.mopat.dao.*;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.model.QuestionDTOMapper;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.*;
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
import de.imi.mopat.validator.QuestionDTOValidator;
import de.imi.mopat.validator.QuestionValidator;
import de.imi.mopat.validator.SliderAnswerValidator;

import java.io.File;
import java.io.IOException;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
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

    private final QuestionType[] equalQuestionTypesSlider = {QuestionType.SLIDER,
            QuestionType.NUMBER_CHECKBOX};
    private final QuestionType[] equalQuestionTypesSelect = {QuestionType.MULTIPLE_CHOICE,
            QuestionType.DROP_DOWN};
    private final List<QuestionType> equalQuestiontypesSliderList = Arrays.asList(
            equalQuestionTypesSlider);
    private final List<QuestionType> equalQuestiontypesSelectList = Arrays.asList(
            equalQuestionTypesSelect);

    public ConditionDao getConditionDao() {
        return this.conditionDao;
    }

    public ConfigurationDao getConfigurationDao() {
        return this.configurationDao;
    }

    public QuestionDao getQuestionDao() {
        return this.questionDao;
    }

    public AnswerDao getAnswerDao() {
        return this.answerDao;
    }

    public MessageSource getMessageSource() {
        return this.messageSource;
    }

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
            setImagePathForImageQuestion(questionDTO, question);
            if (!question.isModifiable()) {
                return editQuestion(null, questionDTO.getId(), model);
            }
        }

        // Update the answerDTOs localizedLabels with the bodyPart label
        if (questionDTO.getQuestionType() == QuestionType.BODY_PART) {
            updateLocalizedLabelsWithBodyPartLabels(questionDTO);
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
        Map<String, String> localizedQuestionText = removePTags(questionDTO);
        // If Question already exists
        if (questionDTO.getId() != null) {
            updatePropertiesOfExistingQuestion(questionDTO, question, localizedQuestionText, minNumberAnswers, maxNumberAnswers);
        } else {
            question = createQuestion(questionDTO, localizedQuestionText, questionnaire, minNumberAnswers, maxNumberAnswers);
            questionnaire.addQuestion(question);
        }
        // Creating or updating the answers for the question, depending on
        // the QuestionType
        questionDTO.getQuestionType().getStrategy().createOrUpdateAnswer(questionDTO, question, this, result, questionnaire);

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
        String defaultSaveRoute = "redirect:/question/list?id=" + question.getQuestionnaire().getId();
        return SaveAndEditNextInOrderUtil.determineNextRoute("question", action, defaultSaveRoute);
    }

    private void setImagePathForImageQuestion(QuestionDTO questionDTO, Question question) {
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
    }

    public void setLocalizedLabelByBodyPartMessage(QuestionDTO questionDTO, AnswerDTO answerDTO) {
        if (answerDTO.getBodyPartMessageCode() != null) {
            SortedMap<String, String> localizedLabel = new TreeMap<>();
            for (String locale : questionDTO.getLocalizedQuestionText().keySet()) {
                localizedLabel.put(locale,
                    messageSource.getMessage(answerDTO.getBodyPartMessageCode(),
                        new Object[]{}, LocaleHelper.getLocaleFromString(locale)));
            }
            answerDTO.setLocalizedLabel(localizedLabel);
        }
    }

    private void updateLocalizedLabelsWithBodyPartLabels(QuestionDTO questionDTO) {
        if (questionDTO.getAnswers() != null && !questionDTO.getAnswers().isEmpty()) {
            List<String> bodyPartImages = new ArrayList<>();
            for (AnswerDTO answerDTO : questionDTO.getAnswers().values()) {
                //update images used in this question
                if (!bodyPartImages.contains(answerDTO.getBodyPartImage())) {
                    bodyPartImages.add(answerDTO.getBodyPartImage());
                }
                setLocalizedLabelByBodyPartMessage(questionDTO, answerDTO);
            }
        }
    }

    private void deleteScoresIfNecessary(QuestionDTO questionDTO, Question question) {
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
    }

    private void deleteImageIfNecessary(QuestionDTO questionDTO, Question question) {
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
    }

    private void removeAnswersIfNecessary(QuestionDTO questionDTO, Question question) {
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
    }

    private SortedMap<String, String> removePTags(QuestionDTO questionDTO) {
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
        return localizedQuestionText;
    }

    private void setMinMaxNumberOfAnswers(QuestionDTO questionDTO, List<QuestionType> equalQuestiontypesSelectList, Question question, Integer minNumberAnswers, Integer maxNumberAnswers) {
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
    }

    private void updatePropertiesOfExistingQuestion(QuestionDTO questionDTO, Question question, Map<String, String> localizedQuestionText, Integer minNumberAnswers, Integer maxNumberAnswers) {
        deleteScoresIfNecessary(questionDTO, question);
        deleteImageIfNecessary(questionDTO, question);
        removeAnswersIfNecessary(questionDTO, question);
        // Update properties
        question.setIsRequired(questionDTO.getIsRequired());
        question.setIsEnabled(questionDTO.getIsEnabled());
        question.setQuestionType(questionDTO.getQuestionType());
        question.setLocalizedQuestionText(localizedQuestionText);
        question.setIsJustInfo(questionDTO.getIsJustInfo());

        // If multiple choice or dropdown question set min/max number of
        // answers
        // otherwise reset to null
        setMinMaxNumberOfAnswers(questionDTO, equalQuestiontypesSelectList, question, minNumberAnswers, maxNumberAnswers);
    }

    private Question createQuestion(QuestionDTO questionDTO, Map<String, String> localizedQuestionText, Questionnaire questionnaire, Integer minNumberAnswers, Integer maxNumberAnswers) {
        Question question;
        // Question is new
        question = new Question(localizedQuestionText, questionDTO.getIsRequired(),
                questionDTO.getIsEnabled(), questionDTO.getQuestionType(),
                (questionnaire.getQuestions().size() + 1), questionnaire, questionDTO.getIsJustInfo());
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
        return question;
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
