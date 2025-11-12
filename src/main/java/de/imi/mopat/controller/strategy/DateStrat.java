package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import org.springframework.validation.BindingResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateStrat implements CreateOrUpdateAnswerStrategy {
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {
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
    }
}
