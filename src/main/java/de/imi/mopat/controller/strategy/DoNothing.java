package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.QuestionDTO;
import org.springframework.validation.BindingResult;

public class DoNothing implements CreateOrUpdateAnswerStrategy{
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {

    }
}
