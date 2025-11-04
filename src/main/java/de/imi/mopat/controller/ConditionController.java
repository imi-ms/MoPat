package de.imi.mopat.controller;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.controller.ConditionService;
import de.imi.mopat.helper.model.BundleDTOMapper;
import de.imi.mopat.helper.model.ConditionDTOMapper;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionActionType;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.conditions.ThresholdComparisonType;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.ConditionDTO;
import de.imi.mopat.model.dto.ConditionListDTO;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.validator.ConditionListDTOValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @since v1.2
 */
@Controller
public class ConditionController {

    private static final ArrayList<QuestionType> CONDITIONABLE_QUESTIONTYPES = new ArrayList<QuestionType>(
        Arrays.asList(QuestionType.MULTIPLE_CHOICE, QuestionType.DROP_DOWN, QuestionType.SLIDER,
            QuestionType.NUMBER_CHECKBOX, QuestionType.NUMBER_INPUT));

    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private ConditionDao conditionDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private ConditionListDTOValidator conditionListDTOValidator;
    @Autowired
    private BundleDTOMapper bundleDTOMapper;
    @Autowired
    private ConditionDTOMapper conditionDTOMapper;
    @Autowired
    private ConditionService conditionService;

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/condition/listQuestionConditions</i>. Shows the list of
     * {@link Condition Conditions} for a given {@link Question}.
     *
     * @param questionId         : The id of the {@link Question}.
     * @param model              : The model, which holds the information for the view.
     * @param redirectAttributes Stores the information for a redirect scenario.
     * @return The <i>question/listQuestion</i> website.
     */
    @GetMapping(value = "/condition/listQuestionConditions")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String showConditionsForQuestion(
        @RequestParam(value = "questionId", required = true) final Long questionId,
        final Model model, final RedirectAttributes redirectAttributes) {
        Question question = questionDao.getElementById(questionId);
        if (CONDITIONABLE_QUESTIONTYPES.contains(question.getQuestionType())) {
            model.addAttribute("question", question);
            return "condition/listQuestion";
        }

        return "redirect:/question/list?id=" + question.getQuestionnaire().getId();
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/condition/edit</i> Shows the page containing the form fields for
     * editing a condition {@link Condition} object.
     *
     * @param conditionId             :The id of the {@link Condition}.
     * @param questionId              :The id of the {@link Question} to which the {@link Condition} belongs.
     * @param action                  :The name of the submit button which has been clicked.
     * @param thresholdValue          :The value for the threshold the {@link Condition} will be triggered.
     * @param thresholdComparisonType The operator ({@link ThresholdComparisonType}) for the comparison with the
     *                                thresholdValue.
     * @param model                   :The model, which holds the information for the view.
     * @param answerId                :The id of the answer to get
     * @return The <i>condition/edit</i> website.
     */
    @GetMapping(value = "/condition/edit")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String showEditConditionForm(
        @RequestParam(value = "conditionId", required = false) final Long conditionId,
        @RequestParam(value = "questionId", required = true) final Long questionId,
        @RequestParam(value = "answerId", required = false) final Long answerId,
        @RequestParam(value = "thresholdValue", required = false) final Double thresholdValue,
        @RequestParam(value = "thresholdComparisonType", required = false) final String thresholdComparisonType,
        @RequestParam(value = "action", required = false) final String action, final Model model) {

        Question question = questionDao.getElementById(questionId);
        Questionnaire questionnaire = question.getQuestionnaire();

        List<Condition> conditions = new ArrayList<>();
        ArrayList<ConditionDTO> conditionDTOs = new ArrayList<>();
        ConditionListDTO conditionListDTO = new ConditionListDTO();

        if (action != null && action.equalsIgnoreCase("backToQuestionnaire")
            || !CONDITIONABLE_QUESTIONTYPES.contains(question.getQuestionType())) {
            return "redirect:/question/list?id=" + question.getQuestionnaire().getId();
        } else if (conditionId != null && conditionId > 0L) {
            conditions = conditionDao.getConditionsByTriggerCondition(
                conditionDao.getElementById(conditionId));
        } else {
            Answer answer;
            // If there is no answerId, select the first answer of the question
            if (answerId == null) {
                answer = answerDao.getElementById(question.getAnswers().get(0).getId());
            } else {
                answer = answerDao.getElementById(answerId);
            }
            // Get the right conditions
            if (answer instanceof SelectAnswer) {
                conditions = conditionDao.getConditionsByTriggerAnswer(answer, null, null);
            } else if (answerId != null && thresholdValue != null
                && thresholdComparisonType != null) {
                conditions = conditionDao.getConditionsByTriggerAnswer(answer, thresholdValue,
                    ThresholdComparisonType.valueOf(thresholdComparisonType));
            } else {
                conditions = conditionDao.getConditionsByTriggerAnswer(answer, 0d,
                    ThresholdComparisonType.SMALLER_THAN);
            }
        }

        // Attach the available BudleDTOs and QuestionnaireDTOs to the
        // ConditionListDTO
        //All BundleDTOs that contain the question's questionnaire
        List<BundleDTO> availableBundleDTOs = new ArrayList<>();
        //All BundleQuestionnaireDTOs that contain the question's
        // questionnaire to make sure that those ones are listed unique
        List<BundleQuestionnaireDTO> availableBundleQuestionnaireDTOs = new ArrayList<>();

        /*
         Get all bundles that contain a bundleQuestionnaire whose
         questionnaire is targetable by the condition.
         Add targetable bundleDTOs and bundleQuestionnaireDTOs seperatly
         to the conditionDTO to access the corresponding objects easily in
         the view.
         This way it is possible to list the two different collections more
         easily.
         Otherwise showing and accessing the items at the view would be more
         complex.
         */
        for (BundleQuestionnaire currentBundleQuestionnaire : questionnaire.getBundleQuestionnaires()) {
            BundleDTO bundleDTO = bundleDTOMapper.apply(true, currentBundleQuestionnaire.getBundle());

            boolean hasAssignedBundleQuestionnaire = false;
            // If there is one questionnaire in this bundle after the current
            // questionnaire
            for (BundleQuestionnaireDTO bundleQuestionnaireDTO : bundleDTO.getBundleQuestionnaireDTOs()) {
                if (currentBundleQuestionnaire.getPosition()
                    < bundleQuestionnaireDTO.getPosition()) {
                    availableBundleQuestionnaireDTOs.add(bundleQuestionnaireDTO);
                    hasAssignedBundleQuestionnaire = true;
                }
            }
            // Only add a bundleDTO if it offers at least one targetable
            // bundleQuestionnaire
            if (hasAssignedBundleQuestionnaire) {
                availableBundleDTOs.add(bundleDTO);
            }
        }

        conditionListDTO.setAvailableBundleDTOs(availableBundleDTOs);
        conditionListDTO.setAvailableBundleQuestionnaireDTOs(availableBundleQuestionnaireDTOs);

        if (!conditions.isEmpty()) {
            for (Condition condition : conditions) {
                ConditionDTO conditionDTO = conditionDTOMapper.apply(condition);
                conditionDTOs.add(conditionDTO);
            }
        } else {
            //Create conditionDTO
            ConditionDTO conditionDTO = new ConditionDTO();
            if (answerId != null) {
                conditionDTO.setTriggerId(answerId);
            }
            if (question.getQuestionType() == QuestionType.SLIDER
                || question.getQuestionType() == QuestionType.NUMBER_CHECKBOX
                || question.getQuestionType() == QuestionType.NUMBER_INPUT) {
                Answer answer;
                if (answerId != null) {
                    answer = answerDao.getElementById(answerId);
                    conditionDTO.setThresholdType(
                        ThresholdComparisonType.valueOf(thresholdComparisonType));
                } else {
                    answer = answerDao.getElementById(question.getAnswers().get(0).getId());
                    conditionDTO.setThresholdType(ThresholdComparisonType.SMALLER_THAN);
                }
                Double minValue = 0d;
                Double maxValue = 0d;
                if (answer instanceof NumberInputAnswer) {
                    minValue = ((NumberInputAnswer) answer).getMinValue();
                    maxValue = ((NumberInputAnswer) answer).getMaxValue();
                } else if (answer instanceof SliderAnswer) {
                    minValue = ((SliderAnswer) answer).getMinValue();
                    maxValue = ((SliderAnswer) answer).getMaxValue();
                }

                if (thresholdValue != null && thresholdValue >= minValue && thresholdValue <= maxValue) {
                    conditionDTO.setThresholdValue(thresholdValue);
                } else if (thresholdValue != null && thresholdValue < minValue) {
                    conditionDTO.setThresholdValue(minValue);
                } else if (thresholdValue != null && thresholdValue > maxValue) {
                    conditionDTO.setThresholdValue(maxValue);
                } else {
                    conditionDTO.setThresholdValue(minValue);
                }
            }
            conditionDTOs.add(conditionDTO);
        }

        conditionListDTO.setConditionDTOs(conditionDTOs);

        // Add current language to the model
        Locale locale = LocaleContextHolder.getLocale();
        model.addAttribute("currentLanguage", locale.toString());
        model.addAttribute("conditionListDTO", conditionListDTO);
        model.addAttribute("question", question);
        model.addAttribute("conditionActionTypeList", ConditionActionType.values());
        model.addAttribute("thresholdComparisonType", ThresholdComparisonType.values());
        return "condition/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/condition/edit</i>. Takes the form fields for editing a
     * {@link Condition} object and save it.
     *
     * @param postAction       :Verifies which submit button was used.
     * @param questionId       :The id of the {@link Question} to which the {@link Condition} belongs.
     * @param conditionListDTO The {@link ConditionListDTO} object that holds the {@link Condition Conditions} to edit.
     * @param result           :The result for validation of the conditionListDTO object.
     * @param model            :The model, which holds the information for the view.
     * @return The <i>condition/listQuestionConditions</i> website.
     */
    @PostMapping(value = "/condition/edit")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String editCondition(@RequestParam final String postAction,
        @RequestParam(value = "questionId", required = true) final Long questionId,
        @ModelAttribute(value = "conditionListDTO") final ConditionListDTO conditionListDTO,
        final BindingResult result, final Model model) {
        if (postAction.equalsIgnoreCase("cancel")) {
            // Clear the model before redirect, to avoid unnecessary
            // attributes in the url.
            model.asMap().clear();
            return "redirect:/condition/listQuestionConditions?questionId=" + questionId;
        }

        //Validate the conditions
        conditionListDTOValidator.validate(conditionListDTO, result);
        if (result.hasErrors()) {
            Locale locale = LocaleContextHolder.getLocale();
            model.addAttribute("currentLanguage", locale.toString());
            model.addAttribute("conditionListDTO", conditionListDTO);
            model.addAttribute("question", questionDao.getElementById(questionId));
            model.addAttribute("conditionActionTypeList", ConditionActionType.values());
            model.addAttribute("thresholdComparisonType", ThresholdComparisonType.values());
            return "condition/edit";
        }

        // Remove all conditions with the same trigger condition
        if (conditionListDTO.getConditionDTOs().get(0).getId() != null) {
            List<Condition> existingConditions = conditionDao.getConditionsByTriggerCondition(
                conditionDao.getElementById(conditionListDTO.getConditionDTOs().get(0).getId()));
            for (Condition condition : existingConditions) {
                ConditionTrigger conditionTrigger = condition.getTrigger();
                conditionTrigger.removeCondition(condition);
                if (condition instanceof SelectAnswerCondition
                    || condition instanceof SliderAnswerThresholdCondition) {
                    answerDao.merge((Answer) conditionTrigger);
                }
            }
        }

        // Iterate over each conditionDTO and save it
        for (ConditionDTO conditionDTO : conditionListDTO.getConditionDTOs()) {
            conditionService.mergeCondition(conditionDTO);
        }

        model.addAttribute("question", questionDao.getElementById(questionId));
        return "redirect:listQuestionConditions?questionId=" + questionId;
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/condition/remove</i>. Removes an existing condition,
     *
     * @param conditionId conditionId The id of the {@link Condition}.
     * @param questionId  The id of the {@link Question} to which the {@link Condition} belongs.
     * @param model       The model, which holds the information for the view.
     * @return The <i>condition/listQuestionConditions</i> website.
     */
    @RequestMapping(value = "/condition/remove", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String removeCondition(
        @RequestParam(value = "id", required = false) final Long conditionId,
        @RequestParam(value = "questionId", required = false) final Long questionId,
        final Model model) {
        // Remove an existing condition
        if (conditionId != null && conditionId > 0) {
            Condition condition = conditionDao.getElementById(conditionId);
            if (condition != null) {
                ConditionTrigger conditionTrigger = condition.getTrigger();
                conditionTrigger.removeCondition(condition);
                if (condition instanceof SelectAnswerCondition
                    || condition instanceof SliderAnswerThresholdCondition) {
                    answerDao.merge((Answer) conditionTrigger);
                }
            }
        }

        model.addAttribute("question", questionDao.getElementById(questionId));
        return "redirect:listQuestionConditions?questionId=" + questionId;
    }
}
