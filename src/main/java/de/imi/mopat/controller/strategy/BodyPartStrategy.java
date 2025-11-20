package de.imi.mopat.controller.strategy;

import de.imi.mopat.controller.QuestionController;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.enumeration.BodyPart;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

public class BodyPartStrategy implements CreateOrUpdateAnswerStrategy {
    @Override
    public void createOrUpdateAnswer(QuestionDTO questionDTO, Question question, QuestionController controller, BindingResult result, Questionnaire questionnaire) {
        List<Answer> removalList = new ArrayList<>();
        // Create a list to collect all answers which should be
        // removed from the question
        findAnswersWithoutDTOToRemoveOrSetBodyPartOn(questionDTO, question, removalList);
        // Remove answers from question
        removeAnswersFromQuestion(removalList, question, controller);
        addAnswersToQuestionFromDTO(questionDTO, question, controller);
    }

    private void findAnswersWithoutDTOToRemoveOrSetBodyPartOn(QuestionDTO questionDTO, Question question, List<Answer> removalList) {
        for (int i = 0; i < question.getAnswers().size(); i++) {
            BodyPartAnswer bodyPartAnswer = (BodyPartAnswer) question.getAnswers().get(i);
            AnswerDTO relatedAnswerDTO = findRelatedAnswerDTO(questionDTO,bodyPartAnswer);
            if (relatedAnswerDTO == null) {
                // Add to answer removal list
                removalList.add(bodyPartAnswer);
            } else {
                bodyPartAnswer.setBodyPart(
                        BodyPart.fromString(relatedAnswerDTO.getBodyPartMessageCode()));
                bodyPartAnswer.setIsEnabled(relatedAnswerDTO.getIsEnabled());
            }
        }
    }

    private void addAnswersToQuestionFromDTO(QuestionDTO questionDTO, Question question, QuestionController controller) {
        for (Long i : questionDTO.getAnswers().keySet()) {
            AnswerDTO answerDTO = questionDTO.getAnswers().get(i);
            // If answer existed before, do nothing
            if (answerDTO.getId() != null) {
                continue;
            }
            // Otherwise set localized labelsfor the answers
            controller.setLocalizedLabelByBodyPartMessage(questionDTO, answerDTO);

            // Create new answer
            BodyPartAnswer bodyPartAnswer = new BodyPartAnswer(
                    BodyPart.fromString(answerDTO.getBodyPartMessageCode()), question,
                    answerDTO.getIsEnabled());
        }
    }
}
