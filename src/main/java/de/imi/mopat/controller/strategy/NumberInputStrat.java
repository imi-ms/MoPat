package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import org.springframework.validation.BindingResult;

public class NumberInputStrat implements CreateOrUpdateAnswerStrategy {
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {
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
    }
}
