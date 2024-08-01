package de.imi.mopat.helper.model;

import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Component
public class QuestionnaireGroupDTOMapper implements Function<QuestionnaireGroup, QuestionnaireGroupDTO> {

    private final QuestionnaireDTOMapper questionnaireDTOMapper;

    @Autowired
    public QuestionnaireGroupDTOMapper(QuestionnaireDTOMapper questionnaireDTOMapper) {
        this.questionnaireDTOMapper = questionnaireDTOMapper;
    }

    @Override
    public QuestionnaireGroupDTO apply(QuestionnaireGroup questionnaireGroup) {

        if (questionnaireGroup == null){
            throw new IllegalArgumentException("QuestionnaireGroup must not be null");
        }
        if (!questionnaireGroup.hasMembers()){
            QuestionnaireGroupDTO noMemberQuestionnaireGroupDTO = new QuestionnaireGroupDTO();
            noMemberQuestionnaireGroupDTO.setGroupId(questionnaireGroup.getId());
            return noMemberQuestionnaireGroupDTO;
        }

        // Sort the questionnaires by version number
        List<QuestionnaireDTO> questionnaireDTOs = questionnaireGroup.getQuestionnaireGroupMembers().stream()
                .map(questionnaireGroupMember -> questionnaireDTOMapper.apply(questionnaireGroupMember.getQuestionnaire()))
                .sorted(Comparator.comparing(QuestionnaireDTO::getVersion))
                .toList();

        // Use the name of the questionnaire with the smallest version number as the group name
        String groupName = questionnaireDTOs.get(0).getName();

        // Get the group ID (assuming all items in groupList have the same group ID)
        Long groupId = questionnaireGroup.getId();

        QuestionnaireGroupDTO questionnaireGroupDTO = new QuestionnaireGroupDTO();
        questionnaireGroupDTO.setGroupId(groupId);
        questionnaireGroupDTO.setGroupName(groupName);
        questionnaireGroupDTO.setQuestionnaireDTOS(questionnaireDTOs);
        return questionnaireGroupDTO;
    }
}