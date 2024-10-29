package de.imi.mopat.helper.model;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

@Component
public class QuestionDTOMapper implements Function<Question, QuestionDTO> {

    private static final int FRONT = 0;
    private static final int BACK = 1;
    private static final int FRONT_BACK = 2;
    private final AnswerDTOMapper answerDTOMapper;

    @Autowired
    public QuestionDTOMapper(ConfigurationDao configurationDao, ConditionDTOMapper conditionDTOMapper) {
        this.answerDTOMapper = new AnswerDTOMapper(configurationDao, conditionDTOMapper);
    }

    /**
     * Converts this {@link Question} object to an {@link QuestionDTO} object.
     *
     * @return An {@link QuestionDTO} object based on this {@link Question}
     * object.
     */
    @Override
    public QuestionDTO apply(Question question) {
        List<String> images = new ArrayList<>();
        SortedMap<Long, AnswerDTO> answerDTOs = new TreeMap<>();

        question.getAnswers().forEach(answer -> {
            AnswerDTO answerDTO = answerDTOMapper.apply(answer);
            answerDTOs.put((long) answerDTOs.size(), answerDTO);

            if (answer instanceof BodyPartAnswer bodyPartAnswer
                    && !images.contains(bodyPartAnswer.getBodyPart().getImagePath())
            ) {
                images.add(bodyPartAnswer.getBodyPart().getImagePath());
            }
        });

        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(question.getId());
        questionDTO.setQuestionType(question.getQuestionType());
        questionDTO.setLocalizedQuestionText(new TreeMap<>(question.getLocalizedQuestionText()));
        questionDTO.setIsRequired(question.getIsRequired());
        questionDTO.setIsEnabled(question.getIsEnabled());
        questionDTO.setMinNumberAnswers(question.getMinNumberAnswers());
        questionDTO.setMaxNumberAnswers(question.getMaxNumberAnswers());
        questionDTO.setCodedValueType(question.getCodedValueType());
        questionDTO.setPosition(question.getPosition());
        questionDTO.setQuestionnaireId(question.getQuestionnaire().getId());
        questionDTO.setAnswers(answerDTOs);
        questionDTO.setBodyPartImages(images);
        questionDTO.setImageType(determineImageType(images));
        return questionDTO;
    }

    private String determineImageType(List<String> images) {
        if (images.contains(Constants.BODY_FRONT) && images.contains(Constants.BODY_BACK)) {
            return Constants.BODY_PART_IMAGE_TYPES[FRONT_BACK];
        } else if (images.contains(Constants.BODY_FRONT)) {
            return Constants.BODY_PART_IMAGE_TYPES[FRONT];
        } else if (images.contains(Constants.BODY_BACK)) {
            return Constants.BODY_PART_IMAGE_TYPES[BACK];
        }
        return Constants.BODY_PART_IMAGE_TYPES[FRONT]; // Is set as default in QuestionDTO
    }
}