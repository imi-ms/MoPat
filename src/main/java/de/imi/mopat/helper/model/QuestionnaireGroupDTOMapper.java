package de.imi.mopat.helper.model;

import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireVersionGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Component
public class QuestionnaireGroupDTOMapper implements Function<QuestionnaireVersionGroup, QuestionnaireVersionGroupDTO> {

    private final QuestionnaireDTOMapper questionnaireDTOMapper;

    @Autowired
    public QuestionnaireGroupDTOMapper(QuestionnaireDTOMapper questionnaireDTOMapper) {
        this.questionnaireDTOMapper = questionnaireDTOMapper;
    }

    @Override
    public QuestionnaireVersionGroupDTO apply(QuestionnaireVersionGroup questionnaireVersionGroup) {
        if (questionnaireVersionGroup == null) {
            throw new IllegalArgumentException("QuestionnaireGroup must not be null");
        }
        if (!questionnaireVersionGroup.hasQuestionnaires()) {
            QuestionnaireVersionGroupDTO noMemberQuestionnaireVersionGroupDTO = new QuestionnaireVersionGroupDTO();
            noMemberQuestionnaireVersionGroupDTO.setGroupId(questionnaireVersionGroup.getId());
            return noMemberQuestionnaireVersionGroupDTO;
        }

        // Sort the questionnaires by version number
        List<QuestionnaireDTO> questionnaireDTOs = questionnaireVersionGroup.getQuestionnaires().stream()
                .map(questionnaireDTOMapper::applyWithoutGroup)
                .sorted(Comparator.comparing(QuestionnaireDTO::getVersion))
                .toList();

        // Use the name of the questionnaire with the smallest version number as the group name
        String groupName = questionnaireDTOs.get(0).getName();

        // Get the group ID (assuming all items in groupList have the same group ID)
        Long groupId = questionnaireVersionGroup.getId();

        QuestionnaireVersionGroupDTO questionnaireVersionGroupDTO = new QuestionnaireVersionGroupDTO();
        questionnaireVersionGroupDTO.setGroupId(groupId);
        questionnaireVersionGroupDTO.setGroupName(groupName);
        questionnaireVersionGroupDTO.setQuestionnaireDTOS(questionnaireDTOs);
        return questionnaireVersionGroupDTO;
    }
}