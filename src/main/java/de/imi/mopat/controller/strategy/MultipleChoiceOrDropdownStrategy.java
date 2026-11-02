package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceOrDropdownStrategy implements CreateOrUpdateAnswerStrategy {
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {
        List<Answer> removalList = new ArrayList<>();
        // Create a list to collect all answers which should be
        // removed from the question
        boolean hasIsOtherFlag = false;
        boolean isIsOtherEnabled = true;
        boolean freetextAnswerExists = false;
        FreetextAnswer existingFreetextAnswer = null;

        for (int i = 0; i < question.getAnswers().size(); i++) {
            if (question.getAnswers().get(i) instanceof SelectAnswer selectAnswer) {
                AnswerDTO relatedAnswerDTO = findRelatedAnswerDTO(questionDTO, selectAnswer);
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
        removeAnswersFromQuestion(removalList, question, controller);
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
    }
}
