package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import org.springframework.validation.BindingResult;

import java.util.Map;
import java.util.regex.Pattern;

public class NumberCheckBoxTextStrategy implements CreateOrUpdateAnswerStrategy {
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {
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
    }
}
