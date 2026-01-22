package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderIcon;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.model.enumeration.QuestionType;
import org.springframework.validation.BindingResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SliderOrNumCheckBoxStrategy implements CreateOrUpdateAnswerStrategy {
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {
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
    }
}
