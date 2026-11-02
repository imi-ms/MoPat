package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface CreateOrUpdateAnswerStrategy {

    void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire);

    default AnswerDTO findRelatedAnswerDTO(QuestionDTO questionDTO, Answer answer) {
        for (AnswerDTO answerDTO : questionDTO.getAnswers().values()) {
            if (answerDTO.getId() != null && answerDTO.getId()
                    .equals(answer.getId())) {
                return answerDTO;
            }
        }
        return null;
    }

    default void removeAnswersFromQuestion(List<Answer> removalList, Question question, QuestionController controller) {
        for (Answer removeAnswer : removalList) {
            // If the deleted answer has any associated conditions
            if (controller.getConditionDao().isConditionTarget(removeAnswer)) {
                // Delete the associated conditions
                for (Condition condition : controller.getConditionDao().getConditionsByTarget(
                        removeAnswer)) {
                    if (condition instanceof SelectAnswerCondition
                            || condition instanceof SliderAnswerThresholdCondition) {
                        // Refresh the trigger so that multiple
                        // conditions of the same trigger will be
                        // deleted correctly
                        ConditionTrigger conditionTrigger = controller.getAnswerDao().getElementById(
                                condition.getTrigger().getId());
                        conditionTrigger.removeCondition(condition);
                        controller.getAnswerDao().merge((Answer) conditionTrigger);
                    }
                    controller.getConditionDao().remove(condition);
                }
            }
            question.removeAnswer(removeAnswer);
        }
    }
}
